package com.realcheck.point.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realcheck.point.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByUserId(Long userId);
}
