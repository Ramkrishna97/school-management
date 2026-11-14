package com.schoolmanagement.inventory;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) { this.service = service; }

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','STORE_MANAGER')")
    public List<InventoryItem> items() { return service.items(); }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','STORE_MANAGER')")
    public InventoryItem item(@PathVariable Long id) { return service.item(id); }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','STORE_MANAGER')")
    public InventoryItem create(@Valid @RequestBody InventoryApi.CreateItem request) {
        return service.create(request);
    }

    @PatchMapping("/items/{id}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','STORE_MANAGER')")
    public InventoryItem adjust(@PathVariable Long id, @RequestBody InventoryApi.AdjustStock request) {
        return service.adjust(id, request.delta());
    }

    @PatchMapping("/items/{id}/condition")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','STORE_MANAGER')")
    public InventoryItem condition(@PathVariable Long id,
                                   @Valid @RequestBody InventoryApi.UpdateCondition request) {
        return service.updateCondition(id, request.status());
    }

    @GetMapping("/requisitions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','STORE_MANAGER')")
    public List<PurchaseRequisition> requisitions() { return service.requisitions(); }

    @PostMapping("/requisitions/{id}/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','STORE_MANAGER')")
    public VendorOrder place(@PathVariable Long id, @Valid @RequestBody InventoryApi.PlaceOrder request) {
        return service.placeOrder(id, request.vendorName());
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','PRINCIPAL','STORE_MANAGER')")
    public List<VendorOrder> orders() { return service.orders(); }

    @PatchMapping("/orders/{id}/receive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','STORE_MANAGER')")
    public VendorOrder receive(@PathVariable Long id) { return service.receiveOrder(id); }
}
