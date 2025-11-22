package com.example.demo.service;

import com.example.demo.data.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.repository.InMemoryProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final InMemoryProductRepository repo;

    public ProductService(InMemoryProductRepository repo) {
        this.repo = repo;
    }
    /**
     * Create a new product from the provided request DTO and persist it.
     * The repository assigns the id and createdAt timestamp.
     *
     * @param req product data transfer object containing product fields
     * @return the generated id for the new product
     */
    public Long create(ProductRequest req) {
        Product p = new Product(
                null,
                req.getName(),
                req.getDescription(),
                req.getBrand(),
                req.getPrice() == null ? 0.0 : req.getPrice(),
                req.getInventory() == null ? 0 : req.getInventory(),
                req.getCategories(),
                null // createdAt is set by repository
        );
        return repo.save(p).getId();
    }

    /**
     * Retrieve a product by its id.
     *
     * @param id product identifier
     * @return an Optional containing the product if found, otherwise empty
     */
    public Optional<Product> getById(Long id) {
        return repo.findById(id);
    }

    /**
     * Return a snapshot list of all products.
     * The returned list is an unmodifiable snapshot of the repository state.
     *
     * @return list of products
     */
    public List<Product> listAll() {
        return repo.findAll();
    }
}
