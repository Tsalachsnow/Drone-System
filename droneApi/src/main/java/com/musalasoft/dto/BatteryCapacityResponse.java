package com.musalasoft.dto;

import com.musalasoft.enums.Status;
import lombok.Data;

@Data
public class BatteryCapacityResponse {
    private String responseCode;
    private String responseMessage;
    private String serialNumber;
    private int batteryCapacity;
    private Status batteryStatus;
}
