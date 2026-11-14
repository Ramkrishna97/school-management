package com.schoolmanagement.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
class InventoryServiceTest {
    @Autowired InventoryService service;

    @Test
    void seededLowStockCreatesOneRequisitionPerItemAndReceiptReplenishesStock() {
        assertThat(service.items()).hasSize(8);
        assertThat(service.requisitions()).hasSize(4);

        service.monitorAll();
        assertThat(service.requisitions()).hasSize(4);

        PurchaseRequisition chalk = service.requisitions().stream()
            .filter(req -> req.getItemId().equals(1L)).findFirst().orElseThrow();
        VendorOrder order = service.placeOrder(chalk.getId(), "Test Vendor");
        service.receiveOrder(order.getId());

        assertThat(service.item(1L).getQuantity()).isGreaterThanOrEqualTo(20);
        assertThat(service.requisitions().stream().filter(req -> req.getId().equals(chalk.getId()))
            .findFirst().orElseThrow().getStatus()).isEqualTo(PurchaseRequisition.Status.FULFILLED);
    }
}
