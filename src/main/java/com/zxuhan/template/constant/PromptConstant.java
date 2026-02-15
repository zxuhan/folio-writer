package com.zxuhan.template.constant;

/**
 * Prompt template constants.
 */
public interface PromptConstant {

    /**
     * Agent 1: Generate title options.
     */
    String AGENT1_TITLE_PROMPT = """
            You are an expert at crafting viral article titles that captivate readers.

            Based on the following topic, generate 3-5 viral article title options:
            Topic: {topic}

            Requirements:
            1. Each option must include a main title and a subtitle.
            2. The main title should include numbers and emotionally charged words to grab attention.
            3. The subtitle should supplement and reinforce the main title's appeal.
            4. Titles should be concise and punchy — no more than 30 words.
            5. Each option should approach the topic from a distinct angle.
            6. The style should match that of trending social-media articles.

            Return JSON only, with no additional content:
            [
              {
                "mainTitle": "Main Title 1",
                "subTitle": "Subtitle 1"
              },
              {
                "mainTitle": "Main Title 2",
                "subTitle": "Subtitle 2"
              },
              {
                "mainTitle": "Main Title 3",
                "subTitle": "Subtitle 3"
              }
            ]
            """;

    /**
     * Agent 2: Generate outline.
     */
    String AGENT2_OUTLINE_PROMPT = """
            You are a professional article planner skilled at designing article structures.

            Based on the following title, generate an article outline:
            Main Title: {mainTitle}
            Subtitle: {subTitle}
            {descriptionSection}

            Requirements:
            1. The outline must have a clear logical structure.
            2. Include an introduction, 3-5 core points, and a concluding section.
            3. Each section must have a clear heading and 2-3 key points.
            4. The outline should suit an article of approximately 2000 words.

            Return JSON only, with no additional content:
            {
              "sections": [
                {
                  "section": 1,
                  "title": "Section Title",
                  "points": ["Point 1", "Point 2"]
                }
              ]
            }
            """;

    /**
     * User supplementary description section (dynamically injected into AGENT2_OUTLINE_PROMPT).
     */
    String AGENT2_DESCRIPTION_SECTION = """

            User additional requirements: {userDescription}
            Please fully incorporate the user's additional requirements into the outline.
            """;

    /**
     * SVG conceptual diagram generation prompt.
     */
    String SVG_DIAGRAM_GENERATION_PROMPT = """
            ### Background ###
            You are a senior information visualization designer skilled at transforming abstract concepts into clear, intuitive SVG diagrams.
            Your work has appeared in reputable media and technical documentation, characterized by a clean, modern, and logically structured style.

            ### Requirement ###
            {requirement}

            ### Task Steps ###
            1. Analyze the requirement: understand the core concept and logical relationships to convey.
            2. Design the layout: determine the overall structure (radial, hierarchical, flowchart, etc.).
            3. Choose elements: use basic shapes such as circles, rectangles, arrows, and connectors.
            4. Apply color: use a modern color scheme to ensure visual harmony.
            5. Generate code: output complete, well-formed SVG code.

            ### Technical Specifications ###
            - Must include the <?xml version="1.0" encoding="UTF-8"?> declaration.
            - Must set viewBox="0 0 800 600" to support responsive scaling.
            - Use font-family="Arial, sans-serif" for cross-platform compatibility.
            - Use semantic id and class names.

            ### Design Style ###
            - Colors: primarily blue (#4A90D9, #6BB3F0, #E8F4FC) with gradient accents.
            - Layout: generous whitespace, even spacing, clear hierarchy.
            - Text: concise labels, appropriate font size (14-18px), high contrast.
            - Connectors: use arrowed lines to indicate direction and relationships, stroke width 2-3px.

            ### Output Requirements ###
            Return only the complete SVG XML code with no explanations or additional content.
            """;

    /**
     * Agent 3: Generate article body content.
     */
    String AGENT3_CONTENT_PROMPT = """
            You are a seasoned content creator skilled at writing high-quality articles.

            Based on the following outline, write the article body:
            Main Title: {mainTitle}
            Subtitle: {subTitle}
            Outline:
            {outline}

            Requirements:
            1. Each section should be substantive, around 300-400 words.
            2. The language should be fluent and engaging.
            3. Include memorable quotes or key sentences to enhance readability.
            4. Add transitional sentences to ensure logical flow.
            5. Use Markdown format with ## headings for each section.

            Return only the Markdown-formatted article body with no additional content.
            """;

