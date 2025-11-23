package com.ecomerce.controller;

import com.ecomerce.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for `ProductController`.
 *
 * This test boots the full Spring context and uses `MockMvc` to exercise the
 * controller stack (serialization, validation, controller, service, repository).
 * It verifies end-to-end behavior for creating a product and then retrieving it
 * via the `Location` header returned by the create endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createThenGetById() throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Integration Product");
        req.setPrice(9.99);
        req.setInventory(5);

        String body = objectMapper.writeValueAsString(req);

        // Step 1: create - POST /products with a valid ProductRequest
        var result = mvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        // Capture the Location header returned by the controller (URI of new resource)
        String location = result.getResponse().getHeader("Location");

        // Step 2: retrieve - GET the created resource using the Location URI and
        // assert the returned JSON matches the original request
        mvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Integration Product"));
    }
}
