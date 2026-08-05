package moksh.urlshortener.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * This class hooks into Spring's connection routing mechanism.
 * It asks the ContextHolder which database type we should use for the current thread.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}
