package com.staff.employee.config;

import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfig {

    // No state is required here; keep config stateless.

    @Bean
    public Instrumentation dataLoaderInstrumentation() {
        return new DataLoaderDispatcherInstrumentation();
    }

    // Register a custom Long scalar so the schema can use `Long` (64-bit) safely.
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        GraphQLScalarType longScalar = GraphQLScalarType.newScalar()
                .name("Long")
                .description("64-bit signed long")
                .coercing(new Coercing<Long, Long>() {
                    @Override
                    public Long serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof Number) {
                            return ((Number) dataFetcherResult).longValue();
                        }
                        throw new CoercingSerializeException("Unable to serialize value as Long: " + dataFetcherResult);
                    }

                    @Override
                    public Long parseValue(Object input) throws CoercingSerializeException {
                        if (input instanceof Number) {
                            return ((Number) input).longValue();
                        }
                        if (input instanceof String) {
                            try {
                                return Long.parseLong((String) input);
                            } catch (NumberFormatException ex) {
                                throw new CoercingSerializeException("Invalid Long value: " + input);
                            }
                        }
                        throw new CoercingSerializeException("Unable to parse value as Long: " + input);
                    }

                    @Override
                    public Long parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof IntValue) {
                            return ((IntValue) input).getValue().longValue();
                        }
                        if (input instanceof StringValue) {
                            try {
                                return Long.parseLong(((StringValue) input).getValue());
                            } catch (NumberFormatException ex) {
                                throw new CoercingParseLiteralException("Invalid Long literal: " + input);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected AST type 'IntValue' or 'StringValue' for Long but was: " + input);
                    }
                })
                .build();

        return wiring -> wiring.scalar(longScalar);
    }
}
