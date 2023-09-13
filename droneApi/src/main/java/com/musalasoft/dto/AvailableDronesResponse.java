package com.musalasoft.dto;

import com.musalasoft.Models.Drone;
import lombok.Data;

import java.util.List;

@Data
public class AvailableDronesResponse {
    private String responseCode;
    private String ResponseMessage;
    private List<Drone> drones;
}
