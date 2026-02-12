package com.zxuhan.template.annotation;

import java.lang.annotation.*;

/**
 * Agent execution annotation.
 * Marks agent methods to automatically record execution logs and performance metrics.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentExecution {

    /**
     * Agent name.
     * Example: "agent1_generate_titles", "agent2_generate_outline"
     */
    String value();

    /**
     * Agent description.
     */
    String description() default "";
}
