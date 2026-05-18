package com.example.MpFitness.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.MpFitness.Services.ForgotPasswordService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForgotPasswordController.class)
@AutoConfigureMockMvc(addFilters = false)
class ForgotPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ForgotPasswordService forgotPasswordService;

    @Test
    void testForgotPasswordSendsEmail() throws Exception {
        doNothing().when(forgotPasswordService).requestPasswordReset("user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testForgotPasswordWithInvalidEmail() throws Exception {
        doThrow(new IllegalArgumentException("Email not found")).when(forgotPasswordService)
                .requestPasswordReset("no@x.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no@x.com\"}"))
                .andExpect(status().isNotFound());
    }
}
