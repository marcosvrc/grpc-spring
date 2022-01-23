package br.com.grpc.spring.util;

import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.entity.ProductEntity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductConverterUtilTest {

    @Test
    public void productEntityToProductOutPutDtoTest(){
        var productEntity = new ProductEntity(1L, "Product Name", 10.00, 10);
        var productOutPutDto = ProductConverterUtil.productEntityToProductOutputDto(productEntity);

        Assertions.assertThat(productEntity)
                .usingRecursiveComparison()
                .isEqualTo(productOutPutDto);
    }

    @Test
    public void productInputDtoToProductEntity(){
        var productInputDto = new ProductInputDTO("Product Name", 10.00, 10);
        var productEntity = ProductConverterUtil.productInputDtoToPoductEntity(productInputDto);

        Assertions.assertThat(productInputDto)
                .usingRecursiveComparison()
                .isEqualTo(productEntity);
    }
}
