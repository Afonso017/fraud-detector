package com.frauddetector.auditservice.repository;

import com.frauddetector.auditservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
