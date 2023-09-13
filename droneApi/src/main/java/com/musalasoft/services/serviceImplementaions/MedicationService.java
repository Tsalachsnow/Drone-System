package com.musalasoft.services.serviceImplementaions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.Models.DispatchLog;
import com.musalasoft.Models.Drone;
import com.musalasoft.Models.Medication;
import com.musalasoft.dto.*;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.exception.GenericException;
import com.musalasoft.services.Schedular.ReturnDroneSchedular;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.musalasoft.enums.State.LOADED;

@RequiredArgsConstructor
//@Transactional
@Slf4j
@Service
public class MedicationService {
    private final DroneServiceRepository droneServiceRepository;
    private final MedicationServiceRepository medicationServiceRepository;
    private final DispatchLogRepository dispatchRepo;
    private final DispatchLogRepository dispatchLogRepository;
    @Autowired
    private ReturnDroneSchedular returnDroneSchedular;

    public LoadMedicationResponse loadMedication(LoadMedicationRequest.Request request) {
        ObjectMapper obj = new ObjectMapper();
        try{
        Drone drone = droneServiceRepository.getById(request.getSerialNumber());
        LoadMedicationResponse response = new LoadMedicationResponse();
        int limit = 0;
        AtomicInteger total = new AtomicInteger();

        request.getMedications().forEach(medication -> {
            total.addAndGet(medication.getWeight());
        });

        limit = drone.getWeightLimitLeft();
        log.info("Drone weight limit left: {}", limit);

        log.info("Total weight of the medication is: " + total);
        if(limit - total.get() < 0){
            log.info("weight limit exceeded");
            response.setResponseMessage("weight limit exceeded with balance of " + (total.get() - limit));
            response.setResponseCode("E91");
            response.setSerialNumber(request.getSerialNumber());
        }else if(drone.getBatteryCapacity() < 25){
            log.info("Battery capacity is less than 25%");
            response.setResponseMessage("Battery capacity is less than 25% cannot load medication");
            response.setResponseCode("E92");
            response.setSerialNumber(request.getSerialNumber());
        }
        else if(drone.getBatteryStatus() == Status.CHARGING){
            log.info("Drone is charging");
            response.setResponseMessage("Drone is charging");
            response.setResponseCode("E93");
            response.setSerialNumber(request.getSerialNumber());
        }
        else if(drone.getState() == State.LOADING){
            log.info("Drone is loading");
            response.setResponseMessage("Drone is loading");
            response.setResponseCode("E94");
            response.setSerialNumber(request.getSerialNumber());
        }
        else if(drone.getState() == State.DELIVERING || drone.getState() == State.DELIVERED || drone.getState() == State.RETURNING){
            log.info("Drone is yet to return");
            response.setResponseMessage("Drone is yet to return");
            response.setResponseCode("E95");
            response.setSerialNumber(request.getSerialNumber());
        }
        else {
            log.info("loading medication");
            drone.setState(State.LOADING);
            droneServiceRepository.save(drone);
            try{
                int finalLimit = limit - total.get();
                log.info("finalLimit :: "+ finalLimit);
                request.getMedications().forEach(medication -> {
                    Medication med = obj.convertValue(medication, Medication.class);
                    drone.setState(LOADED);
                    drone.setWeightLimitLeft(finalLimit);
                    med.setStatus(State.LOADED);
                    med.setDrone(drone);
                    medicationServiceRepository.save(med);
                });

                droneServiceRepository.save(drone);
                response.setResponseMessage("medication loaded successfully");
                response.setResponseCode("000");
                response.setSerialNumber(request.getSerialNumber());
                log.info("medication loaded successfully");
            }
            catch (Exception e) {
                log.info("Error Cause :: "+ e.getMessage());
                response.setResponseMessage("error loading medication");
                response.setResponseCode("E92");
                response.setSerialNumber(request.getSerialNumber());
            }
        }
        log.info("Medication response: " + response);
        return response;
        }
        catch (Exception e) {
            log.info("Error Cause :: {}",Arrays.toString(e.getStackTrace()));
            throw new GenericException("E91", e.getMessage(), null);
        }
    }

    public FetchAllLoadedMedicationResponse getLoadedMedications(String serialNumber) {
        log.info("serial number: " + serialNumber);
        FetchAllLoadedMedicationResponse response = new FetchAllLoadedMedicationResponse();
        try{
        List<Medication> meds = medicationServiceRepository.findAllByDrone_SerialNumberAndStatus(serialNumber, LOADED);
        log.info("medications: " + Arrays.asList(meds));
        if(meds.isEmpty()) {
            response.setResponseCode("E93");
            response.setResponseMessage("no medication loaded on this drone");
        }else{
            response.setResponseCode("000");
            response.setResponseMessage("medications loaded on this drone");
        }
        response.setMedicationList(meds);
        log.info("loaded medications response: " + response);
        return response;
        }
        catch (Exception e) {
            log.info("Error Cause :: {}",Arrays.toString(e.getStackTrace()));
            throw new GenericException("E91", e.getMessage(), null);
        }
    }

