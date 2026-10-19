package com.ecomerce.service;

import com.ecomerce.dto.ProductRequest;
import com.ecomerce.exception.NotFoundException;
import com.ecomerce.model.Product;
import com.ecomerce.repository.InMemoryProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour tests for {@link ProductService} backed by the real in-memory repository
 * (no mocks), complementing the Mockito-based {@link ProductServiceTest}.
 */
class ProductServiceBehaviourTest {

    private InMemoryProductRepository repo;
    private ProductService svc;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
        svc = new ProductService(repo);
    }

    private static final Instant T1 = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2024-01-02T00:00:00Z");
    private static final Instant T3 = Instant.parse("2024-01-03T00:00:00Z");

    private Product add(long id, String name, String brand, double price, List<String> categories, Instant createdAt) {
        return repo.save(new Product(id, name, "", brand, price, 1, categories, createdAt));
    }

    private static ProductRequest request(String name, Double price, Integer inventory) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setDescription("description");
        r.setBrand("Brand");
        r.setPrice(price);
        r.setInventory(inventory);
        r.setCategories(List.of("cat"));
        return r;
    }

    private static List<Long> ids(List<Product> products) {
        List<Long> ids = new ArrayList<>();
        for (Product p : products) {
            ids.add(p.getId());
        }
        return ids;
    }

    // ---- create -----------------------------------------------------------------------------

    @Test
    void create_persistsEveryFieldAndReturnsTheGeneratedId() {
        Long id = svc.create(request("Widget", 12.5, 3));

        Product stored = svc.getById(id);
        assertEquals("Widget", stored.getName());
        assertEquals("description", stored.getDescription());
        assertEquals("Brand", stored.getBrand());
        assertEquals(12.5, stored.getPrice());
        assertEquals(3, stored.getInventory());
        assertEquals(List.of("cat"), stored.getCategories());
        assertNotNull(stored.getCreatedAt());
    }

    @Test
    void create_defaultsMissingPriceAndInventoryToZero() {
        Long id = svc.create(request("Free", null, null));

        Product stored = svc.getById(id);
        assertEquals(0.0, stored.getPrice());
        assertEquals(0, stored.getInventory());
    }

    @Test
    void create_assignsDistinctIds() {
        assertNotEquals(svc.create(request("A", 1.0, 1)), svc.create(request("B", 1.0, 1)));
    }

    // ---- getById / listAll ------------------------------------------------------------------

    @Test
    void getById_throwsWithTheIdInTheMessageWhenMissing() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.getById(404L));
        assertEquals("Product not found: 404", ex.getMessage());
    }

    @Test
    void listAll_returnsEveryProduct() {
        assertTrue(svc.listAll().isEmpty());
        add(1, "A", "x", 1, List.of(), T1);
        add(2, "B", "x", 1, List.of(), T2);
        assertEquals(2, svc.listAll().size());
    }

    // ---- list: sorting ----------------------------------------------------------------------

    @Test
    void list_sortsByCreationDateByDefault() {
        add(3, "C", "x", 1, List.of(), T3);
        add(1, "A", "x", 1, List.of(), T1);
        add(2, "B", "x", 1, List.of(), T2);

        assertEquals(List.of(1L, 2L, 3L), ids(svc.list(null, null, null, null, null, 0, 10)));
        assertEquals(List.of(1L, 2L, 3L), ids(svc.list(null, null, null, null, "date", 0, 10)));
        assertEquals(List.of(1L, 2L, 3L), ids(svc.list(null, null, null, null, "unknown-key", 0, 10)));
    }

    @Test
    void list_sortsByPriceAscendingAndIgnoresCase() {
        add(1, "A", "x", 30, List.of(), T1);
        add(2, "B", "x", 10, List.of(), T2);
        add(3, "C", "x", 20, List.of(), T3);

        assertEquals(List.of(2L, 3L, 1L), ids(svc.list(null, null, null, null, "price", 0, 10)));
        assertEquals(List.of(2L, 3L, 1L), ids(svc.list(null, null, null, null, "PRICE", 0, 10)));
    }

    @Test
    void list_sortsByNameIgnoringCaseAndTreatingNullAsEmpty() {
        add(1, "banana", "x", 1, List.of(), T1);
        add(2, "Apple", "x", 1, List.of(), T2);
        add(3, null, "x", 1, List.of(), T3);
        add(4, "cherry", "x", 1, List.of(), T1);

        assertEquals(List.of(3L, 2L, 1L, 4L), ids(svc.list(null, null, null, null, "name", 0, 10)));
    }

    // ---- list: filtering --------------------------------------------------------------------

    @Test
    void list_filtersByBrandCaseInsensitivelyAndTrimmed() {
        add(1, "A", "Nike", 1, List.of(), T1);
        add(2, "B", "ADIDAS", 1, List.of(), T2);
        add(3, "C", null, 1, List.of(), T3);

        assertEquals(List.of(1L), ids(svc.list("  nike ", null, null, null, "date", 0, 10)));
        assertEquals(List.of(2L), ids(svc.list("adidas", null, null, null, "date", 0, 10)));
        assertTrue(svc.list("puma", null, null, null, "date", 0, 10).isEmpty());
    }

    @Test
    void list_filtersByCategoryAndToleratesMissingCategories() {
        add(1, "A", "x", 1, List.of("Shoes", "Sport"), T1);
        add(2, "B", "x", 1, List.of("hats"), T2);
        add(3, "C", "x", 1, null, T3);
        add(4, "D", "x", 1, Arrays.asList("shoes", null), T3.plusSeconds(1));

        assertEquals(List.of(1L, 4L), ids(svc.list(null, " SHOES ", null, null, "date", 0, 10)));
        assertEquals(List.of(2L), ids(svc.list(null, "hats", null, null, "date", 0, 10)));
        assertTrue(svc.list(null, "gloves", null, null, "date", 0, 10).isEmpty());
    }

    @Test
    void list_priceBoundsAreInclusive() {
        add(1, "A", "x", 5, List.of(), T1);
        add(2, "B", "x", 10, List.of(), T2);
        add(3, "C", "x", 15, List.of(), T3);

        assertEquals(List.of(2L, 3L), ids(svc.list(null, null, 10.0, null, "price", 0, 10)));
        assertEquals(List.of(1L, 2L), ids(svc.list(null, null, null, 10.0, "price", 0, 10)));
        assertEquals(List.of(2L), ids(svc.list(null, null, 10.0, 10.0, "price", 0, 10)));
        assertTrue(svc.list(null, null, 11.0, 14.0, "price", 0, 10).isEmpty());
    }

    @Test
    void list_combinesFilters() {
        add(1, "A", "Nike", 5, List.of("shoes"), T1);
        add(2, "B", "Nike", 50, List.of("shoes"), T2);
        add(3, "C", "Nike", 5, List.of("hats"), T3);
        add(4, "D", "Adidas", 5, List.of("shoes"), T1);

        assertEquals(List.of(1L), ids(svc.list("nike", "shoes", 1.0, 10.0, "date", 0, 10)));
    }

    // ---- list: pagination -------------------------------------------------------------------

    @Test
    void list_paginates() {
        for (long i = 1; i <= 5; i++) {
            add(i, "P" + i, "x", i, List.of(), T1.plusSeconds(i));
        }

        assertEquals(List.of(1L, 2L), ids(svc.list(null, null, null, null, "price", 0, 2)));
        assertEquals(List.of(3L, 4L), ids(svc.list(null, null, null, null, "price", 1, 2)));
        assertEquals(List.of(5L), ids(svc.list(null, null, null, null, "price", 2, 2)));
        assertTrue(svc.list(null, null, null, null, "price", 3, 2).isEmpty());
    }

    @Test
    void list_normalisesInvalidPagingValues() {
        for (long i = 1; i <= 12; i++) {
            add(i, "P" + i, "x", i, List.of(), T1.plusSeconds(i));
        }

        assertEquals(10, svc.list(null, null, null, null, "price", 0, 0).size(), "size <= 0 falls back to 10");
        assertEquals(10, svc.list(null, null, null, null, "price", 0, -5).size());
        assertEquals(svc.list(null, null, null, null, "price", 0, 10), svc.list(null, null, null, null, "price", -3, 10),
                "negative pages are treated as the first page");
    }

    // ---- update -----------------------------------------------------------------------------

    @Test
    void update_replacesFields() {
        Long id = svc.create(request("Old", 1.0, 1));
        ProductRequest changes = request("New", 99.0, 7);
        changes.setDescription("new description");
        changes.setBrand("NewBrand");
        changes.setCategories(List.of("a", "b"));

        svc.update(id, changes);

        Product updated = svc.getById(id);
        assertEquals("New", updated.getName());
        assertEquals("new description", updated.getDescription());
        assertEquals("NewBrand", updated.getBrand());
        assertEquals(99.0, updated.getPrice());
        assertEquals(7, updated.getInventory());
        assertEquals(List.of("a", "b"), updated.getCategories());
    }

    @Test
    void update_keepsPriceAndInventoryWhenOmitted() {
        Long id = svc.create(request("Old", 5.0, 2));

        svc.update(id, request("Renamed", null, null));

        Product updated = svc.getById(id);
        assertEquals("Renamed", updated.getName());
        assertEquals(5.0, updated.getPrice());
        assertEquals(2, updated.getInventory());
    }

    @Test
    void update_throwsWhenMissing() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.update(77L, request("X", 1.0, 1)));
        assertEquals("Product not found: 77", ex.getMessage());
    }

    // ---- delete -----------------------------------------------------------------------------

    @Test
    void delete_removesTheProduct() {
        Long id = svc.create(request("Doomed", 1.0, 1));

        svc.delete(id);

        assertTrue(repo.findById(id).isEmpty());
        assertThrows(NotFoundException.class, () -> svc.delete(id), "a second delete reports not found");
    }

    @Test
    void delete_throwsWhenMissing() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> svc.delete(5L));
        assertEquals("Product not found: 5", ex.getMessage());
    }
}
