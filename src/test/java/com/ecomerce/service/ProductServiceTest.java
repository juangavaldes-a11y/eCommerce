package com.ecomerce.service;

import com.ecomerce.data.ProductRequest;
import com.ecomerce.model.Product;
import com.ecomerce.repository.InMemoryProductRepository;
import com.ecomerce.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    InMemoryProductRepository repo;

    ProductService svc;

    @BeforeEach
    void setUp() {
        svc = new ProductService(repo);
    }

    @Test
    void create_assignsIdAndReturnsId() {
        ProductRequest req = new ProductRequest();
        req.setName("Widget");
        req.setPrice(12.5);
        req.setInventory(3);

        Product saved = new Product(1L, "Widget", "desc", "Brand", 12.5, 3, List.of("cat"), Instant.now());
        when(repo.save(any())).thenReturn(saved);

        Long id = svc.create(req);

        assertEquals(1L, id);
        verify(repo).save(any());
    }

    @Test
    void getById_found() {
        Product p = new Product(2L, "Gadget", "desc", "Acme", 5.0, 2, List.of(), Instant.now());
        when(repo.findById(2L)).thenReturn(Optional.of(p));

        Product got = svc.getById(2L);

        assertSame(p, got);
    }

    @Test
    void getById_notFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.getById(99L));
    }

    @Test
    void list_filtersAndPagination() {
        Product a = new Product(1L, "A", "", "BrandX", 5.0, 1, List.of("c1"), Instant.now());
        Product b = new Product(2L, "B", "", "BrandY", 15.0, 1, List.of("c2"), Instant.now());
        Product c = new Product(3L, "C", "", "BrandX", 10.0, 1, List.of("c1"), Instant.now());
        when(repo.findAll()).thenReturn(List.of(a, b, c));

        List<Product> res = svc.list("BrandX", null, null, null, "price", 0, 10);

        assertEquals(2, res.size());
        assertEquals(5.0, res.get(0).getPrice());
    }
}
