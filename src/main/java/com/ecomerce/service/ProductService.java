package com.example.demo.service;

import com.example.demo.data.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.repository.InMemoryProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

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

    /**
     * List products with optional filtering, sorting and pagination.
     *
     * @param brand optional brand filter (case-insensitive)
     * @param category optional category filter (case-insensitive)
     * @param priceMin optional minimum price (inclusive)
     * @param priceMax optional maximum price (inclusive)
     * @param sort optional sort key: "price", "name" or "date" (default: date)
     * @param page zero-based page index
     * @param size page size (must be > 0)
     * @return list of products matching the criteria (page snapshot)
     */
    public List<Product> list(String brand, String category, Double priceMin, Double priceMax, String sort, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        Comparator<Product> comparator = Comparator.comparing(Product::getCreatedAt);
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price":
                    comparator = Comparator.comparingDouble(Product::getPrice);
                    break;
                case "name":
                    comparator = Comparator.comparing(p -> p.getName() == null ? "" : p.getName(), String.CASE_INSENSITIVE_ORDER);
                    break;
                case "date":
                default:
                    comparator = Comparator.comparing(Product::getCreatedAt);
            }
        }

        final String brandFilter = brand == null ? null : brand.trim().toLowerCase();
        final String categoryFilter = category == null ? null : category.trim().toLowerCase();

        return repo.findAll().stream()
                .filter(p -> brandFilter == null || (p.getBrand() != null && p.getBrand().toLowerCase().equals(brandFilter)))
                .filter(p -> categoryFilter == null || (p.getCategories() != null && p.getCategories().stream().anyMatch(c -> c != null && c.toLowerCase().equals(categoryFilter))))
                .filter(p -> priceMin == null || p.getPrice() >= priceMin)
                .filter(p -> priceMax == null || p.getPrice() <= priceMax)
                .sorted(comparator)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing product by id with values from the request.
     * Only replaces fields from the request (no partial-merge logic beyond direct assignment).
     *
     * @param id product identifier
     * @param req update request payload
     * @return true if product existed and was updated, false if not found
     */
    public boolean update(Long id, ProductRequest req) {
        return repo.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setDescription(req.getDescription());
            existing.setBrand(req.getBrand());
            existing.setPrice(req.getPrice() == null ? existing.getPrice() : req.getPrice());
            existing.setInventory(req.getInventory() == null ? existing.getInventory() : req.getInventory());
            existing.setCategories(req.getCategories());
            repo.save(existing);
            return true;
        }).orElse(false);
    }

    /**
     * Delete a product by id.
     *
     * @param id product identifier
     * @return true if the product existed and was deleted, false otherwise
     */
    public boolean delete(Long id) {
        return repo.findById(id).map(p -> {
            repo.deleteById(id);
            return true;
        }).orElse(false);
    }
}
