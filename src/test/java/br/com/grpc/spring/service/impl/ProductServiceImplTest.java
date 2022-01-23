package br.com.grpc.spring.service.impl;

import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import br.com.grpc.spring.entity.ProductEntity;
import br.com.grpc.spring.exception.ProductAlreadyExistsException;
import br.com.grpc.spring.exception.ProductNotFoundException;
import br.com.grpc.spring.repository.ProductRepository;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    private static final String PRODUCT_NAME = "Product Name";
    private static final Double PRICE = 10.00;
    private static final Integer QUANTITY_STOCK = 10;
    private static final Long ID = 1L;

    private static final String NAME_FIELD = "name";
    private static final String PRICE_FIELD = "price";
    private static final String QUANTITY_IN_STOCK_FIELD = "quantityInStock";
    private static final String ID_FIELD = "id";

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void createProductSuccessTest(){
        ProductEntity productEntity = createProductEntity();
        Mockito.when(productRepository.save(Mockito.any())).thenReturn(productEntity);

        ProductInputDTO productInputDTO = createProductInputDTO();

        ProductOutputDTO productOutputDTO = productService.create(productInputDTO);

        Assertions.assertThat(productOutputDTO)
                .usingRecursiveComparison()
                .isEqualTo(productEntity);
    }

    @Test
    public void createProductExceptionTest(){
        ProductEntity productEntity = createProductEntity();
        Mockito.when(productRepository.findByNameIgnoreCase(Mockito.any())).thenReturn(Optional.of(productEntity));

        ProductInputDTO productInputDTO = createProductInputDTO();

        Assertions.assertThatExceptionOfType(
                ProductAlreadyExistsException.class).isThrownBy(() -> productService.create(productInputDTO));

    }

    @Test
    public void findByIdProductSuccessTest(){
        ProductEntity productEntity = createProductEntity();

        Mockito.when(productRepository.findById(Mockito.any())).thenReturn(Optional.of(productEntity));

        ProductOutputDTO productOutputDTO = productService.findById(1L);

        Assertions.assertThat(productOutputDTO)
                .usingRecursiveComparison()
                .isEqualTo(productEntity);
    }

    @Test
    public void findByIdProductNotFoundExceptionTest(){

        Mockito.when(productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(
                ProductNotFoundException.class).isThrownBy(() -> productService.findById(1L));
    }

    @Test
    public void deleteProductSuccessTest(){

        ProductEntity productEntity = createProductEntity();

        Mockito.when(productRepository.findById(Mockito.any())).thenReturn(Optional.of(productEntity));

        Assertions.assertThatNoException().isThrownBy(() ->  productService.delete(1L));
    }

    @Test
    public void findAllProductSuccessTest(){

        List<ProductEntity> listProductEntity = createListProductEntity();

        Mockito.when(productRepository.findAll()).thenReturn(listProductEntity);

        List<ProductOutputDTO> listProductOutputDTO = productService.findAll();

        //Outra opção de validação
        //Assertions.assertThat(listProductOutputDTO.size()).isGreaterThan(1);

        Assertions.assertThat(listProductOutputDTO)
                .extracting(ID_FIELD, NAME_FIELD, PRICE_FIELD, QUANTITY_IN_STOCK_FIELD)
                .contains(
                        Tuple.tuple(1L, "Computador", 1234.98, 100),
                        Tuple.tuple(2L, "Celular", 564.66, 500)
                );
    }

    @Test
    public void deleteProductNotFoundExceptionTest(){

        Mockito.when(productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(
                ProductNotFoundException.class).isThrownBy(() -> productService.delete(1L));
    }

    private ProductEntity createProductEntity(){
        return ProductEntity.builder()
                .id(ID)
                .name(PRODUCT_NAME)
                .price(PRICE)
                .quantityInStock(QUANTITY_STOCK).build();
    }

    private ProductInputDTO createProductInputDTO(){
        return ProductInputDTO.builder()
                .name(PRODUCT_NAME)
                .price(PRICE)
                .quantityInStock(QUANTITY_STOCK).build();
    }

    private List<ProductEntity> createListProductEntity(){
        return Arrays.asList(
                new ProductEntity(1L, "Computador", 1234.98, 100),
                new ProductEntity(2L, "Celular", 564.66, 500));
    }
}
