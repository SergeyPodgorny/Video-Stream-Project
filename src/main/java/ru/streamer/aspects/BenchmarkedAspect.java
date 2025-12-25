package ru.streamer.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Aspect
@Slf4j
public class BenchmarkedAspect {

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
