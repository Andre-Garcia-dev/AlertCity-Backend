package com.AlertCity_Backend.repository;

import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.Report;
import com.AlertCity_Backend.model.ReportState;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ReportSpecification {

    private ReportSpecification() {
    }

    /**
     * Construye una especificación JPA dinámica para filtrar reportes por múltiples criterios opcionales.
     */
    public static Specification<Report> filterBy(
            UUID userId,
            Category category,
            ReportState state,
            Boolean urgent,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("idUser"), userId));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (state != null) {
                predicates.add(cb.equal(root.get("state"), state));
            }
            if (urgent != null) {
                predicates.add(cb.equal(root.get("urgent"), urgent));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateTime"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateTime"), toDate));
            }

            if (query != null && Report.class.equals(query.getResultType()) && query.getOrderList().isEmpty()) {
                query.orderBy(cb.desc(root.get("dateTime")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
