package com.parking.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingLotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetParkingLots() throws Exception {
        mockMvc.perform(get("/parking-lots")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetParkingLotById() throws Exception {
        mockMvc.perform(get("/parking-lots/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetParkingSpaces() throws Exception {
        mockMvc.perform(get("/parking-lots/1/spaces")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}