package br.com.grpc.spring.handler;

import br.com.grpc.spring.exception.BaseBusinessException;
import io.grpc.StatusException;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Traduz exceções de negócio ({@link BaseBusinessException}) para o {@link io.grpc.Status}
 * gRPC correspondente. Substitui o antigo {@code @GrpcAdvice}/{@code @GrpcExceptionHandler}
 * do starter net.devh pela integração nativa do Spring Boot (>= 4.1): basta expor um bean
 * do tipo {@link GrpcExceptionHandler}.
 */
@Component
public class ExceptionHandler implements GrpcExceptionHandler {

    @Override
    @Nullable
    public StatusException handleException(Throwable exception) {
        if (exception instanceof BaseBusinessException baseBusinessException) {
            return baseBusinessException.getStatusCode()
                    .withCause(baseBusinessException.getCause())
                    .withDescription(baseBusinessException.getErrorMessage())
                    .asException();
        }
        return null;
    }
}
