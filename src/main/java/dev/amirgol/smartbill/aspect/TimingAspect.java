package dev.amirgol.smartbill.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Logs the execution time of any method in the service layer.
     * <p>
     * This advice is applied to any method in the service layer by using the
     * {@code within} pointcut. The {@code within} pointcut is used to narrow
     * the scope of the advice to only the methods in the service layer, since
     * the advice is declared in a separate class.
     * <p>
     * This advice logs the execution time of the method by wrapping the
     * {@code ProceedingJoinPoint.proceed()} call in a timer. The timer
     * measures the time taken to execute the method and logs the result.
     *
     * @param pjp a {@link ProceedingJoinPoint} representing the method being
     *            executed.
     * @return the result of the method execution.
     * @throws Throwable if any error occurs during the execution of the method.
     */
    @Around("within(dev.amirgol.smartbill..service..*)")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = pjp.proceed();
        long end = System.currentTimeMillis();
        long duration = end - start;
        logger.info("⏱️ [{}] executed in {} ms",
                pjp.getSignature(),
                duration
        );
        return proceed;
    }

}
