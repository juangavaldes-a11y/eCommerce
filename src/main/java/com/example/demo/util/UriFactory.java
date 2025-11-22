package com.example.demo.util;

import java.net.URI;
import java.util.Objects;
import org.springframework.lang.NonNull;

/**
 * Helper for building common application URIs in one place.
 */
public final class UriFactory {
    private UriFactory() {}

    /**
     * Build the Location URI for a product resource with the given id.
     * The method is annotated as {@link NonNull} so callers and static analysis
     * tools can treat the returned value as non-null.
     *
     * @param id product id
     * @return non-null URI pointing to /products/{id}
     */
    public static @NonNull URI productLocation(long id) {
        return Objects.requireNonNull(URI.create("/products/" + id));
    }
}
