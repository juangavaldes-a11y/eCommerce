package com.ecomerce.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UriFactoryTest {

    @Test
    void productLocationPointsAtTheProductResource() {
        URI uri = UriFactory.productLocation(17L);

        assertEquals("/products/17", uri.toString());
        assertFalse(uri.isAbsolute());
    }

    @Test
    void utilityClassCannotBeInstantiatedNormally() throws Exception {
        Constructor<UriFactory> constructor = UriFactory.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
