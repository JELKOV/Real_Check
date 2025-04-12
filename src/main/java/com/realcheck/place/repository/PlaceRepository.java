package com.realcheck.place.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.realcheck.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByOwnerId(Long ownerId);
}
