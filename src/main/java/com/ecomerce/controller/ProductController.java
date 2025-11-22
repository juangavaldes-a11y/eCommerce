package com.example.demo.controller;

import com.example.demo.data.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import com.example.demo.util.UriFactory;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * Create a new product from the provided request body.
     *
     * @param req validated product creation request
     * @return 201 Created with the new product id in the response body and
     *         a Location header pointing to /products/{id}
     */
    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody ProductRequest req) {
        Long id = service.create(req);
        return ResponseEntity.created(UriFactory.productLocation(id)).body(id);
    }

    /**
     * Retrieve a product by its id.
     *
     * @param id product identifier
     * @return 200 OK with the Product if found, or 404 Not Found if missing
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
