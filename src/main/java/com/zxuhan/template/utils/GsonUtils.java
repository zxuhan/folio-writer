package com.zxuhan.template.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * Gson utility class
 * Provides a shared Gson instance to avoid repeated instantiation
 *
 */
@Slf4j
public class GsonUtils {

    /**
     * Singleton Gson instance
     */
    private static final Gson GSON = new GsonBuilder()
            .create();

    private GsonUtils() {
        // Private constructor - prevent instantiation
    }

    /**
     * Strip markdown code fences from an LLM response.
     *
     * Gemini and other models often wrap JSON in ```json ... ``` even when the
     * prompt asks for raw JSON. This helper unwraps the fence so the result can
     * be fed straight to {@link #fromJson}.
     */
    public static String unwrapJson(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }

    /**
     * Get the shared Gson instance
     *
     * @return Gson instance
     */
    public static Gson getInstance() {
        return GSON;
    }

    /**
     * Serialize an object to a JSON string
     *
     * @param obj object to serialize
     * @return JSON string
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

    /**
     * Deserialize a JSON string to an object
     *
     * @param json  JSON string
     * @param clazz target type
     * @param <T>   generic type
     * @return deserialized object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, clazz);
    }

    /**
     * Deserialize a JSON string to a generic object
     *
     * @param json      JSON string
     * @param typeToken TypeToken type reference
     * @param <T>       generic type
     * @return deserialized object
     */
    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, typeToken.getType());
    }

    /**
     * Deserialize a JSON string using a Type
     *
     * @param json JSON string
     * @param type Type reference
     * @param <T>  generic type
     * @return deserialized object
     */
    public static <T> T fromJson(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, type);
    }

    /**
     * Safely deserialize a JSON string; returns null on parse failure
     *
     * @param json  JSON string
     * @param clazz target type
     * @param <T>   generic type
     * @return deserialized object, or null if parsing fails
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            log.error("JSON parse failed, json={}", json, e);
            return null;
        }
    }

    /**
     * Safely deserialize a JSON string to a generic object; returns null on parse failure
     *
     * @param json      JSON string
     * @param typeToken TypeToken type reference
     * @param <T>       generic type
     * @return deserialized object, or null if parsing fails
     */
    public static <T> T fromJsonSafe(String json, TypeToken<T> typeToken) {
        try {
            return fromJson(json, typeToken);
        } catch (JsonSyntaxException e) {
            log.error("JSON parse failed, json={}", json, e);
            return null;
        }
    }
}
