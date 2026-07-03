package com.etiya.productservice.services.concretes;

import com.etiya.productservice.entities.Product;
import com.etiya.productservice.repositories.ProductRepository;
import com.etiya.productservice.services.abstracts.ProductService;
import com.etiya.productservice.services.dtos.requests.CreateProductRequest;
import com.etiya.productservice.services.dtos.requests.UpdateProductRequest;
import com.etiya.productservice.services.dtos.responses.CreatedProductResponse;
import com.etiya.productservice.services.dtos.responses.DeletedProductResponse;
import com.etiya.productservice.services.dtos.responses.GetAllProductsResponse;
import com.etiya.productservice.services.dtos.responses.GetByIdProductResponse;
import com.etiya.productservice.services.dtos.responses.UpdatedProductResponse;
import com.etiya.productservice.config.CacheConfig;
import com.etiya.productservice.services.exceptions.BusinessException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business layer implementation. Maps between request/response DTOs and the entity,
 * and applies business rules before delegating to the data access layer.
 *
 * <p>GET islemleri Redis'te cache'lenir: {@code getAll()} -> "products" cache'i,
 * {@code getById()} -> "product" cache'i. Veriyi degistiren {@code add}, {@code update}
 * ve {@code delete} islemleri her iki cache'i de tamamen bosaltir
 * ({@code allEntries = true}) — boylece bir sonraki GET guncel veriyi veritabanindan
 * okuyup yeniden cache'ler.</p>
 */
@Service
public class ProductManager implements ProductService {

    private final ProductRepository productRepository;

    public ProductManager(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @CacheEvict(value = {CacheConfig.PRODUCTS_CACHE, CacheConfig.PRODUCT_CACHE}, allEntries = true)
    public CreatedProductResponse add(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        Product saved = productRepository.save(product);

        return new CreatedProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getUnitPrice(),
                saved.getStock(),
                saved.getDescription());
    }

    @Override
    @CacheEvict(value = {CacheConfig.PRODUCTS_CACHE, CacheConfig.PRODUCT_CACHE}, allEntries = true)
    public UpdatedProductResponse update(UpdateProductRequest request) {
        Product product = findProductOrThrow(request.getId());
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        Product saved = productRepository.save(product);

        return new UpdatedProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getUnitPrice(),
                saved.getStock(),
                saved.getDescription());
    }

    @Override
    @CacheEvict(value = {CacheConfig.PRODUCTS_CACHE, CacheConfig.PRODUCT_CACHE}, allEntries = true)
    public DeletedProductResponse delete(int id) {
        Product product = findProductOrThrow(id);
        productRepository.deleteById(id);
        return new DeletedProductResponse(product.getId(), product.getName());
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "'all'")
    public List<GetAllProductsResponse> getAll() {
        return productRepository.findAll().stream()
                .map(product -> new GetAllProductsResponse(
                        product.getId(),
                        product.getName(),
                        product.getUnitPrice(),
                        product.getStock(),
                        product.getDescription()))
                .toList();
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCT_CACHE, key = "#id")
    public GetByIdProductResponse getById(int id) {
        Product product = findProductOrThrow(id);
        return new GetByIdProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getStock(),
                product.getDescription());
    }

    private Product findProductOrThrow(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + id));
    }
}
