package com.arrive.invoice_app.repositories;

import com.arrive.invoice_app.entities.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LineItem entity.
 */
@Repository
public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    List<LineItem> findByInvoiceId(Long invoiceId);
}
