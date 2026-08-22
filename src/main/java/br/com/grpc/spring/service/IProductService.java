package br.com.grpc.spring.service;

import br.com.grpc.spring.dto.ProductInputDTO;
import br.com.grpc.spring.dto.ProductOutputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;

public interface IProductService {

    ProductOutputDTO create(@Valid ProductInputDTO inputDTO);
    ProductOutputDTO findById(@Positive Long id);
    void delete(@Positive Long id);

    /**
     * Lista produtos paginados. {@code page} < 0 é tratado como 0; {@code size} <= 0
     * usa o tamanho padrão ({@value br.com.grpc.spring.service.impl.ProductServiceImpl#DEFAULT_PAGE_SIZE});
     * {@code size} acima do máximo é limitado a {@value br.com.grpc.spring.service.impl.ProductServiceImpl#MAX_PAGE_SIZE}.
     */
    Page<ProductOutputDTO> findAll(int page, int size);
}
