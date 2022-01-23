package br.com.grpc.spring.service;

import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;

import java.util.List;

public interface IProductService {

    ProductOutputDTO create(ProductInputDTO inputDTO);
    ProductOutputDTO findById(Long id);
    void delete(Long id);
    List<ProductOutputDTO> findAll();
}
