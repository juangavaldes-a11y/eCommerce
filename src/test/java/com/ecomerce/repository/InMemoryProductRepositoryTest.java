package com.ecomerce.repository;

import com.ecomerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
    }

    private static Product newProduct(String name) {
        return new Product(null, name, "desc", "brand", 1.0, 1, List.of("c"), null);
    }

    @Test
    void save_assignsSequentialIdsAndCreationTimestamp() {
        Instant before = Instant.now();

        Product first = repo.save(newProduct("first"));
        Product second = repo.save(newProduct("second"));

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
        assertNotNull(first.getCreatedAt());
        assertFalse(first.getCreatedAt().isBefore(before));
    }

    @Test
    void save_keepsExistingIdAndTimestampWhenUpdating() {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Product existing = new Product(42L, "kept", "d", "b", 2.0, 2, List.of(), created);

        Product saved = repo.save(existing);

        assertSame(existing, saved);
        assertEquals(42L, saved.getId());
        assertEquals(created, saved.getCreatedAt());
        assertTrue(repo.findById(42L).isPresent());
    }

    @Test
    void save_replacesAnExistingEntryWithTheSameId() {
        Product saved = repo.save(newProduct("original"));
        saved.setName("renamed");
        repo.save(saved);

        assertEquals(1, repo.findAll().size());
        assertEquals("renamed", repo.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertTrue(repo.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAnUnmodifiableSnapshot() {
        repo.save(newProduct("a"));
        List<Product> snapshot = repo.findAll();

        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(newProduct("b")));

        repo.save(newProduct("c"));
        assertEquals(1, snapshot.size(), "an earlier snapshot must not reflect later writes");
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void deleteById_removesOnlyTheRequestedProduct() {
        Product keep = repo.save(newProduct("keep"));
        Product drop = repo.save(newProduct("drop"));

        repo.deleteById(drop.getId());
        repo.deleteById(12345L); // deleting an unknown id is a no-op

        assertTrue(repo.findById(drop.getId()).isEmpty());
        assertTrue(repo.findById(keep.getId()).isPresent());
    }

    @Test
    void clear_removesEverything() {
        repo.save(newProduct("a"));
        repo.save(newProduct("b"));

        repo.clear();

        assertTrue(repo.findAll().isEmpty());
    }
}
