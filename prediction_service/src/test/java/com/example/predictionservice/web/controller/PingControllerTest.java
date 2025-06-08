package com.example.predictionservice.web.controller;

import com.example.predictionservice.service.PingService;
import com.example.predictionservice.web.dto.PingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PingController.class)
public class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PingService pingService;

    @Test
    public void testPing() throws Exception {
        // Given
        PingResponse expectedResponse = new PingResponse("pong", System.currentTimeMillis(), "UP");
        when(pingService.ping()).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
} 