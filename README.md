# Folio - Agentic Writing

<p align="center">
  <img alt="Java"             src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Spring Boot"      src="https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white">
  <img alt="Spring AI Alibaba" src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.0-FF6A00?logo=spring&logoColor=white">
  <img alt="Gemini"           src="https://img.shields.io/badge/LLM-Gemini%202.5-4285F4?logo=google&logoColor=white">
  <img alt="Vue"              src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white">
  <img alt="TypeScript"       src="https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript&logoColor=white">
  <img alt="Docker"           src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white">
  <img alt="License"          src="https://img.shields.io/badge/License-MIT-blue">
</p>

> A multi-agent backend that turns one topic into a fully illustrated, publish-ready article. Five specialized LLM agents wired into Spring AI Alibaba's `StateGraph`, with token-level streaming, parallel image generation across six providers, an explicit phase state machine you can intervene in, and atomic VIP-quota enforcement. Spring Boot 3 on the back, Vue 3 + Ant Design Vue on the front.

## Try it live

> _Live URL added once deployed_ — `<https://…>`

You can sign up, or use one of the seeded accounts (all share password `12345678`):

| Role  | Account | What it can do |
|-------|---------|----------------|
| Admin | `admin` | Everything — user management, statistics dashboard, unlimited generations |
| VIP   | `vip`   | Unlimited generations + AI image generation + LLM-authored SVG diagrams |
| User  | `user`  | 5 free generations, photo / icon / mermaid / meme images |
| Test  | `test`  | Same as `user`, kept clean for fresh demos |

> Want to test the upgrade flow yourself? Sign up as a regular user, hit **VIP** in the nav, and pay with the Stripe test card `4242 4242 4242 4242` (any future expiry, any CVC). The webhook flips your role to `vip`.

---

## Architecture

```mermaid
flowchart TB
    Start([topic + style]) --> P1[Phase 1 · Title agent]
    P1 -->|user picks a title| P2[Phase 2 · Outline agent · <i>streamed</i>]
    P2 -->|user accepts / refines outline| P3

    subgraph P3 [Phase 3 · single StateGraph · streamed]
      direction LR
      A3[Content agent] --> A4[Image agent] --> PAR[/Parallel images/] --> A5[Merger]
    end

    P3 --> Final([illustrated article])
```

The pipeline is **three independent `StateGraph`s**, one per phase, built and compiled per request inside `ArticleAgentOrchestrator`. Phases 1 and 2 are single-node graphs; Phase 3 is the four-node sequential graph above. Splitting it this way is what lets the user **interrupt between phases** — pick from the title candidates, edit or re-prompt the outline, then commit to the body. Every phase that calls an LLM streams tokens back to the browser over SSE; image generation streams per-image events as they finish.

---

## The agents

| # | Agent | Implementation notes | I/O |
|---|---|---|---|
| 1 | **`TitleGeneratorAgent`** | Prompt asks for 3–5 distinct angles, ≤30 words, with numbers / emotional hooks. Style suffix (`TECH` / `EMOTIONAL` / `EDUCATIONAL` / `HUMOROUS`) appended to steer tone. JSON parsed via `GsonUtils.unwrapJson` to tolerate code-fence wrappers. | `ChatModel.call()` → list of `{mainTitle, subTitle}` |
| 2 | **`OutlineGeneratorAgent`** | Tokens flow through `StreamHandlerContext` and out as `AGENT2_STREAMING:` SSE frames. Optional `userDescription` is interpolated into the prompt — that's how the user steers tone or angle without prompt-engineering. | `ChatModel.stream()` → `OutlineResult` of 3–5 sections |
| 3 | **`ContentGeneratorAgent`** | Receives the full outline as JSON to keep section boundaries; emits `AGENT3_STREAMING:` SSE frames so the UI renders text as it's being written. | `ChatModel.stream()` → Markdown body with `[image_position_N]` placeholders |
| 4 | **`ImageAnalyzerAgent`** | The model decides **what kind** of image each spot wants (photo / AI-render / mermaid / icon / meme / SVG). Output is filtered against the article's `enabledImageMethods` — any disallowed kind is rewritten to the first allowed alternative, so a non-VIP can't get VIP image kinds even if the LLM picked one. | `ChatModel.call()` → `{contentWithPlaceholders, imageRequirements[]}` |
| — | **`ParallelImageGenerator`** | Groups requirements by `imageSource`, runs one `CompletableFuture` per provider, joins with `allOf().join()`. Each successful image emits an `IMAGE_COMPLETE` SSE frame so the UI can render images progressively. Failures are isolated per image; a thread-safe `CopyOnWriteArrayList` collects whatever succeeded. | Pure code → `List<ImageResult>` with R2 URLs |
| 5 | **`ContentMergerAgent`** | Defensive placeholder substitution — warns on missing slots and tolerates three different upstream result shapes (`ArticleState.ImageResult`, `ImageGenerationTool.ImageGenerationResult`, raw `Map`). | Pure code → `fullContent` (final Markdown) |

