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
@Table(name = "vendor_orders")
public class VendorOrder {
    public enum Status { PLACED, RECEIVED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long requisitionId;
    @Column(nullable = false)
    private String vendorName;
    @Column(nullable = false)
    private int quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PLACED;
    @Column(nullable = false)
    private Instant placedAt = Instant.now();
    private Instant receivedAt;

    protected VendorOrder() {}
    public VendorOrder(Long requisitionId, String vendorName, int quantity) {
        this.requisitionId = requisitionId;
        this.vendorName = vendorName;
        this.quantity = quantity;
    }
    public void receive() { status = Status.RECEIVED; receivedAt = Instant.now(); }
    public Long getId() { return id; }
    public Long getRequisitionId() { return requisitionId; }
    public String getVendorName() { return vendorName; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public Instant getPlacedAt() { return placedAt; }
    public Instant getReceivedAt() { return receivedAt; }
}
