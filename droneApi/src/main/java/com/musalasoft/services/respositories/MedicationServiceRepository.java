package com.musalasoft.services.respositories;

import com.musalasoft.Models.Drone;
import com.musalasoft.Models.Medication;
import com.musalasoft.enums.State;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicationServiceRepository extends JpaRepository<Medication, Long> {
     Medication findByDrone_SerialNumber(String serialNumber);
//    @Query("SELECT m FROM Medication m WHERE m.drone.serialNumber = ?1 AND WHERE m.status = ?1")
    List<Medication> findAllByDrone_SerialNumberAndStatus(String serialNumber, State status);
}