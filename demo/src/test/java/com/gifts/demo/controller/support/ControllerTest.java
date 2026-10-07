package com.gifts.demo.controller.support;

import com.gifts.demo.controller.exception.ApiExceptionHandler;
import com.gifts.demo.security.SecurityConfig;
import com.gifts.demo.security.currentUser.SpringSecurityCurrentUserProvider;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for controller slice tests.
 *
 * <p>Centralises the {@link Import} set shared by all controller tests so that
 * Spring's test-context cache can reuse the same application context across
 * multiple test classes. Each test class still declares
 * {@code @WebMvcTest(SomeController.class)} to restrict the controller slice,
 * but the common infrastructure beans (security, exception handling, current-user
 * provider) are wired in via this single annotation.</p>
 *
 * <p>Controller-specific mapper implementations ({@code *WebMapperImpl}) must
 * still be added via a local {@code @Import} on the test class itself, because
 * they differ per controller and cannot be shared.</p>
 *
 * <pre>
 * {@code
 * @WebMvcTest(GroupController.class)
 * @ControllerTest
 * class GroupControllerTest { ... }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        SecurityConfig.class,
        ApiExceptionHandler.class,
        SpringSecurityCurrentUserProvider.class
})
public @interface ControllerTest {
}
