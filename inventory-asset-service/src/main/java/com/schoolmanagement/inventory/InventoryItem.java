package com.schoolmanagement.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    public enum Category { CONSUMABLE, LAB_EQUIPMENT, SPORTS_GEAR, IT_HARDWARE }
    public enum Status { OPERATIONAL, NEEDS_REPAIR, DAMAGED, LOW_STOCK }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String sku;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Category category;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private int minimumQuantity;
    @Column(nullable = false)
    private int reorderQuantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status;

    protected InventoryItem() {}

    public InventoryItem(String sku, String name, Category category, int quantity,
                         int minimumQuantity, int reorderQuantity, Status status) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.minimumQuantity = minimumQuantity;
        this.reorderQuantity = reorderQuantity;
        this.status = status;
        refreshStockStatus();
    }

    public void changeStock(int delta) {
        if ((long) quantity + delta < 0 || (long) quantity + delta > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Stock adjustment would produce an invalid quantity");
        }
        quantity += delta;
        refreshStockStatus();
    }

    public void setCondition(Status condition) {
        if (condition == Status.LOW_STOCK) throw new IllegalArgumentException("LOW_STOCK is calculated from quantity");
        status = condition;
        refreshStockStatus();
    }

    public void refreshStockStatus() {
        if (quantity < minimumQuantity && status == Status.OPERATIONAL) status = Status.LOW_STOCK;
        else if (quantity >= minimumQuantity && status == Status.LOW_STOCK) status = Status.OPERATIONAL;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public int getMinimumQuantity() { return minimumQuantity; }
    public int getReorderQuantity() { return reorderQuantity; }
    public Status getStatus() { return status; }
    public boolean needsReorder() { return quantity < minimumQuantity; }
}
