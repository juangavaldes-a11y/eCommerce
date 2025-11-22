package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository {
    private final ConcurrentHashMap<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    /**
     * Persist the given product. If the product has no id, a new id is assigned
     * and the creation timestamp is set.
     *
     * @param p product to save
     * @return the saved product (same instance) with id populated
     */
    public Product save(Product p) {
        if (p.getId() == null) {
            p.setId(idSeq.getAndIncrement());
            p.setCreatedAt(Instant.now());
        }
        store.put(p.getId(), p);
        return p;
    }

    /**
     * Find a product by id.
     *
     * @param id product identifier
     * @return Optional containing the product if present
     */
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Return an unmodifiable snapshot of all products currently in the store.
     * This avoids exposing internal mutable structures to callers.
     *
     * @return unmodifiable list snapshot of products
     */
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    /**
     * Remove the product with the given id from the store.
     *
     * @param id product identifier to remove
     */
    public void deleteById(Long id) {
        store.remove(id);
    }

    /**
     * Clear all products from the in-memory store. Primarily useful for tests.
     */
    public void clear() { store.clear(); }
}
