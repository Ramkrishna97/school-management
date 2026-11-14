package com.schoolmanagement.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class InventoryApi {
    private InventoryApi() {}
    public record CreateItem(@NotBlank String sku, @NotBlank String name,
                             @NotNull InventoryItem.Category category,
                             @Min(0) int quantity, @Min(1) int minimumQuantity,
                             @Min(1) int reorderQuantity,
                             @NotNull InventoryItem.Status status) {}
    public record AdjustStock(int delta) {}
    public record UpdateCondition(@NotNull InventoryItem.Status status) {}
    public record PlaceOrder(@NotBlank String vendorName) {}
}
