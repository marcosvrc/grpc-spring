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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ProductServiceImpl implements IProductService {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

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
    public Page<ProductOutputDTO> findAll(int page, int size) {
        int effectivePage = Math.max(page, 0);
        int effectiveSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return productRepository.findAll(PageRequest.of(effectivePage, effectiveSize))
                .map(ProductConverterUtil::productEntityToProductOutputDto);
    }

    private void checkDuplicity(String name){
        this.productRepository.findByNameIgnoreCase(name)
                .ifPresent(e -> {
                    throw new ProductAlreadyExistsException(name);
                });
    }
}
