package com.schoolmanagement.inventory;

import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final List<PurchaseRequisition.Status> ACTIVE =
        List.of(PurchaseRequisition.Status.OPEN, PurchaseRequisition.Status.ORDERED);

    private final InventoryItemRepository items;
    private final PurchaseRequisitionRepository requisitions;
    private final VendorOrderRepository orders;

    public InventoryService(InventoryItemRepository items, PurchaseRequisitionRepository requisitions,
                            VendorOrderRepository orders) {
        this.items = items;
        this.requisitions = requisitions;
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> items() { return items.findAll(); }

    @Transactional(readOnly = true)
    public InventoryItem item(Long id) {
        return items.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));
    }

    @Transactional
    public InventoryItem create(InventoryApi.CreateItem request) {
        String sku = request.sku().trim().toUpperCase(Locale.ROOT);
        if (items.existsBySkuIgnoreCase(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        InventoryItem item = items.save(new InventoryItem(sku, request.name().trim(),
            request.category(), request.quantity(), request.minimumQuantity(),
            request.reorderQuantity(), request.status()));
        checkStock(item);
        return item;
    }

    @Transactional
    public InventoryItem adjust(Long id, int delta) {
        InventoryItem item = item(id);
        item.changeStock(delta);
        checkStock(item);
        return item;
    }

    @Transactional
    public InventoryItem updateCondition(Long id, InventoryItem.Status status) {
        InventoryItem item = item(id);
        item.setCondition(status);
        return item;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @Transactional
    public void monitorAll() {
        for (InventoryItem item : items.findAll()) checkStock(item);
    }

    private void checkStock(InventoryItem item) {
        // Lock the item before checking for an active requisition to avoid duplicate alerts.
        item = items.lockById(item.getId()).orElseThrow();
        item.refreshStockStatus();
        if (item.needsReorder()) {
            if (!requisitions.existsByItemIdAndStatusIn(item.getId(), ACTIVE)) {
                int quantity = Math.max(item.getReorderQuantity(), item.getMinimumQuantity() - item.getQuantity());
                PurchaseRequisition requisition = requisitions.save(new PurchaseRequisition(item.getId(), quantity));
                log.warn("LOW_STOCK item={} quantity={} minimum={} requisition={}",
                    item.getSku(), item.getQuantity(), item.getMinimumQuantity(), requisition.getId());
            }
        } else {
            requisitions.findByItemIdAndStatusIn(item.getId(), List.of(PurchaseRequisition.Status.OPEN))
                .forEach(PurchaseRequisition::cancel);
        }
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequisition> requisitions() { return requisitions.findAll(); }

    @Transactional(readOnly = true)
    public List<VendorOrder> orders() { return orders.findAll(); }

    @Transactional
    public VendorOrder placeOrder(Long requisitionId, String vendorName) {
        PurchaseRequisition requisition = requisitions.findById(requisitionId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisition not found"));
        if (requisition.getStatus() != PurchaseRequisition.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requisition is not open");
        }
        VendorOrder order = orders.save(new VendorOrder(requisitionId, vendorName.trim(), requisition.getQuantity()));
        requisition.markOrdered();
        return order;
    }

    @Transactional
    public VendorOrder receiveOrder(Long orderId) {
        VendorOrder order = orders.findById(orderId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor order not found"));
        if (order.getStatus() != VendorOrder.Status.PLACED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already received");
        }
        PurchaseRequisition requisition = requisitions.findById(order.getRequisitionId()).orElseThrow();
        InventoryItem item = item(requisition.getItemId());
        item.changeStock(order.getQuantity());
        order.receive();
        requisition.markFulfilled();
        checkStock(item);
        return order;
    }
}