Every LLM-touching agent is annotated with `@AgentExecution(...)`. An AOP aspect (`AgentExecutionAspect`) intercepts every call and writes a row into `agent_log` (taskId, prompt, duration, status, error message). The save is fired async via `AgentLogService.saveLogAsync`, so logging never sits on the hot path.

### Image strategy + fallback

`ImageServiceStrategy` auto-discovers all `ImageSearchService` beans at `@PostConstruct` and registers them in an `EnumMap<ImageMethodEnum, ImageSearchService>`. For each requirement it:

1. Resolves the chosen provider (or falls back to `getDefaultSearchMethod()` if the source is unknown).
2. Calls the service. If it returns nothing usable, hands off to the strategy-defined alternative.
3. If that fails too, drops to **Picsum** — random photo, but always a valid image.
4. Uploads the bytes to **Cloudflare R2** (S3-compatible) and returns the public URL.

Net effect: an article never ships with broken images, only with degraded ones.

---

## Real-time streaming over SSE

Streaming an LLM response through a state graph is awkward — graph state gets serialized between nodes, and `Consumer<String>` is not serializable. The fix:

- `StreamHandlerContext` holds the per-request callback in a `ThreadLocal<Consumer<String>>`.
- The orchestrator binds it before `graph.invoke(...)` and clears it in a `finally` block.
- Agents pull it via `StreamHandlerContext.send(token)` — no graph-state coupling.
- `SseEmitterManager` (a `ConcurrentHashMap<taskId, SseEmitter>`) handles the wire side with timeout / completion / error callbacks that auto-evict the emitter.

Event types: `AGENT1_COMPLETE`, `AGENT2_STREAMING`, `AGENT2_COMPLETE`, `AGENT3_STREAMING`, `AGENT3_COMPLETE`, `AGENT4_COMPLETE`, `IMAGE_COMPLETE`, `AGENT5_COMPLETE`, `MERGE_COMPLETE`, `ERROR`.

---

## Phase state machine

The article isn't a fire-and-forget async job — it's a stateful conversation the user can step through, abandon, or resume.

```mermaid
stateDiagram-v2
    [*]                --> PENDING
    PENDING            --> TITLE_GENERATING:    create()
    TITLE_GENERATING   --> TITLE_SELECTING:     agent1 done
    TITLE_SELECTING    --> OUTLINE_GENERATING:  user picks title
    OUTLINE_GENERATING --> OUTLINE_EDITING:     agent2 done
    OUTLINE_EDITING    --> OUTLINE_GENERATING:  user asks AI to refine
    OUTLINE_EDITING    --> CONTENT_GENERATING:  user accepts outline
    CONTENT_GENERATING --> [*]
```

`ArticlePhaseEnum.canTransitionTo(...)` validates every move in code — illegal transitions throw a `BusinessException` instead of silently corrupting state. A separate `ArticleStatusEnum` (`PENDING / PROCESSING / COMPLETED / FAILED`) tracks orthogonal lifecycle health for list views and admin dashboards.

---

## VIP, quota, and payments

- **Atomic quota deduction** — `UPDATE user SET quota = quota - 1 WHERE id = ? AND quota > 0` inside a `@Transactional` boundary. Affected-rows = 0 ⇒ `BusinessException("Out of quota")`. No read-then-write window, no need for a distributed lock on the hot path.
- **VIP / admin bypass** — role check skips the deduction entirely.
- **Stripe checkout** — `PaymentService.createVipPaymentSession()` issues a Checkout session; `StripeWebhookController` verifies signatures with `Webhook.constructEvent(...)` before flipping the user to VIP and recording a `payment_record` row.
- **Refunds** — reverse the VIP flag and refund through Stripe in one call.
- **Image-method gating** — `ArticleServiceImpl.validateImageMethods` rejects the request up front if a non-VIP asks for `NANO_BANANA` or `SVG_DIAGRAM`. Inside the pipeline the agent's choices are filtered again, defense in depth.

