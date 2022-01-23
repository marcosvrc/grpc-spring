package br.com.grpc.spring.service.impl;

import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import br.com.grpc.spring.entity.ProductEntity;
import br.com.grpc.spring.exception.ProductAlreadyExistsException;
import br.com.grpc.spring.exception.ProductNotFoundException;
import br.com.grpc.spring.repository.ProductRepository;
import br.com.grpc.spring.service.IProductService;
import br.com.grpc.spring.util.ProductConverterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductOutputDTO create(ProductInputDTO inputDTO) {
        this.checkDuplicity(inputDTO.getName());
        var productEntity = ProductConverterUtil.productInputDtoToPoductEntity(inputDTO);
        var productCreated = this.productRepository.save(productEntity);
        return ProductConverterUtil.productEntityToProductOutputDto(productCreated);
    }

    @Override
    public ProductOutputDTO findById(Long id) {
        Optional<ProductEntity> productEntity = productRepository.findById(id);
        productEntity.orElseThrow(() -> new ProductNotFoundException(id));
        return ProductConverterUtil.productEntityToProductOutputDto(productEntity.get());
    }

    @Override
    public void delete(Long id) {
        Optional<ProductEntity> productEntity = productRepository.findById(id);
        productEntity.orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(productEntity.get());
    }

    @Override
    public List<ProductOutputDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductConverterUtil::productEntityToProductOutputDto)
                .collect(Collectors.toList());
    }

    private void checkDuplicity(String name){
        this.productRepository.findByNameIgnoreCase(name)
                .ifPresent(e -> {
                    throw new ProductAlreadyExistsException(name);
                });
    }
}
