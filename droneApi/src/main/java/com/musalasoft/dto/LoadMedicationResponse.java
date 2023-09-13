package com.musalasoft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoadMedicationResponse {
    private String responseCode;
    private String responseMessage;
    @JsonProperty("loaded_drones_serial_number")
    private String serialNumber;

}
