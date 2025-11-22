package com.example.demo.service;

import com.example.demo.data.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.repository.InMemoryProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {
    private final InMemoryProductRepository repo;

    public ProductService(InMemoryProductRepository repo) {
        this.repo = repo;
    }

    public Long create(ProductRequest req) {
        Product p = new Product(null,
                req.getName(),
                req.getDescription(),
                req.getBrand(),
                req.getPrice() == null ? 0.0 : req.getPrice(),
                req.getInventory() == null ? 0 : req.getInventory(),
                req.getCategories(),
                Instant.now());
        return repo.save(p).getId();
    }

    public Product getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Product> listAll() {
        return repo.findAll();
    }
}