    /**
     * Agent 4: Analyze image requirements (supports multiple image sources, uses placeholder approach).
     */
    String AGENT4_IMAGE_REQUIREMENTS_PROMPT = """
            You are a professional digital media editor skilled at selecting and placing images in articles.

            Based on the following article content, analyze the image requirements and insert image placeholders into the body:
            Main Title: {mainTitle}
            Article Body:
            {content}

            [IMPORTANT] Available image methods (strictly choose only from the list below — do not use any unlisted methods):
            {availableMethods}

            Usage requirements for each image method:
            {methodUsageGuide}

            General requirements:
            1. Identify positions that need images (cover, key sections, between paragraphs, etc.).
            2. Decide the number of images flexibly based on the article's content and structure — avoid too many or too few.
            3. **Insert placeholders into the body** using the following two formats:
               - Regular image placeholder: {{IMAGE_PLACEHOLDER_N}}, where N is the image sequence number (1, 2, 3...); must occupy its own line.
               - Icon placeholder: {{ICON_PLACEHOLDER_N}}; may be placed inline within a text line (for ICONIFY type only).
               - Note: the cover image (position=1) does not need a placeholder and must not appear in the body.
               - Other image placeholders may be placed at any appropriate position (after section headings, between paragraphs, within list items, inline in text, etc.).
            4. **The imageSource field must be exactly one of the available methods listed above — no other values are allowed.**
            5. placeholderId must exactly match the placeholder inserted in the body.
            6. position=1 is the cover image.

            Return JSON only, with no additional content:
            {
              "contentWithPlaceholders": "",
              "imageRequirements": [
                {
                  "position": 1,
                  "type": "cover",
                  "sectionTitle": "",
                  "imageSource": "NANO_BANANA",
                  "keywords": "",
                  "prompt": "A modern minimalist illustration of AI technology concept, featuring abstract neural network patterns with blue and purple gradient colors, clean design suitable for article cover, 16:9 aspect ratio",
                  "placeholderId": ""
                },
                {
                  "position": 2,
                  "type": "section",
                  "sectionTitle": "Section Title 1",
                  "imageSource": "PEXELS",
                  "keywords": "business success teamwork office",
                  "prompt": "",
                  "placeholderId": "{{IMAGE_PLACEHOLDER_1}}"
                },
                {
                  "position": 3,
                  "type": "inline",
                  "sectionTitle": "",
                  "imageSource": "ICONIFY",
                  "keywords": "check circle",
                  "prompt": "",
                  "placeholderId": "{{ICON_PLACEHOLDER_1}}"
                },
                {
                  "position": 4,
                  "type": "section",
                  "sectionTitle": "Section Title 2",
                  "imageSource": "MERMAID",
                  "keywords": "",
                  "prompt": "flowchart TB\\n    A[User Request] --> B[Load Balancer]\\n    B --> C[Application Server]",
                  "placeholderId": "{{IMAGE_PLACEHOLDER_2}}"
                }
              ]
            }
            """;

    // region Article style prompts

    /**
     * Tech style prompt appendix.
     */
    String STYLE_TECH_PROMPT = """

            **Important: Write in a technology style.**
            - Use professional, rigorous language with industry terminology and jargon.
            - Maintain clear logic and support claims with data and facts.
            - Adopt an objective, rational tone and avoid subjective emotional expression.
            - Emphasize technological innovation, trends, and solutions.
            - Where appropriate, cite authoritative sources or expert opinions.
            """;

    /**
     * Emotional style prompt appendix.
     */
    String STYLE_EMOTIONAL_PROMPT = """

            **Important: Write in an emotional style.**
            - Use warm, delicate language that resonates with readers.
            - Employ rhetorical devices such as metaphors and parallelism to enhance expressiveness.
            - Focus on emotional expression, real stories, and personal reflections.
            - Evoke readers' emotional resonance and convey positivity.
            - Include lyrical sentences where appropriate to add warmth.
            """;

    /**
     * Educational style prompt appendix.
     */
    String STYLE_EDUCATIONAL_PROMPT = """

            **Important: Write in an educational style.**
            - Use plain, accessible language to explain concepts clearly and progressively.
            - Maintain a clear structure that guides readers step by step for easy learning.
            - Use examples and analogies to help readers understand complex content.
            - Summarize key knowledge points and provide practical learning suggestions.
            - Encourage critical thinking and inspire independent exploration.
            """;

    /**
     * Light and humorous style prompt appendix.
     */
    String STYLE_HUMOROUS_PROMPT = """

            **Important: Write in a light and humorous style.**
            - Use a relaxed, lively, and witty tone.
            - Incorporate popular slang, playful expressions, and amusing analogies.
            - Include appropriate self-deprecation or lighthearted jabs to add fun.
            - Keep the content easy to read so readers enjoy learning while being entertained.
            - Add interesting anecdotes or memes where fitting, without sacrificing professionalism.
            """;

    /**
     * AI outline modification prompt.
     */
    String AI_MODIFY_OUTLINE_PROMPT = """
            You are a professional article planner skilled at refining article structures based on user feedback.

            Current article information:
            Main Title: {mainTitle}
            Subtitle: {subTitle}

            Current outline:
            {currentOutline}

            User modification suggestions:
            {modifySuggestion}

            Requirements:
            1. Adjust the outline structure according to the user's modification suggestions.
            2. Preserve the logical coherence and completeness of the outline.
            3. Delete sections as suggested, add new ones as requested, and modify as directed.
            4. Keep the JSON format unchanged.
            5. Automatically re-number the sections sequentially.

            Return only the updated JSON outline with no additional content:
            {
              "sections": [
                {
                  "section": 1,
                  "title": "Section Title",
                  "points": ["Point 1", "Point 2"]
                }
              ]
            }
            """;

    // endregion
}
