package br.com.grpc.spring.controller;

import br.com.grpc.spring.*;
import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import br.com.grpc.spring.service.IProductService;
import br.com.grpc.spring.util.ProductConverterUtil;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ProductController extends ProductServiceGrpc.ProductServiceImplBase {

    @Autowired
    private IProductService  productService;

    @Override
    public void create(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        ProductInputDTO productInputDto = ProductConverterUtil.productRequestToProductInputDto(request);
        ProductOutputDTO productOutputDTO = productService.create(productInputDto);

        responseObserver.onNext(ProductConverterUtil.productOutPutToProductResponse(productOutputDTO));
        responseObserver.onCompleted();
    }

    @Override
    public void findById(RequestById request, StreamObserver<ProductResponse> responseObserver) {
        ProductOutputDTO productOutputDTO = productService.findById(request.getId());
        responseObserver.onNext(ProductConverterUtil.productOutPutToProductResponse(productOutputDTO));
        responseObserver.onCompleted();
    }

    @Override
    public void findAll(EmptyRequest request, StreamObserver<ProductResponseList> responseObserver) {
        List<ProductOutputDTO> productOutputList = productService.findAll();
        List<ProductResponse> productResponseList =
                productOutputList.stream().map(
                        ProductConverterUtil::productOutPutToProductResponse)
                        .collect(Collectors.toList());

        responseObserver.onNext(ProductResponseList.newBuilder().addAllProduct(productResponseList).build());
        responseObserver.onCompleted();
    }


    @Override
    public void delete(RequestById request, StreamObserver<EmptyResponse> responseObserver) {
        productService.delete(request.getId());
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }


}
