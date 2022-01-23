package br.com.grpc.spring.handler;

import br.com.grpc.spring.exception.BaseBusinessException;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class ExceptionHandler {

    @GrpcExceptionHandler(BaseBusinessException.class)
    public StatusRuntimeException handleBusinessException(BaseBusinessException baseBusinessException){
        return baseBusinessException.getStatusCode()
                .withCause(baseBusinessException.getCause())
                .withDescription(baseBusinessException.getErrorMessage()).asRuntimeException();
    }
}
