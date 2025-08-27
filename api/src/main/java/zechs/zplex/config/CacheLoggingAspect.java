package zechs.zplex.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Aspect
@Component
public class CacheLoggingAspect {

    private static final Logger logger = Logger.getLogger(CacheLoggingAspect.class.getName());

    @Around("@annotation(cacheable)")
    public Object logCacheHit(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        Object result = joinPoint.proceed();
        logger.info("Executed method " + joinPoint.getKind() + " -> cache miss");
        return result;
    }
}