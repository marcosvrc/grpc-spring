package br.com.grpc.spring.controller;

import br.com.grpc.spring.*;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext
public class ProductControllerTest {

    private static final String NAME_FIELD = "name";
    private static final String PRICE_FIELD = "price";
    private static final String QUANTITY_IN_STOCK_FIELD = "quantity_in_stock";
    private static final String ID_FIELD = "id";

    @GrpcClient("inProcess")
    private ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    public void setUp(){
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void createProductSuccessTest(){

        ProductRequest productRequest = createProductRequest("Product Name");

        ProductResponse productResponse = productServiceBlockingStub.create(productRequest);

        Assertions.assertThat(productRequest)
                .usingRecursiveComparison()
                .comparingOnlyFields(NAME_FIELD, PRICE_FIELD, QUANTITY_IN_STOCK_FIELD)
                .isEqualTo(productResponse);
    }

    @Test
    public void createProductAlreadyExistsExceptionTest(){

        ProductRequest productRequest = createProductRequest("Computador");

        Assertions.assertThatExceptionOfType(StatusRuntimeException.class)
                .isThrownBy(() -> productServiceBlockingStub.create(productRequest))
                .withMessage("ALREADY_EXISTS: Produto Computador já cadastrado no sistema.");

    }

    @Test
    public void findByIdProductSuccessTest(){

        RequestById requestById = createRequestById(1L);

        ProductResponse productResponse = productServiceBlockingStub.findById(requestById);

        Assertions.assertThat(productResponse.getId()).isEqualTo(requestById.getId());
    }

    @Test
    public void findByIdProductNotFoundTest(){

        RequestById requestById = createRequestById(5L);

        Assertions.assertThatExceptionOfType(StatusRuntimeException.class)
                .isThrownBy(() -> productServiceBlockingStub.findById(requestById))
                .withMessage("NOT_FOUND: Produto com Id 5 não encontrado.");
    }

    @Test
    public void deleteProductNotFoundTest(){

        RequestById requestById = createRequestById(5L);

        Assertions.assertThatExceptionOfType(StatusRuntimeException.class)
                .isThrownBy(() -> productServiceBlockingStub.delete(requestById))
                .withMessage("NOT_FOUND: Produto com Id 5 não encontrado.");
    }

    @Test
    public void findAllProductSuccessTest(){

        ProductResponseList response = productServiceBlockingStub.findAll(EmptyRequest.newBuilder().build());

        Assertions.assertThat(response.getProductList().size()).isGreaterThan(1);
    }

    @Test
    public void deleteProductSuccessTest(){

        RequestById requestById = createRequestById(1L);

        Assertions.assertThatNoException().isThrownBy(() ->  productServiceBlockingStub.delete(requestById));
    }


    private ProductRequest createProductRequest(String name){
        return ProductRequest.newBuilder()
                .setName(name)
                .setPrice(100.00)
                .setQuantityInStock(78).build();
    }

    private RequestById createRequestById(Long id){
        return RequestById.newBuilder()
                .setId(id).build();
    }

}
