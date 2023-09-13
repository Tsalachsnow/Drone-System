package com.musalasoft.controller;

import com.musalasoft.dto.*;
import com.musalasoft.services.serviceImplementaions.DroneService;
import com.musalasoft.services.serviceImplementaions.DroneRegistrationService;
import com.musalasoft.services.serviceImplementaions.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RequestMapping("/drone-api")
@RequiredArgsConstructor
@RestController
public class DroneMedicationController {
    private final DroneRegistrationService droneRegistrationService;
    private final DroneService droneService;
    private final MedicationService medicationService;


    @PostMapping("/registerDrone")
    public ResponseEntity<DroneRegistrationResponse> DroneRegistration(@Valid @RequestBody DroneRegistrationRequest request) {
        return ResponseEntity.ok().body(droneRegistrationService.registerDrone(request));
    }

    @GetMapping("/availableDrones")
    public ResponseEntity<AvailableDronesResponse> isDroneAvailable() {
        return ResponseEntity.ok().body(droneService.droneAvailable());
    }

    @PostMapping("/loadMedication")
    public ResponseEntity<LoadMedicationResponse> loadMedication(@Valid @RequestBody LoadMedicationRequest.Request request) {
        return ResponseEntity.ok().body(medicationService.loadMedication(request));
    }

    @GetMapping(path = "/loadedMedications/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FetchAllLoadedMedicationResponse> fetchAllLoadedMedications(@PathVariable String serialNumber) {
        return ResponseEntity.ok().body(medicationService.getLoadedMedications(serialNumber));
    }

    @GetMapping(path = "/batteryCapacity/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BatteryCapacityResponse> batteryCheck(@PathVariable String serialNumber) {
        return ResponseEntity.ok().body(droneService.batteryCapacity(serialNumber));
    }
    @GetMapping(path = "/fetchAllDrones", produces = MediaType.APPLICATION_JSON_VALUE)
    public AvailableDronesResponse getAllDrones() {
        return droneService.fetchAllDrones();
    }

    @PostMapping(path = "/chargeBattery", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChargeBatteryResponse> chargeBattery(@Valid @RequestBody GenericDroneRequest request) {
        return ResponseEntity.ok().body(droneService.chargeBattery(request));
    }


    @PostMapping(path = "/dispatchDrone", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DispatchResponse> dispatch(@Valid @RequestBody DispatchRequest request) {
        return ResponseEntity.ok().body(droneService.dispatchDrone(request));
    }

    @PostMapping(path = "/confirmDelivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfirmationResponse> dispatch(@Valid @RequestBody DeliveryConfirmationRequest request) {
        return ResponseEntity.ok().body(medicationService.confirmDelivery(request));
    }

    @GetMapping(path = "/getDeliveryStaus/{dispatchNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeliveryStatusResponse getDeliveryStatus(@PathVariable String dispatchNumber) {
        return medicationService.getDeliveryStatus(dispatchNumber);
    }

    @GetMapping(path = "/getDroneCurrentStatus/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DroneCurrentStatusResponse getDroneCurrentStatus(@PathVariable String serialNumber) {
        return droneService.getDroneCurrentStatus(serialNumber);
    }
}
