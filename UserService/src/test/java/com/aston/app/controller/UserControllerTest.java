package com.aston.app.controller;

import com.aston.app.UserServiceApplication;
import com.aston.app.dto.UserDTO;
import com.aston.app.dto.UserResponseDTO;
import com.aston.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponseDTO userResponseDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setName("Test User");
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setAge(30);
        userResponseDTO.setCreatedAt(LocalDateTime.now());

        userDTO = new UserDTO();
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setAge(30);
    }

    @Test
    void testGetAllUsersReturningUsersList() throws Exception {
        List<UserResponseDTO> userList = new ArrayList<>();
        userList.add(userResponseDTO);
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test User"));
    }

    @Test
    void testCreateValidUserReturningCreatedUser() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testGetUserByIdReturningValidUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testUpdateByIdReturningUpdatedUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testDeleteByIdNoReturning() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetByEmailReturningValidUser() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userResponseDTO);

        mockMvc.perform(get("/users/email?email=test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

}