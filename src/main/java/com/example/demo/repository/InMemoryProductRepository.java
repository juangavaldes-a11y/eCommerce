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

    public Product save(Product p) {
        if (p.getId() == null) {
            p.setId(idSeq.getAndIncrement());
            p.setCreatedAt(Instant.now());
        }
        store.put(p.getId(), p);
        return p;
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Product> findAll() {
        // return an unmodifiable copy snapshot of current values to avoid callers
        // mutating the internal store and to provide consistent view
        return List.copyOf(store.values());
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public void clear() { store.clear(); }
}
