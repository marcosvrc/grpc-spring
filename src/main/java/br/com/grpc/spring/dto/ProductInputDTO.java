package br.com.grpc.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInputDTO {

    private String name;
    private Double price;
    private Integer quantityInStock;

}
