package dev.amirgol.smartbill.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Defines a pointcut representing the controller layer. Matches any method in
     * classes under the {@code dev.amirgol.smartbill.presentation.controller} package.
     */
    @Pointcut("within(dev.amirgol.smartbill..presentation.controller..*)")
    public void controllerLayer() {
    }

    /**
     * Logs entry into the controller method along with method name and arguments.
     *
     * @param jp the join point representing the method being executed
     */
    @Before("controllerLayer()")
    public void logBeforeController(JoinPoint jp) {
        logger.info("\uD83D\uDD0D [ENTER] {}.{}() with args = {}",
                jp.getSignature().getDeclaringType().getSimpleName(), // Class name
                jp.getSignature().getName(), // Method name
                jp.getArgs() // Method arguments
        );
    }

    /**
     * Logs exit from the controller method along with method name, arguments, and return value.
     *
     * @param jp     the join point representing the method being executed
     * @param result the return value of the method being executed
     */
    @AfterReturning(pointcut = "controllerLayer()", returning = "result")
    public void logAfterController(JoinPoint jp, Object result) {
        logger.info("✅ [EXIT] {}.{}() returned = {}",
                jp.getTarget().getClass().getSimpleName(),
                jp.getSignature().getName(),
                result

        );
    }

    /**
     * Logs an exception thrown from the controller method along with method name and exception message.
     *
     * @param jp the join point representing the method being executed
     * @param ex the exception thrown by the method
     */
    @AfterThrowing(pointcut = "within(dev.amirgol.smartbill..presentation.controller..*) || controllerLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint jp, Throwable ex) {
        logger.error("❌ [EXCEPTION] {}.{}() throws = {}",
                jp.getTarget().getClass().getSimpleName(),
                jp.getSignature().getName(),
                ex.getMessage()
        );
    }
}
