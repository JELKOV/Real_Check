package com.realcheck.request.repository;

import com.realcheck.request.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByIsClosedFalse();
}