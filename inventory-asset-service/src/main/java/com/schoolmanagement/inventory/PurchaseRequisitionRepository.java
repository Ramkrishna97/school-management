package com.schoolmanagement.inventory;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {
    boolean existsByItemIdAndStatusIn(Long itemId, Collection<PurchaseRequisition.Status> statuses);
    List<PurchaseRequisition> findByItemIdAndStatusIn(Long itemId, Collection<PurchaseRequisition.Status> statuses);
}
