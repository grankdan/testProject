package com.task.test;

import com.task.test.data.Product;
import com.task.test.data.ProductRepository;
import com.task.test.errors.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(@Autowired final ProductRepository productRepository) {
        Assert.notNull(productRepository, "Product repository was not correctly injected");
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public List<EntityModel<Product>> read() {
        return productRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/{id}")
    public EntityModel<Product> read(@PathVariable final Long id) {
        return toModel(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @PostMapping("/products")
    EntityModel<Product> create(@RequestBody final Product product) {
        return toModel(productRepository.save(product));
    }

    @PutMapping(value = "/products/{id}")
    public EntityModel<Product> update(@RequestBody final Product update, @PathVariable final Long id) {
        return toModel(productRepository.findById(id).map(existing -> {
            existing.copyFields(update);
            return productRepository.save(existing);
        }).orElseGet(() -> {
            update.setId(id);
            return productRepository.save(update);
        }));
    }

    @DeleteMapping("/products/{id}")
    void delete(@PathVariable final Long id) {
        productRepository.findById(id).ifPresentOrElse(product -> productRepository.deleteById(id), () -> {
            throw new ProductNotFoundException(id);
        });
    }

    private EntityModel<Product> toModel(final Product product) {
        return EntityModel.of(product,
                linkTo(methodOn(ProductController.class).read()).withRel("products"),
                linkTo(methodOn(ProductController.class).read(product.getId())).withSelfRel()
        );
    }
}
