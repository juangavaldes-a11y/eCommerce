package main.java.com.ecomerce.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a product in the catalog.
 *
 * This is a simple POJO used by the in-memory repository and service layer.
 */
public class Product {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private double price;
    private int inventory;
    private List<String> categories;
    private Instant createdAt;

    public Product() {}

    public Product(Long id, String name, String description, String brand, double price, int inventory, List<String> categories, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.categories = categories;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getInventory() { return inventory; }
    public void setInventory(int inventory) { this.inventory = inventory; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
