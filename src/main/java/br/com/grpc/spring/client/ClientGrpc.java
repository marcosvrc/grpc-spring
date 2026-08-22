package br.com.grpc.spring.client;

import br.com.grpc.spring.EmptyResponse;
import br.com.grpc.spring.FindAllRequest;
import br.com.grpc.spring.ProductRequest;
import br.com.grpc.spring.ProductResponse;
import br.com.grpc.spring.ProductResponseList;
import br.com.grpc.spring.ProductServiceGrpc;
import br.com.grpc.spring.RequestById;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;


public class ClientGrpc {

    // Precisa bater com "app.security.api-key" (variável de ambiente API_KEY) do servidor.
    private static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final String API_KEY =
            System.getenv().getOrDefault("API_KEY", "local-dev-key-change-me");

    public static void main(String[] args) {

        ManagedChannel channel = createChannel();

        ProductServiceGrpc.ProductServiceBlockingStub stub = createStub(channel);

        ProductResponse responseProductA = createProduct(stub, "COMPUTADOR", 2540.99, 400);
        ProductResponse responseProductB = createProduct(stub, "CELULAR", 1098.33, 320);

        System.out.println("ProductA Created: \n" + responseProductA);
        System.out.println("ProductB Created: \n" + responseProductB);

        System.out.println(responseProductA.getNameBytes());

        ProductResponse responseFindProductA = findById(stub,responseProductA.getId());
        System.out.println("Query Product A: \n" + responseFindProductA);

        ProductResponseList responseFindAll = findAll(stub);
        System.out.println("Query All Products (page " + responseFindAll.getPage()
                + ", " + responseFindAll.getTotalElements() + " no total, "
                + responseFindAll.getTotalPages() + " pagina(s)) \n");
        responseFindAll.getProductList().forEach(System.out::println);

        System.out.println("Delete ProductA \n");
        EmptyResponse responseA = deleteProduct(stub,responseProductA.getId());
        System.out.println("Deleted \n");

        System.out.println("Delete ProductB \n");
        EmptyResponse responseB = deleteProduct(stub,responseProductB.getId());
        System.out.println("Deleted \n");


    }

    private static ManagedChannel createChannel(){
        return ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
    }

    private static ProductServiceGrpc.ProductServiceBlockingStub createStub(ManagedChannel channel){
        Metadata headers = new Metadata();
        headers.put(API_KEY_HEADER, API_KEY);
        return ProductServiceGrpc.newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private static ProductResponse findById(ProductServiceGrpc.ProductServiceBlockingStub stub, long id){
        return stub.findById(createRequestById(id));
    }

    private static ProductResponseList findAll(ProductServiceGrpc.ProductServiceBlockingStub stub){
        return stub.findAll(FindAllRequest.newBuilder().setPage(0).setSize(20).build());
    }

    private static ProductResponse createProduct(ProductServiceGrpc.ProductServiceBlockingStub stub,
                               String name, double price, int quantityInStock){
        return stub.create(createProductRequest(name, price, quantityInStock));
    }

    private static EmptyResponse deleteProduct(ProductServiceGrpc.ProductServiceBlockingStub stub, long id){
        return stub.delete(createRequestById(id));
    }


    private static RequestById createRequestById(long id){
        return RequestById.newBuilder().setId(id).build();
    }

    private static ProductRequest createProductRequest(String name, double price, int quantityInStock){
        return ProductRequest.newBuilder()
                .setName(name)
                .setPrice(price)
                .setQuantityInStock(quantityInStock)
                .build();
    }
}
