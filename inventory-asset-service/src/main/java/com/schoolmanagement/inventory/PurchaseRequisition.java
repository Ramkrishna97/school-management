package com.schoolmanagement.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "purchase_requisitions")
public class PurchaseRequisition {
    public enum Status { OPEN, ORDERED, FULFILLED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long itemId;
    @Column(nullable = false)
    private int quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.OPEN;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected PurchaseRequisition() {}
    public PurchaseRequisition(Long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
    public void markOrdered() { status = Status.ORDERED; }
    public void markFulfilled() { status = Status.FULFILLED; }
    public void cancel() { status = Status.CANCELLED; }
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
