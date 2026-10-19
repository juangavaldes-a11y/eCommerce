package com.ecomerce;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DemoApplicationTest {

    @Test
    void mainStartsTheApplication() {
        // Random port and no banner so this can run alongside the other Spring tests.
        assertDoesNotThrow(() -> DemoApplication.main(new String[] {
                "--server.port=0",
                "--spring.main.banner-mode=off"
        }));
    }
}
