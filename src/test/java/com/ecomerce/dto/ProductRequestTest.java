package com.ecomerce.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProductRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static ProductRequest valid() {
        ProductRequest r = new ProductRequest();
        r.setName("Widget");
        r.setDescription("A widget");
        r.setBrand("Acme");
        r.setPrice(10.0);
        r.setInventory(5);
        r.setCategories(List.of("tools"));
        return r;
    }

    private static Set<String> invalidFields(ProductRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void gettersReturnWhatSettersStored() {
        ProductRequest r = valid();

        assertEquals("Widget", r.getName());
        assertEquals("A widget", r.getDescription());
        assertEquals("Acme", r.getBrand());
        assertEquals(10.0, r.getPrice());
        assertEquals(5, r.getInventory());
        assertEquals(List.of("tools"), r.getCategories());
    }

    @Test
    void aCompleteRequestIsValid() {
        assertTrue(invalidFields(valid()).isEmpty());
    }

    @Test
    void optionalFieldsMayBeOmittedAndZeroPriceIsAllowed() {
        ProductRequest r = new ProductRequest();
        r.setName("Only required");
        r.setPrice(0.0);
        r.setInventory(0);

        assertTrue(invalidFields(r).isEmpty());
    }

    @Test
    void blankNamesAreRejected() {
        ProductRequest r = valid();
        r.setName("   ");
        assertEquals(Set.of("name"), invalidFields(r));

        r.setName(null);
        assertEquals(Set.of("name"), invalidFields(r));
    }

    @Test
    void priceIsRequiredAndCannotBeNegative() {
        ProductRequest r = valid();
        r.setPrice(null);
        assertEquals(Set.of("price"), invalidFields(r));

        r.setPrice(-0.01);
        assertEquals(Set.of("price"), invalidFields(r));
    }

    @Test
    void inventoryIsRequired() {
        ProductRequest r = valid();
        r.setInventory(null);
        assertEquals(Set.of("inventory"), invalidFields(r));
    }
}
