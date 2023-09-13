package com.musalasoft.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musalasoft.enums.State;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Table(name = "medication")
@Entity
@Data
public class Medication {
    @JsonIgnore
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp ="^[a-zA-Z0-9_-]*$")
    private String name;

    private int weight;

    @Pattern(regexp = "^[A-Z0-9_]*$")
    private String code;

    private String image;

    @Enumerated(EnumType.STRING)
    private State status;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="drone_serial_number", referencedColumnName = "serial_number", nullable = false)
    private Drone drone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
