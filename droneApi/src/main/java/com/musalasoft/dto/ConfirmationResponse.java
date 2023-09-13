package com.musalasoft.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class ConfirmationResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
    private LocalTime expectedReturnTime;
    private List<LoadMedicationRequest.Medication1> receivedMedications;

}
