package com.ecomerce.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void constructorPopulatesEveryField() {
        Instant now = Instant.now();
        Product p = new Product(7L, "Name", "Desc", "Brand", 9.5, 4, List.of("a", "b"), now);

        assertEquals(7L, p.getId());
        assertEquals("Name", p.getName());
        assertEquals("Desc", p.getDescription());
        assertEquals("Brand", p.getBrand());
        assertEquals(9.5, p.getPrice());
        assertEquals(4, p.getInventory());
        assertEquals(List.of("a", "b"), p.getCategories());
        assertEquals(now, p.getCreatedAt());
    }

    @Test
    void settersReplaceEveryField() {
        Product p = new Product();
        Instant now = Instant.now();

        p.setId(1L);
        p.setName("n");
        p.setDescription("d");
        p.setBrand("b");
        p.setPrice(2.5);
        p.setInventory(3);
        p.setCategories(List.of("c"));
        p.setCreatedAt(now);

        assertEquals(1L, p.getId());
        assertEquals("n", p.getName());
        assertEquals("d", p.getDescription());
        assertEquals("b", p.getBrand());
        assertEquals(2.5, p.getPrice());
        assertEquals(3, p.getInventory());
        assertEquals(List.of("c"), p.getCategories());
        assertEquals(now, p.getCreatedAt());
    }

    @Test
    void defaultsAreEmpty() {
        Product p = new Product();
        assertNull(p.getId());
        assertNull(p.getName());
        assertEquals(0.0, p.getPrice());
        assertEquals(0, p.getInventory());
    }

    @Test
    void equalityIsBasedOnIdOnly() {
        Product a = new Product(1L, "A", "", "", 1, 1, List.of(), null);
        Product sameId = new Product(1L, "Different", "x", "y", 99, 9, List.of("z"), Instant.now());
        Product otherId = new Product(2L, "A", "", "", 1, 1, List.of(), null);

        assertEquals(a, a);
        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertNotEquals(a, otherId);
        assertNotEquals(a, null);
        assertNotEquals(a, "not a product");
    }

    @Test
    void productsWithoutIdsCompareEqualBecauseBothIdsAreNull() {
        Product a = new Product();
        Product b = new Product();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
