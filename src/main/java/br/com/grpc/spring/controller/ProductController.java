package br.com.grpc.spring.controller;

import br.com.grpc.spring.EmptyResponse;
import br.com.grpc.spring.FindAllRequest;
import br.com.grpc.spring.ProductRequest;
import br.com.grpc.spring.ProductResponse;
import br.com.grpc.spring.ProductResponseList;
import br.com.grpc.spring.ProductServiceGrpc;
import br.com.grpc.spring.RequestById;
import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import br.com.grpc.spring.service.IProductService;
import br.com.grpc.spring.util.ProductConverterUtil;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.grpc.server.service.GrpcService;

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
    public void findAll(FindAllRequest request, StreamObserver<ProductResponseList> responseObserver) {
        Page<ProductOutputDTO> productPage = productService.findAll(request.getPage(), request.getSize());
        List<ProductResponse> productResponseList = productPage.getContent().stream()
                .map(ProductConverterUtil::productOutPutToProductResponse)
                .collect(Collectors.toList());

        responseObserver.onNext(ProductResponseList.newBuilder()
                .addAllProduct(productResponseList)
                .setPage(productPage.getNumber())
                .setSize(productPage.getSize())
                .setTotalElements(productPage.getTotalElements())
                .setTotalPages(productPage.getTotalPages())
                .build());
        responseObserver.onCompleted();
    }


    @Override
    public void delete(RequestById request, StreamObserver<EmptyResponse> responseObserver) {
        productService.delete(request.getId());
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }


}
