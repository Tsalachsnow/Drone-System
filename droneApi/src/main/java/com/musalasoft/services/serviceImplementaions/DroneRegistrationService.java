package com.musalasoft.services.serviceImplementaions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.Models.Drone;
import com.musalasoft.dto.DroneRegistrationRequest;
import com.musalasoft.dto.DroneRegistrationResponse;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.exception.GenericException;
import com.musalasoft.services.respositories.DroneServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class DroneRegistrationService {
    private final DroneServiceRepository droneServiceRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    public DroneRegistrationResponse registerDrone(DroneRegistrationRequest request) {
        DroneRegistrationResponse response = new DroneRegistrationResponse();
        try {
            Drone drone = objectMapper.convertValue(request, Drone.class);
            drone.setWeightLimit(
                    request.getModel().name().equalsIgnoreCase("Lightweight")? 200:
                            request.getModel().name().equalsIgnoreCase("Middleweight")? 300:
                                    request.getModel().name().equalsIgnoreCase("Cruiserweight")? 400:500);
            drone.setState(State.IDLE);
            drone.setBatteryStatus(Status.NOT_CHARGING);
            drone.setWeightLimitLeft(drone.getWeightLimit());
            log.info("Drone registration request received: {}", drone);
            if(droneServiceRepository.findById(drone.getSerialNumber()).isPresent()){
                response.setSerialNumber(drone.getSerialNumber());
                response.setResponseCode("E21");
                response.setResponseMessage("Drone already registered");
            }else{
                if(drone.getBatteryCapacity() < 25) {
                    drone.setBatteryStatus(Status.LOW_BATTERY);
                }
            droneServiceRepository.save(drone);
            response.setResponseCode("000");
            response.setResponseMessage("Drone registered successfully");
            response.setSerialNumber(request.getSerialNumber());
            }
        } catch (Exception e) {
            log.info(Arrays.toString(e.getStackTrace()));
            response.setResponseCode("E91");
            response.setResponseMessage("Drone registration failed");
            throw new GenericException("E91", e.getMessage(), null);

        }
        log.info("Drone registration response: {}", response);
        return response;
    }


}
