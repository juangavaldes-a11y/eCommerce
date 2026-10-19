package com.ecomerce.controller;

import com.ecomerce.dto.ProductRequest;
import com.ecomerce.repository.InMemoryProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests of the REST API (controller, validation, service, repository, cache, error handling).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InMemoryProductRepository repository;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void emptyStore() {
        repository.clear();
    }

    private static ProductRequest request(String name, String brand, double price, List<String> categories) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setBrand(brand);
        r.setPrice(price);
        r.setInventory(10);
        r.setCategories(categories);
        return r;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private long create(ProductRequest request) throws Exception {
        String body = mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Long.parseLong(body);
    }

    // ---- health -----------------------------------------------------------------------------

    @Test
    void healthEndpointReportsOk() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    // ---- create -----------------------------------------------------------------------------

    @Test
    void create_returns201WithLocationAndId() throws Exception {
        String body = mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request("Lamp", "Acme", 25.0, List.of("home")))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/products/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mvc.perform(get("/products/" + Long.parseLong(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lamp"))
                .andExpect(jsonPath("$.brand").value("Acme"))
                .andExpect(jsonPath("$.price").value(25.0))
                .andExpect(jsonPath("$.inventory").value(10))
                .andExpect(jsonPath("$.categories[0]").value("home"));
    }

    @Test
    void create_rejectsInvalidPayloads() throws Exception {
        // Validation failures and unparsable JSON are client errors (400), not server errors.
        String[] invalidBodies = {
                "{\"name\":\"\",\"price\":1.0,\"inventory\":1}",
                "{\"name\":\"No price\",\"inventory\":1}",
                "{\"name\":\"Negative\",\"price\":-1.0,\"inventory\":1}",
                "{\"name\":\"No inventory\",\"price\":1.0}",
                "{not valid json",
        };

        for (String invalid : invalidBodies) {
            mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        mvc.perform(get("/products")).andExpect(jsonPath("$", hasSize(0)));
    }

    // ---- read -------------------------------------------------------------------------------

    @Test
    void getById_returns404WithMessageWhenMissing() throws Exception {
        mvc.perform(get("/products/987654"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 987654"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getById_populatesTheProductsCache() throws Exception {
        long id = create(request("Cached", "Acme", 1.0, List.of()));
        assertNull(cacheManager.getCache("products").get(id));

        mvc.perform(get("/products/" + id)).andExpect(status().isOk());

        assertNotNull(cacheManager.getCache("products").get(id));
    }

    // ---- list -------------------------------------------------------------------------------

    @Test
    void list_appliesFiltersSortingAndPaging() throws Exception {
        create(request("Cheap shoe", "Nike", 20.0, List.of("shoes")));
        create(request("Pricey shoe", "Nike", 120.0, List.of("shoes")));
        create(request("Mid hat", "Nike", 40.0, List.of("hats")));
        create(request("Other shoe", "Adidas", 60.0, List.of("shoes")));

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mvc.perform(get("/products").param("brand", "nike").param("category", "SHOES").param("sort", "price"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Cheap shoe"))
                .andExpect(jsonPath("$[1].name").value("Pricey shoe"));

        mvc.perform(get("/products").param("priceMin", "30").param("priceMax", "100").param("sort", "name"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Mid hat"))
                .andExpect(jsonPath("$[1].name").value("Other shoe"));

        mvc.perform(get("/products").param("sort", "price").param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Other shoe"))
                .andExpect(jsonPath("$[1].name").value("Pricey shoe"));
    }

    // ---- update -----------------------------------------------------------------------------

    @Test
    void update_replacesTheProductAndEvictsTheCache() throws Exception {
        long id = create(request("Before", "Acme", 5.0, List.of("old")));
        mvc.perform(get("/products/" + id)).andExpect(jsonPath("$.name").value("Before"));

        mvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request("After", "NewBrand", 9.0, List.of("new")))))
                .andExpect(status().isNoContent());

        assertNull(cacheManager.getCache("products").get(id));
        mvc.perform(get("/products/" + id))
                .andExpect(jsonPath("$.name").value("After"))
                .andExpect(jsonPath("$.brand").value("NewBrand"))
                .andExpect(jsonPath("$.price").value(9.0));
    }

    @Test
    void update_returns404WhenMissing() throws Exception {
        mvc.perform(put("/products/424242")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request("Ghost", "Acme", 1.0, List.of()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 424242"));
    }

    @Test
    void update_rejectsInvalidPayloads() throws Exception {
        long id = create(request("Valid", "Acme", 5.0, List.of()));

        mvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":1.0,\"inventory\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")));

        mvc.perform(get("/products/" + id)).andExpect(jsonPath("$.name").value("Valid"));
    }

    // ---- delete -----------------------------------------------------------------------------

    @Test
    void delete_removesTheProductAndSubsequentReadsReturn404() throws Exception {
        long id = create(request("Doomed", "Acme", 5.0, List.of()));
        mvc.perform(get("/products/" + id)).andExpect(status().isOk());

        mvc.perform(delete("/products/" + id)).andExpect(status().isNoContent());

        mvc.perform(get("/products/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/products/" + id)).andExpect(status().isNotFound());
    }
}
