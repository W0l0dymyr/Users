package com.example.Users.controllers;


import com.example.Users.models.User;
import com.example.Users.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private User user;
    private User userToUpdate;

    @BeforeEach
    public void setUp() {
        userToUpdate = new User();

        user = new User();
        user.setEmail("example@example.com");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setBirthDate(LocalDate.of(2000, 3, 4));
    }


    @Test
    public void testCreateUser_ValidInput_Success() {

        BindingResult bindingResult = mock(BindingResult.class);

        // Mock userService behavior
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isUserOldEnough(user.getBirthDate())).thenReturn(true);
        when(userService.createUser(user)).thenReturn(true);

        // Call the method
        ResponseEntity<?> response = userController.createUser(user, bindingResult);

        // Assertions
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
        assertFalse(bindingResult.hasErrors()); // Assert no validation errors
    }

    @Test
    public void testCreateUser_bindingResultHasErrors() {

        BindingResult bindingResult = mock(BindingResult.class);

        // Mock userService behavior
        when(bindingResult.hasErrors()).thenReturn(true);

        // Call the method
        ResponseEntity<?> response = userController.createUser(user, bindingResult);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(bindingResult.hasErrors());

    }

    @Test
    public void testCreateUser_InvalidAge_BadRequest() {

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isUserOldEnough(user.getBirthDate())).thenReturn(false);

        // Call the method
        ResponseEntity<?> response = userController.createUser(user, bindingResult);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("User must be at least"));
    }

    @Test
    public void testCreateUser_ExistingEmail_BadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        when(userService.isUserOldEnough(user.getBirthDate())).thenReturn(true);
        when(userService.createUser(user)).thenReturn(false);

        // Call the method
        ResponseEntity<?> response = userController.createUser(user, bindingResult);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Such email already exists"));
    }


    @Test
    public void testUpdatePartialUser_UserNotFound() {
        String email = "test@example.com";
        User userToUpdate = new User();
        userToUpdate.setEmail("newemail@example.com");
        when(userService.getUsersByEmail()).thenReturn(new HashMap<>());

        ResponseEntity<?> response = userController.updatePartialUser(email, userToUpdate);

        assertEquals(ResponseEntity.badRequest().body("User with this email not found"), response);
    }

    @Test
    public void testUpdatePartialUser_InvalidEmailFormat() {
        userToUpdate.setEmail("invalidEmail");

        Map<String, User> map = new HashMap<>();
        map.put(user.getEmail(), user);

        when(userService.getUsersByEmail()).thenReturn(map);
        when(userService.isValidEmail(userToUpdate.getEmail())).thenReturn(false);

        ResponseEntity<?> response = userController.updatePartialUser(user.getEmail(), userToUpdate);

        assertEquals(ResponseEntity.badRequest().body("Invalid email format"), response);
    }

    @Test
    public void testUpdatePartialUser_ExistingEmail_BadRequest() {
        userToUpdate.setEmail(user.getEmail()); // Існуюча електронна пошта

        Map<String, User> usersByEmail = new HashMap<>();
        usersByEmail.put(user.getEmail(), user); // Користувач з оригінальною електронною поштою

        when(userService.getUsersByEmail()).thenReturn(usersByEmail);
        when(userService.isValidEmail(userToUpdate.getEmail())).thenReturn(true);
        System.out.println(userService.getUsersByEmail().containsKey(userToUpdate.getEmail()));
        ResponseEntity<?> response = userController.updatePartialUser(user.getEmail(), userToUpdate);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Such email already exists"));
    }

    @Test
    public void testUpdatePartialUser_FutureBirthDate() throws Exception {

        userToUpdate.setBirthDate(LocalDate.now().plusDays(1)); // Set birth date in the future

        // Mock userService behavior
        Map<String, User> mockUsers = new HashMap<>();
        mockUsers.put(user.getEmail(), user);
        when(userService.getUsersByEmail()).thenReturn(mockUsers);

        // Call the method
        ResponseEntity<?> response = userController.updatePartialUser(user.getEmail(), userToUpdate);
        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Birth date cannot be in the future"));
    }

    @Test
    public void testUpdatePartialUser_UserIsNotOldEnough() throws Exception {
        userToUpdate.setBirthDate(user.getBirthDate().plusYears(10)); // Set birth date in the future

        // Mock userService behavior
        Map<String, User> mockUsers = new HashMap<>();
        mockUsers.put(user.getEmail(), user);
        when(userService.getUsersByEmail()).thenReturn(mockUsers);
        when(userService.getMinUserAge()).thenReturn(18);
        // Call the method
        ResponseEntity<?> response = userController.updatePartialUser(user.getEmail(), userToUpdate);
        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("User must be at least "));
    }

    @Test
    public void testUpdatePartialUser_Success() throws Exception {

        userToUpdate.setFirstName("NewFirstName"); // Update only first name

        // Mock userService behavior
        Map<String, User> mockUsers = new HashMap<>();
        User existingUser = new User();
        existingUser.setFirstName(user.getFirstName());
        mockUsers.put(user.getEmail(), user);
        when(userService.getUsersByEmail()).thenReturn(mockUsers);
        when(userService.isValidEmail(userToUpdate.getEmail())).thenReturn(true); // Mock valid email format
        when(userService.isUserOldEnough(any(LocalDate.class))).thenReturn(true); // Mock user is old enough
        when(userService.updatePartialUser(user.getEmail(), userToUpdate)).thenReturn(existingUser); // Mock successful update

        // Call the method
        ResponseEntity<?> response = userController.updatePartialUser(user.getEmail(), userToUpdate);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(existingUser, response.getBody());
    }

    @Test
    public void testUpdateUser_ValidInput_Success() {

        BindingResult bindingResult = mock(BindingResult.class);

        userToUpdate.setFirstName("NewName");
        userToUpdate.setEmail("newemail@gmail.com");
        userToUpdate.setLastName("newLastname");
        userToUpdate.setBirthDate(LocalDate.of(1990, 12, 4));
        when(userService.getMinUserAge()).thenReturn(18);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isUserOldEnough(userToUpdate.getBirthDate())).thenReturn(true);
        when(userService.updateUser(user.getEmail(), userToUpdate)).thenReturn(userToUpdate);


        ResponseEntity<?> response = userController.updateUser(user.getEmail(), userToUpdate, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(userToUpdate, response.getBody());
        assertFalse(bindingResult.hasErrors()); // Немає помилок валідації
    }

    @Test
    public void testUpdateUser_UserNotFound() {

        BindingResult bindingResult = mock(BindingResult.class);

        userToUpdate.setFirstName("NewName");
        userToUpdate.setEmail("newemail@gmail.com");
        userToUpdate.setLastName("newLastname");
        userToUpdate.setBirthDate(LocalDate.of(1990, 12, 4));
        when(userService.getMinUserAge()).thenReturn(18);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isUserOldEnough(userToUpdate.getBirthDate())).thenReturn(true);
        when(userService.updateUser(user.getEmail(), userToUpdate)).thenReturn(null);


        ResponseEntity<?> response = userController.updateUser(user.getEmail(), userToUpdate, bindingResult);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        assertTrue(response.getBody().toString().contains("User not found"));
    }

    @Test
    public void testUpdateUser_UserIsNotOldEnough() {

        BindingResult bindingResult = mock(BindingResult.class);

        userToUpdate.setFirstName("NewName");
        userToUpdate.setEmail("newemail@gmail.com");
        userToUpdate.setLastName("newLastname");
        userToUpdate.setBirthDate(LocalDate.of(1990, 12, 4));
        when(userService.getMinUserAge()).thenReturn(18);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isUserOldEnough(userToUpdate.getBirthDate())).thenReturn(false);


        ResponseEntity<?> response = userController.updateUser(user.getEmail(), userToUpdate, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertTrue(response.getBody().toString().contains("User must be at least "));
    }

    @Test
    public void testUpdateUser_EmailAlreadyExists() {

        BindingResult bindingResult = mock(BindingResult.class);

        userToUpdate.setFirstName("NewName");
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setLastName("newLastname");
        userToUpdate.setBirthDate(LocalDate.of(1990, 12, 4));

        when(userService.getMinUserAge()).thenReturn(18);

        when(bindingResult.hasErrors()).thenReturn(false);


        ResponseEntity<?> response = userController.updateUser(user.getEmail(), userToUpdate, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertTrue(response.getBody().toString().contains("Such email already exists"));
    }

    @Test
    public void testUpdateUser_bindingResultHasErrors() {
        BindingResult bindingResult = mock(BindingResult.class);

        userToUpdate.setFirstName("NewName");
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setLastName("newLastname");
        userToUpdate.setBirthDate(LocalDate.of(1990, 12, 4));

        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<?> response = userController.updateUser(user.getEmail(), userToUpdate, bindingResult);

        // Assertions
        System.out.println(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(bindingResult.hasErrors());
    }

    @Test
    public void testDeleteUser_Success() {

        when(userService.deleteUser(user.getEmail())).thenReturn(true);

        ResponseEntity<?> response = userController.deleteUser(user.getEmail());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    public void testDeleteUser_UserNotFound() {


        when(userService.deleteUser(user.getEmail())).thenReturn(false);

        ResponseEntity<?> response = userController.deleteUser(user.getEmail());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    public void testSearchUsersByBirthDateRange_ValidDateRange_Success() throws Exception {

        // Sample birth dates
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2005, 12, 31);

        // Mock UserService behavior
        List<User> mockUsers = new ArrayList<>(); // Replace with your mock data
        when(userService.getUsersByBirthDateRange(fromDate, toDate)).thenReturn(mockUsers);

        // Call the method
        ResponseEntity<?> response = userController.searchUsersByBirthDateRange(fromDate.toString(), toDate.toString());

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUsers, response.getBody());
    }

    @Test
    public void testSearchUsersByBirthDateRange_InvalidDateRange_BadRequest() {

        // From date after To date
        LocalDate fromDate = LocalDate.of(2005, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 12, 31);

        // Call the method
        ResponseEntity<?> response = userController.searchUsersByBirthDateRange(fromDate.toString(), toDate.toString());

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid date range"));
    }
}