package com.arrive.invoice_app.repositories;

import com.arrive.invoice_app.entities.Invoice;
import com.arrive.invoice_app.entities.InvoiceStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, InvoiceRepositoryCustom {

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCustomerEmail(String customerEmail);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerNameContainingIgnoreCase(String customerName);
}

interface InvoiceRepositoryCustom {
    List<Invoice> searchInvoices(String customerName, String status, String dateFrom, String dateTo);
}

@Repository
class InvoiceRepositoryCustomImpl implements InvoiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<Invoice> searchInvoices(String customerName, String status, String dateFrom, String dateTo) {
        StringBuilder sql = new StringBuilder("SELECT * FROM invoice WHERE 1=1");

        if (customerName != null && !customerName.isEmpty()) {
            sql.append(" AND customer_name LIKE '%" + customerName + "%'");
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = '" + status + "'");
        }

        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND created_at >= '" + dateFrom + "'");
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND created_at <= '" + dateTo + "'");
        }

        sql.append(" ORDER BY created_at DESC");

        return entityManager.createNativeQuery(sql.toString(), Invoice.class).getResultList();
    }
}
