package com.musalasoft.services.serviceImplementaions;

import com.musalasoft.Models.DispatchLog;
import com.musalasoft.Models.Drone;
import com.musalasoft.Models.Medication;
import com.musalasoft.services.Schedular.ChargeBatterySchedular;
import com.musalasoft.services.Schedular.DeliverySchedular;
import com.musalasoft.services.Schedular.ReturnDroneSchedular;
import com.musalasoft.dto.*;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.exception.GenericException;
import com.musalasoft.exception.NoDataFoundException;
import com.musalasoft.services.respositories.DispatchLogRepository;
import com.musalasoft.services.respositories.DroneServiceRepository;
import com.musalasoft.services.respositories.MedicationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DroneService {
    private final DroneServiceRepository droneServiceRepository;
    private final DispatchLogRepository dispatchRepo;
    private final MedicationServiceRepository medication;
    @Autowired
    private ChargeBatterySchedular battery;
    @Autowired
    private DeliverySchedular dispatch;


    public AvailableDronesResponse droneAvailable(){
        AvailableDronesResponse availableDronesResponse = new AvailableDronesResponse();
        try {
            List<Drone> drones1 = droneServiceRepository.findAllByState(State.IDLE);
            availableDronesResponse.setResponseCode("000");
            availableDronesResponse.setResponseMessage("List of Available Drones Fetched Successfully");
            availableDronesResponse.setDrones(drones1);
            log.info("Available drone Response: {}", availableDronesResponse);

        }catch (Exception e){
            log.info(Arrays.toString(e.getStackTrace()));
            availableDronesResponse.setResponseCode("E91");
            availableDronesResponse.setResponseMessage("Failed to Fetch List Available Drones");
            throw new NoDataFoundException("No Data Found");
        }
        return availableDronesResponse;
    }

    public BatteryCapacityResponse batteryCapacity(String serialNumber){
        BatteryCapacityResponse batteryCapacityResponse = new BatteryCapacityResponse();
        try {
            Drone drone = droneServiceRepository.getById(serialNumber);
            if (drone != null) {
                batteryCapacityResponse.setResponseCode("000");
                batteryCapacityResponse.setResponseMessage("Battery Capacity Fetched Successfully");
                batteryCapacityResponse.setSerialNumber(serialNumber);
                batteryCapacityResponse.setBatteryCapacity(drone.getBatteryCapacity());
                batteryCapacityResponse.setBatteryStatus(drone.getBatteryStatus());
                log.info("Battery Capacity Response: {}", batteryCapacityResponse);
            }else {
                batteryCapacityResponse.setResponseCode("E92");
                batteryCapacityResponse.setResponseMessage("Drone Not Found");
            }
               }catch(Exception e){
                log.info(Arrays.toString(e.getStackTrace()));
                batteryCapacityResponse.setResponseCode("E91");
                batteryCapacityResponse.setResponseMessage("Failed to Fetch Battery Capacity");
                throw new GenericException("E91", e.getMessage(), null);
            }
            return batteryCapacityResponse;
        }

    public AvailableDronesResponse fetchAllDrones(){
        AvailableDronesResponse availableDronesResponse = new AvailableDronesResponse();
        try {
            List<Drone> drones1 = droneServiceRepository.findAll();
            availableDronesResponse.setResponseCode("000");
            availableDronesResponse.setResponseMessage("List of All Drones Fetched Successfully");
            availableDronesResponse.setDrones(drones1);
            log.info("Available drone Response: {}", availableDronesResponse);

        }catch (Exception e){
            log.info(Arrays.toString(e.getStackTrace()));
            availableDronesResponse.setResponseCode("E91");
            availableDronesResponse.setResponseMessage("Failed to Fetch All Drones");
            throw new GenericException("E91", e.getMessage(), null);
        }
        return availableDronesResponse;

}

    public ChargeBatteryResponse chargeBattery(GenericDroneRequest request){
        log.info("Charging initiated do not unplug the drone");
        ChargeBatteryResponse chargeBatteryResponse = new ChargeBatteryResponse();
        try{
        Drone drone = droneServiceRepository.getById(request.getSerialNumber());
        if(drone.getBatteryStatus().equals(Status.CHARGING)){
            chargeBatteryResponse.setResponseCode("E93");
            chargeBatteryResponse.setResponseMessage("Drone is already charging");
            chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
            chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
            return chargeBatteryResponse;
        }else if(drone.getBatteryStatus().equals(Status.FULLY_CHARGED)){
            chargeBatteryResponse.setResponseCode("E94");
            chargeBatteryResponse.setResponseMessage("Drone Fully Charged");
            chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
            chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
            return chargeBatteryResponse;
        }
        drone.setBatteryStatus(Status.CHARGING);
        chargeBatteryResponse.setResponseCode("000");
        chargeBatteryResponse.setResponseMessage("SUCCESS");
        chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
        chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
        droneServiceRepository.save(drone);
        CompletableFuture.supplyAsync(() ->{
            battery.charge(drone);
            return chargeBatteryResponse;
        }).thenApply(ChargeBatteryResponse -> {
            while(drone.getBatteryCapacity() < 100){
                battery.charge(drone);
            }
            drone.setBatteryStatus(Status.FULLY_CHARGED);
            droneServiceRepository.save(drone);
            chargeBatteryResponse.setResponseCode("000");
            chargeBatteryResponse.setResponseMessage("SUCCESS");
            chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
            chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
            log.info("Charge Battery Response: {}", chargeBatteryResponse);
            return chargeBatteryResponse;
        });
        return chargeBatteryResponse;
    }catch (Exception e) {
        e.printStackTrace();
        log.error("Error occurred while performing charge Battery Request: {}", e.getMessage());

        throw new GenericException("E91", e.getMessage(), null);
    }
}

    public DispatchResponse dispatchDrone(DispatchRequest request){
        String dispatchNo = UUID.randomUUID().toString().substring(0, 8);
        DispatchLog logger = new DispatchLog();
        DispatchResponse dispatchResponse = new DispatchResponse();
        try{
            Optional<DispatchLog> dispatchLog = dispatchRepo.findByDispatchNumber(request.getDispatchNumber());
            try{
            if(!dispatchLog.isPresent()){
        List<Medication> med = medication.findAllByDrone_SerialNumberAndStatus(request.getSerialNumber(), State.LOADED);
        Drone drone = droneServiceRepository.getById(request.getSerialNumber());

        LocalTime time1 = LocalTime.now();
        LocalTime time = LocalTime.of(time1.getHour(), time1.getMinute(), time1.getSecond());
        LocalTime expectedTime = LocalTime.parse(request.getExpectedArrivalTime());
        LocalTime dfgh = LocalTime.of(expectedTime.getHour(), expectedTime.getMinute(), expectedTime.getSecond());

        if (drone.getState().equals(State.DELIVERING)) {
            dispatchResponse.setResponseCode("E95");
            dispatchResponse.setResponseMessage("Drone is already delivering");
            dispatchResponse.setCurrentState(drone.getState());
            return dispatchResponse;
        }else if(drone.getState() != State.LOADED && drone.getState() != State.IDLE){
            dispatchResponse.setResponseCode("E96");
            dispatchResponse.setResponseMessage("Drone not available");
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }else if (drone.getState().equals(State.IDLE)){
            dispatchResponse.setResponseCode("E97");
            dispatchResponse.setResponseMessage("Drone is yet to be loaded");
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }else if(time.isAfter(dfgh)){
            dispatchResponse.setResponseCode("E98");
            dispatchResponse.setResponseMessage("Expected Arrival Time is in the past");
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }
            drone.setState(State.DELIVERING);
            droneServiceRepository.save(drone);
            dispatchResponse.setResponseCode("000");
            dispatchResponse.setResponseMessage("Drone with serial number "
                    + request.getSerialNumber() + " dispatched successfully");
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
        dispatchResponse.setExpectedDeliveryTime(dfgh);
        CompletableFuture.supplyAsync(() ->{
            dispatch.deliver(drone, request.getExpectedArrivalTime());
            log.info("current time: {}", dispatchResponse);
            return dispatchResponse;
        }).thenApply(DispatchResponse -> {
            while(time.isBefore(dfgh)){
                dispatch.deliver(drone, request.getExpectedArrivalTime());
                dispatchResponse.setCurrentState(State.DELIVERED);
            }
            return dispatchResponse;
        });
        med.forEach(x->{
            x.setStatus(State.DELIVERED);
            medication.save(x);
        });
        logger.setDroneModel(drone.getModel());
        logger.setSerialNumber(drone.getSerialNumber());
        logger.setTimestamp(new Timestamp(System.currentTimeMillis()));
        logger.setExpectedDeliveryTime(dfgh);
        logger.setDeliveryStatus(dispatchResponse.getCurrentState());
        logger.setDispatchNumber(dispatchNo);
        int totalWeight = drone.getWeightLimit() - drone.getWeightLimitLeft();
        logger.setMedicationTotalWeight(totalWeight == 0? drone.getWeightLimit():totalWeight);
        dispatchRepo.save(logger);
        dispatchResponse.setDispatchNumber(dispatchNo);
        return dispatchResponse;
            }
         }catch (Exception e) {
            } throw new GenericException("E99", "Invalid Dispatch Number or Dispatch Number already exists", null);

        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error occurred while performing Dispatch For Loaded Items: {}", e.getMessage());

            throw new GenericException("E91", e.getMessage(), null);
        }
    }

    public DroneCurrentStatusResponse getDroneCurrentStatus(String serialNumber) {
        DroneCurrentStatusResponse response = new DroneCurrentStatusResponse();
        try{
        Drone drone = droneServiceRepository.getById(serialNumber);
        response.setResponseCode("000");
        response.setResponseMessage("Drone Status Fetched Successfully");
        response.setBatteryCapacity(drone.getBatteryCapacity());
        response.setDroneCurrentState(drone.getState());
        response.setDroneModel(drone.getModel());
        response.setBatteryStatus(drone.getBatteryStatus());
        return response;
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error occurred while Getting Current Status for Drone: {}", e.getMessage());
            throw new NoDataFoundException("No Data Found");
        }
    }

}
