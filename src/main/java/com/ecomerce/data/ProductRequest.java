package main.java.com.ecomerce.data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO used for creating or updating a Product via the REST API.
 * Validation annotations ensure required fields are present and valid.
 */
public class ProductRequest {
    @NotBlank
    private String name;
    private String description;
    private String brand;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double price;
    @NotNull
    private Integer inventory;
    private List<String> categories;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getInventory() { return inventory; }
    public void setInventory(Integer inventory) { this.inventory = inventory; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}
