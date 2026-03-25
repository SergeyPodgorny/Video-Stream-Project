package ru.streamer.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

@Component
@Aspect
public class BenchmarkedAspect {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkedAspect.class);

    @Around("@annotation(ru.streamer.annotations.Benchmarked)")
    public Object performTimeMeasure(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant startTime = Instant.now();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(startTime, Instant.now());
            log.info("Method {} executed in {} ms", methodName, duration.toMillis());
            return result;
        } catch (Throwable throwable) {
            Duration duration = Duration.between(startTime, Instant.now());
            log.warn("Method {} failed after {} ms", methodName, duration.toMillis());
            throw throwable;
        }
    }
}
