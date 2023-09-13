package com.musalasoft.services.respositories;

import com.musalasoft.Models.Drone;
import com.musalasoft.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroneServiceRepository extends JpaRepository<Drone, String> {
  Optional<Drone> findById(String serialNumber);
  List<Drone> findAllByState(State state);
}

