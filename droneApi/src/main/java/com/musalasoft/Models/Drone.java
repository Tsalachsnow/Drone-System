package com.musalasoft.Models;

import com.musalasoft.enums.Model;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drone")
@Data
public class Drone {
    @Id
    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private Model model;

    @Max(value = 500, message = "weight limit exceeded")
    private int weightLimit;

    private int weightLimitLeft;

    @Max(value = 100, message = "cannot exceed 100%")
    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    private Status batteryStatus;

    @Enumerated(EnumType.STRING)
    private State state;

}
