package com.example.demo.controller;

import com.example.demo.data.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import com.example.demo.util.UriFactory;
import java.util.List;

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

    /**
     * List products with optional filters, sorting and pagination.
     * Supported query params: brand, category, priceMin, priceMax, sort (price/name/date), page, size
     */
    @GetMapping
    public ResponseEntity<List<Product>> list(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false, defaultValue = "date") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        List<Product> res = service.list(brand, category, priceMin, priceMax, sort, page, size);
        return ResponseEntity.ok(res);
    }

    /**
     * Update a product by id. Replaces product fields with the request values.
     * Returns 204 No Content on success, 404 Not Found if the product doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        boolean updated = service.update(id, req);
        if (updated) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete a product by id. Returns 204 No Content on success, 404 Not Found otherwise.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
