package com.staff.employee.config;

import com.staff.employee.dataloader.EmployeeDataLoader;
import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DataLoaderWebInterceptor implements WebGraphQlInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderWebInterceptor.class);
    private final EmployeeDataLoader employeeDataLoader;

    public DataLoaderWebInterceptor(EmployeeDataLoader employeeDataLoader) {
        this.employeeDataLoader = employeeDataLoader;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        // Create a per-request registry to avoid sharing loaders between requests
        DataLoaderRegistry registry = new DataLoaderRegistry();
        registry.register("employeeDataLoader", employeeDataLoader.getDataLoader());
        request.configureExecutionInput((exec, builder) -> builder.dataLoaderRegistry(registry).build());
        return chain.next(request);
    }
}
