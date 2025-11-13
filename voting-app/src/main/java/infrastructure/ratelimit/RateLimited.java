package infrastructure.ratelimit;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for rate limiting
 * 
 * Usage:
 * 
 * @RateLimited(type = RateLimitType.VOTING)
 *                   public void vote() { ... }
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * The type of rate limiting to apply
     */
    RateLimitType value() default RateLimitType.QUERY;

    /**
     * Rate limit types
     */
    enum RateLimitType {
        VOTING, // 10 requests per minute
        QUERY, // 100 requests per minute
        ADMIN // 50 requests per minute
    }
}
