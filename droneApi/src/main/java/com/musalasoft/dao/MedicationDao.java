package com.musalasoft.dao;

import com.musalasoft.Models.Medication;
import com.musalasoft.dto.FetchAllLoadedMedicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Repository
public class MedicationDao {
    @Value("${spring.datasource.url}")
    private String connection;

    @Value("${spring.datasource.username}")
    private String username;
    @Value("${jdbc.driver}")
    private String JDBC_DRIVER;

    public FetchAllLoadedMedicationResponse fetchAllLoadedMedication(String serialNumber){
        FetchAllLoadedMedicationResponse response = new FetchAllLoadedMedicationResponse();
        Connection conn = null;
        Statement cSt = null;
//        ResultSet rs = null;
        try{
            Class.forName(JDBC_DRIVER);
            log.info("connection string: " + connection);
            log.info("password: " + username);
            log.info("JDBC_DRIVER: " + JDBC_DRIVER);
        conn = DriverManager.getConnection(connection, username, "");
        cSt = conn.createStatement();
           String serialNum = serialNumber;
            String  sqlQuery = "SELECT id, code, image, name, weight FROM Medication WHERE drone_serial_number = ?";
            PreparedStatement ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, serialNum);
            log.info("sqlQuery: " + ps);
            ResultSet rs = ps.executeQuery();

            List<Medication> medicationList1 = new ArrayList<>();
            while (rs.next()){
                   Medication medication = new Medication();
                    medication.setCode(rs.getString("code"));
                    medication.setImage(rs.getString("image"));
                    medication.setName(rs.getString("name"));
                    medication.setWeight(rs.getInt("weight"));
                    medicationList1.add(medication);
            }
            response.setResponseCode("000");
            response.setResponseMessage("SUCCESS");
            response.setMedicationList(medicationList1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
