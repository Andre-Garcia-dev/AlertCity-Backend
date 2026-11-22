package com.AlertCity_Backend.repository;

import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<List<ReportResponse>> getReportsByIdUser(String userId);
}
