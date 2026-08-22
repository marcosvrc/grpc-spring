package br.com.grpc.spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInputDTO {

    @NotBlank(message = "O nome do produto é obrigatório.")
    private String name;

    @NotNull(message = "O preço é obrigatório.")
    @Positive(message = "O preço deve ser maior que zero.")
    private Double price;

    @NotNull(message = "A quantidade em estoque é obrigatória.")
    @PositiveOrZero(message = "A quantidade em estoque não pode ser negativa.")
    private Integer quantityInStock;

}
