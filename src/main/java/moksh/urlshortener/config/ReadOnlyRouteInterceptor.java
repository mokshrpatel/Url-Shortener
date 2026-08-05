package moksh.urlshortener.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This Aspect intercepts methods annotated with @Transactional.
 * If readOnly = true, it routes the connection to the SLAVE.
 * Otherwise, it routes to the MASTER.
 */
@Aspect
@Component
@Order(0) // Ensure this runs before Spring's transaction manager starts the transaction
public class ReadOnlyRouteInterceptor {

    @Around("@annotation(transactional)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
            } else {
                DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
            }
            return proceedingJoinPoint.proceed();
        } finally {
            // Always clear the context to prevent thread-pool pollution
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}