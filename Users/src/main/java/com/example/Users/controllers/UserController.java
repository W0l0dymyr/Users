package com.example.Users.controllers;

import com.example.Users.models.User;
import com.example.Users.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {
        // Handling input data errors
        if (result.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.add(error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        // Checking user's age
        if (!userService.isUserOldEnough(user.getBirthDate())) {
            return ResponseEntity.badRequest().body("User must be at least " + userService.getMinUserAge() + " years old.");
        }

        // Creating the user
        if (!userService.createUser(user)) {
            return ResponseEntity.badRequest().body("Such email already exists");
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
    }

    @PatchMapping("/{email}")
    public ResponseEntity<?> updatePartialUser(@PathVariable String email, @RequestBody User user) {
        User existingUser = userService.getUsersByEmail().get(email);
        // If user with this email doesn't exist, return null
        if (existingUser == null) {
            return ResponseEntity.badRequest().body("User with this email not found");
        }

        // Checking email format and uniqueness
        if (user.getEmail() != null) {
            if (!userService.isValidEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Invalid email format");
            }
            if (userService.getUsersByEmail().containsKey(user.getEmail())) {
                return ResponseEntity.badRequest().body("Such email already exists");
            }
        }
        // Checking birth date validity and user's age
        if (user.getBirthDate() != null) {
            if (user.getBirthDate().isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body("Birth date cannot be in the future");
            }
            if (!userService.isUserOldEnough(user.getBirthDate())) {
                return ResponseEntity.badRequest().body("User must be at least " + userService.getMinUserAge() + " years old.");
            }
        }
        // Update the user
        User updatedUser = userService.updatePartialUser(email, user);
        return ResponseEntity.ok(updatedUser);
    }


    // PUT method to update all user fields
    @PutMapping("/{email}")
    public ResponseEntity<?> updateUser(@PathVariable String email, @Valid @RequestBody User user, BindingResult result) {
        // Handling input data errors
        if (result.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.add(error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        // Check if the email already exists
        if (user.getEmail().equals(email) || userService.getUsersByEmail().containsKey(user.getEmail())) {
            return ResponseEntity.badRequest().body("Such email already exists");
        }
        // Check user's age
        if (!userService.isUserOldEnough(user.getBirthDate())) {
            return ResponseEntity.badRequest().body("User must be at least " + userService.getMinUserAge() + " years old.");
        }
        // Update the user
        User updatedUser = userService.updateUser(email, user);
        if (updatedUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(updatedUser);
    }

    // DELETE method to delete a user
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        boolean deleted = userService.deleteUser(email);
        if (!deleted) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("User deleted successfully");
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchUsersByBirthDateRange(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("Invalid date range");
        }
        List<User> users = userService.getUsersByBirthDateRange(from, to);
        return ResponseEntity.ok(users);
    }

}
