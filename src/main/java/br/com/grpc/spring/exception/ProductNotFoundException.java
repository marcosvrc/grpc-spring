package br.com.grpc.spring.exception;

import io.grpc.Status;

public class ProductNotFoundException extends BaseBusinessException{

    private static final String ERROR_MESSAGE = "Produto com Id %s não encontrado.";
    private Long id;

    public ProductNotFoundException(Long id){
        super(String.format(ERROR_MESSAGE,id));
        this.id = id;
    }

    @Override
    public Status getStatusCode() {
        return Status.NOT_FOUND;
    }

    @Override
    public String getErrorMessage() {
        return String.format(ERROR_MESSAGE,this.id);
    }
}
