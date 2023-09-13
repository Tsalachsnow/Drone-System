package com.musalasoft.dto;

import com.musalasoft.Models.Medication;
import lombok.Data;

import java.util.List;

@Data
public class FetchAllLoadedMedicationResponse {
    private String responseCode;
    private String responseMessage;
    private List<Medication> medicationList;
}
