package com.ecomerce.controller;

import com.ecomerce.dto.ProductRequest;
import com.ecomerce.model.Product;
import com.ecomerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecomerce.util.UriFactory;
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
        Product p = service.getById(id);
        return ResponseEntity.ok(p);
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
        service.update(id, req);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a product by id. Returns 204 No Content on success, 404 Not Found otherwise.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