---

## Tech stack

| Layer    | Stack |
|----------|-------|
| Backend  | Java 21 · Spring Boot 3.5.9 · Spring AI Alibaba 1.1.0 (`StateGraph`) · Spring AI OpenAI 1.0.1 · MyBatis-Flex · Stripe Java · AWS SDK v2 (S3) · OkHttp · Jsoup · Knife4j · Hutool · Lombok |
| LLM      | Gemini 2.5 Flash (text) and Gemini 2.5 Flash Image / Nano Banana (images), called via the OpenAI-compatible endpoint and the Google Gen AI Java SDK |
| Storage  | MySQL 8 · Redis (sessions + Redisson distributed locks) · Cloudflare R2 (images) |
| Frontend | Vue 3.5 · TypeScript 5.8 · Vite 7 · Pinia · Vue Router · Ant Design Vue · ECharts · Axios |
| Infra    | Docker Compose (backend · frontend behind nginx · MySQL · Redis) · GitHub Actions deploy workflow |

---

## Run it locally

### Docker Compose (recommended)

```bash
cp .env.example .env
# fill in: GEMINI_API_KEY, PEXELS_API_KEY (required)
#         STRIPE_API_KEY, R2_*, NANO_BANANA_API_KEY (optional)
docker compose up -d --build
```

| Service     | URL                                  |
|-------------|--------------------------------------|
| Frontend    | <http://localhost>                   |
| Backend API | <http://localhost:8123/api>          |
| API docs    | <http://localhost:8123/api/doc.html> |

MySQL and Redis stay on the internal Docker network — uncomment the `ports:` in `docker-compose.yml` if you want to attach a client.

### Local dev (no Docker)

```bash
# backend
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# fill in API keys
mvn spring-boot:run

# frontend (another shell)
cd frontend && npm install && npm run dev
```

Backend on `:8123/api`, frontend on `:5173`.

---

## Project layout

```
src/main/java/com/zxuhan/template/
├── agent/
│   ├── agents/                              # 5 agents (Title / Outline / Content / Image / Merger)
│   ├── parallel/ParallelImageGenerator.java # CompletableFuture image fan-out
│   ├── tools/ImageGenerationTool.java       # @Tool wrapper callable from agents
│   ├── context/StreamHandlerContext.java    # ThreadLocal SSE bridge
│   ├── config/AgentConfig.java
│   └── ArticleAgentOrchestrator.java        # builds + invokes the 3 phase StateGraphs
├── annotation/AgentExecution.java           # AOP marker
├── aop/AgentExecutionAspect.java            # auto-logs every agent call
├── service/
│   ├── ImageServiceStrategy.java            # provider selection + fallback
│   ├── {Pexels,Mermaid,Iconify,EmojiPack,NanoBanana,SvgDiagram}Service.java
│   ├── R2Service.java                       # S3-compatible upload
│   ├── ArticleAgentService.java             # legacy synchronous fallback path
│   └── ArticleAsyncService.java             # @Async entry into the orchestrator
├── controller/                              # Article · User · Payment · StripeWebhook · Statistics · Health
├── manager/SseEmitterManager.java
├── model/{entity,dto,vo,enums}/
└── config/                                  # CORS · JSON · Async · per-provider config
sql/                                         # base schema + incremental migrations
frontend/                                    # Vue 3 + Vite SPA
```

---

## Suggested GitHub repo metadata

**About**

> Multi-agent article generator: Spring AI Alibaba StateGraph, token streaming over SSE, parallel image generation across six providers, Vue 3 frontend.

**Topics**

```
spring-boot · spring-ai · spring-ai-alibaba · multi-agent · ai-agents
llm · gemini · stategraph · sse · server-sent-events
ai-content-generation · ai-article-generator · vue3 · typescript
ant-design-vue · stripe · mybatis-flex · cloudflare-r2 · docker · java21
```

---

## License

MIT.
