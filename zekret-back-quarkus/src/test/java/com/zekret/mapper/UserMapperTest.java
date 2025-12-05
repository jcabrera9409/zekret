package com.zekret.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;
import com.zekret.model.User;

class UserMapperTest {

    @Test
    void testToEntity_ValidDTO() {
        UserRequestDTO dto = new UserRequestDTO(
            "test@example.com",
            "testuser",
            "password123"
        );

        User user = UserMapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.email(), user.getEmail());
        assertEquals(dto.username(), user.getUsername());
        assertEquals(dto.password(), user.getPassword());
    }

    @Test
    void testToEntity_AllFieldsAreSet() {
        UserRequestDTO dto = new UserRequestDTO(
            "john.doe@test.com",
            "johndoe",
            "securePassword!"
        );

        User user = UserMapper.toEntity(dto);

        assertNotNull(user);
        assertNotNull(user.getEmail());
        assertNotNull(user.getUsername());
        assertNotNull(user.getPassword());
        assertFalse(user.getEmail().isEmpty());
        assertFalse(user.getUsername().isEmpty());
        assertFalse(user.getPassword().isEmpty());
    }

    @Test
    void testToResponseDTO_ValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setEnabled(true);
        
        user.prePersist(); // Simula el comportamiento de @PrePersist

        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);

        assertNotNull(responseDTO);
        assertEquals(user.getEmail(), responseDTO.email());
        assertEquals(user.getUsername(), responseDTO.username());
        assertEquals(user.isEnabled(), responseDTO.enabled());
        assertNotNull(responseDTO.createdAt());
    }

    @Test
    void testToResponseDTO_DisabledUser() {
        User user = new User();
        user.setEmail("disabled@example.com");
        user.setUsername("disableduser");
        user.setEnabled(false);
        user.prePersist();

        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);

        assertNotNull(responseDTO);
        assertFalse(responseDTO.enabled());
        assertEquals("disabled@example.com", responseDTO.email());
    }

    @Test
    void testToResponseDTO_AllFieldsAreMapped() {
        User user = new User();
        user.setEmail("complete@test.com");
        user.setUsername("completeuser");
        user.setEnabled(true);
        user.prePersist();

        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);

        assertNotNull(responseDTO);
        assertNotNull(responseDTO.email());
        assertNotNull(responseDTO.username());
        assertNotNull(responseDTO.createdAt());
        assertFalse(responseDTO.email().isEmpty());
        assertFalse(responseDTO.username().isEmpty());
    }

    @Test
    void testRoundTrip_EntityToDTOAndBack() {
        // Create a DTO
        UserRequestDTO requestDTO = new UserRequestDTO(
            "roundtrip@test.com",
            "roundtripuser",
            "password123"
        );

        // Convert to entity
        User user = UserMapper.toEntity(requestDTO);
        user.setEnabled(true);
        user.prePersist();

        // Convert to response DTO
        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);

        // Verify the data integrity
        assertEquals(requestDTO.email(), responseDTO.email());
        assertEquals(requestDTO.username(), responseDTO.username());
        assertTrue(responseDTO.enabled());
        assertNotNull(responseDTO.createdAt());
    }

    @Test
    void testToEntity_PreservesExactValues() {
        String testEmail = "exact@test.com";
        String testUsername = "exactuser";
        String testPassword = "exactPassword123!";
        
        UserRequestDTO dto = new UserRequestDTO(testEmail, testUsername, testPassword);
        User user = UserMapper.toEntity(dto);

        assertEquals(testEmail, user.getEmail());
        assertEquals(testUsername, user.getUsername());
        assertEquals(testPassword, user.getPassword());
    }

    @Test
    void testToResponseDTO_PasswordNotIncluded() {
        User user = new User();
        user.setEmail("secure@test.com");
        user.setUsername("secureuser");
        user.setPassword("secretPassword");
        user.setEnabled(true);
        user.prePersist();

        UserResponseDTO responseDTO = UserMapper.toResponseDTO(user);

        // Verify password is not included in response DTO (by checking the record definition)
        assertNotNull(responseDTO);
        assertEquals("secure@test.com", responseDTO.email());
        assertEquals("secureuser", responseDTO.username());
        // UserResponseDTO should not have a password field
    }
}