    public DeliveryStatusResponse getDeliveryStatus(String dispatchNumber){
        DeliveryStatusResponse response = new DeliveryStatusResponse();
        try{
        Optional<DispatchLog> dispatch = dispatchLogRepository.findByDispatchNumber(dispatchNumber+ "D");
        if(dispatch.isPresent()){
            response.setResponseCode("000");
            response.setResponseMessage("Medications Delivered Successfully");
            response.setDeliveryStatus(dispatch.get().getDeliveryStatus());
            response.setExpectedDeliveryTime(dispatch.get().getExpectedDeliveryTime());
        }else{
            Optional<DispatchLog> dispatch1 = dispatchLogRepository.findByDispatchNumber(dispatchNumber);
            response.setResponseCode("000");
            response.setResponseMessage("Medications Dispatched");
            response.setDeliveryStatus(dispatch1.get().getDeliveryStatus());
            response.setExpectedDeliveryTime(dispatch1.get().getExpectedDeliveryTime());
        }

        return response;
        }
        catch (Exception e) {
            log.info("Error Cause :: {}",Arrays.toString(e.getStackTrace()));
            throw new GenericException("E91", e.getMessage(), null);
        }
    }

    public ConfirmationResponse confirmDelivery(DeliveryConfirmationRequest request){
        ConfirmationResponse response = new ConfirmationResponse();
        DispatchLog logger = new DispatchLog();
        LocalTime actualDeliveryTime = request.getTimeOfDelivery();
        try{
            List<Medication> list = medicationServiceRepository.findAllByDrone_SerialNumberAndStatus(request.getSerialNumber(), State.DELIVERED);
            Drone drone = droneServiceRepository.getById(request.getSerialNumber());
            Optional<DispatchLog> dispatchedItem = dispatchRepo.findByDispatchNumber(request.getDispatchNumber());
            if(drone.getState().equals(State.DELIVERED)){
                log.info("Delivery Successful, Return initiated");
                if(dispatchedItem != null ){
                    LocalTime dispatchTime = dispatchedItem.get().getExpectedDeliveryTime();
                    long hours = dispatchTime.until(actualDeliveryTime, ChronoUnit.HOURS);
                    long minutes = dispatchTime.until(actualDeliveryTime, ChronoUnit.MINUTES);
                    long seconds = dispatchTime.until(actualDeliveryTime, ChronoUnit.SECONDS);

                    LocalTime now = LocalTime.now();
                    LocalTime timeNow = LocalTime.of(now.getHour(), now.getMinute(), now.getSecond());
                    LocalTime returnTime = timeNow.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                    if (drone.getState() != State.DELIVERED) {
                        response.setResponseCode("E95");
                        response.setResponseMessage("Drone Return cannot be initiated at this stage");
                        response.setSerialNumber(drone.getSerialNumber());
                        return response;
                    }else if(timeNow.isAfter(returnTime)){
                        response.setResponseCode("E98");
                        response.setResponseMessage("Expected Arrival Time is in the past");
                        response.setSerialNumber(drone.getSerialNumber());
                        response.setExpectedReturnTime(returnTime);
                        return response;
                    }
                    drone.setState(State.RETURNING);
                    droneServiceRepository.save(drone);
                    response.setResponseCode("000");
                    response.setResponseMessage("Items Received, request for drone return with serial number :: "
                            +request.getSerialNumber()+" initiated");
                    response.setExpectedReturnTime(returnTime);
                    response.setSerialNumber(request.getSerialNumber());
                    List<LoadMedicationRequest.Medication1> medList = new ArrayList<>();
                    list.forEach(x->{
                        LoadMedicationRequest.Medication1 med = new LoadMedicationRequest.Medication1();
                        med.setCode(x.getCode());
                        med.setImage(x.getImage());
                        med.setName(x.getName());
                        med.setWeight(x.getWeight());
                        medList.add(med);
                    });
                    response.setReceivedMedications(medList);
                    CompletableFuture.supplyAsync(() ->{
                        returnDroneSchedular.returnDrone(drone, returnTime);
                        log.info("current time: {}", response);
                        return response;
                    }).thenApply(DispatchResponse -> {
                        while(timeNow.isBefore(returnTime)){
                            returnDroneSchedular.returnDrone(drone, returnTime);
                        }
                        return response;
                    });
                }
            }
            logger.setDroneModel(drone.getModel());
            logger.setSerialNumber(drone.getSerialNumber());
            logger.setTimestamp(new Timestamp(System.currentTimeMillis()));
            assert dispatchedItem != null;
            logger.setExpectedDeliveryTime(dispatchedItem.get().getExpectedDeliveryTime());
            logger.setExpectedReturnTime(response.getExpectedReturnTime());
            logger.setDeliveryStatus(State.DELIVERED);
            logger.setDispatchNumber(request.getDispatchNumber() + "D");
            logger.setMedicationTotalWeight(0);
            dispatchRepo.save(logger);
            return response;
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error occurred while Confirming Delivery: {}", e.getMessage());

            throw new GenericException("E91", e.getMessage(), null);
        }
    }
}
