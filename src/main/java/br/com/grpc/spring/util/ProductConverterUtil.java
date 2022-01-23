package br.com.grpc.spring.util;

import br.com.grpc.spring.ProductRequest;
import br.com.grpc.spring.ProductResponse;
import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import br.com.grpc.spring.entity.ProductEntity;

public class ProductConverterUtil {

    public static ProductOutputDTO productEntityToProductOutputDto(ProductEntity productEntity){
        return ProductOutputDTO.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
                .quantityInStock(productEntity.getQuantityInStock()).build();
    }

    public static ProductEntity productInputDtoToPoductEntity(ProductInputDTO productInputDTO){
        return ProductEntity.builder()
                .id(null)
                .name(productInputDTO.getName())
                .price(productInputDTO.getPrice())
                .quantityInStock(productInputDTO.getQuantityInStock()).build();
    }

    public static ProductResponse productOutPutToProductResponse(ProductOutputDTO productOutputDTO){
        return ProductResponse.newBuilder()
                .setId(productOutputDTO.getId())
                .setName(productOutputDTO.getName())
                .setPrice(productOutputDTO.getPrice())
                .setQuantityInStock(productOutputDTO.getQuantityInStock()).build();
    }

    public static ProductInputDTO productRequestToProductInputDto(ProductRequest productRequest){
        return ProductInputDTO.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .quantityInStock(productRequest.getQuantityInStock()).build();
    }


}
