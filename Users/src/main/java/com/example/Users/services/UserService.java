package com.example.Users.services;

import com.example.Users.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Value("${min.user.age}")
    private int minUserAge;
    private final Map<String, User> usersByEmail = new HashMap<>();

    // Method to create a new user
    public boolean createUser(User user) {
        if (usersByEmail.containsKey(user.getEmail())) {
            return false;
        }
        usersByEmail.put(user.getEmail(), user);
        return true;
    }

    public boolean isUserOldEnough(LocalDate birthDate) {
        return birthDate.isBefore(LocalDate.now().minusYears(getMinUserAge()));
    }

    // Method to update some fields of a user
    public User updatePartialUser(String email, User updatedUser) {
        User existingUser = usersByEmail.get(email);

        if (updatedUser.getEmail() != null) {
            // Updates the user's email
            updateEmail(email, updatedUser.getEmail(), existingUser);
        }
        // Updates other fields of the user
        if (updatedUser.getFirstName() != null && updatedUser.getFirstName().length() > 0) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null && updatedUser.getLastName().length() > 0) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getBirthDate() != null) {
            existingUser.setBirthDate(updatedUser.getBirthDate());
        }

        return existingUser;
    }

    // Method to fully update user information
    public User updateUser(String email, User updatedUser) {
        User existingUser = usersByEmail.get(email);
        if (existingUser == null) {
            return null;
        }

        // Updates the user's email
        updateEmail(email, updatedUser.getEmail(), existingUser);
        // Updates other fields of the user
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setBirthDate(updatedUser.getBirthDate());

        return existingUser;
    }

    // Method to delete a user
    public boolean deleteUser(String email) {
        if (usersByEmail.containsKey(email)) {
            usersByEmail.remove(email);
            return true; // User successfully deleted
        }
        return false; // Return false if user not found
    }

    // Method to get a list of users in a given birth date range
    public List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to) {
        return usersByEmail.values().stream().filter(user -> user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to)).collect(Collectors.toList());
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public int getMinUserAge() {
        return minUserAge;
    }

    public Map<String, User> getUsersByEmail() {
        return usersByEmail;
    }

    private void updateEmail(String email, String newEmail, User existingUser) {
        usersByEmail.remove(email);
        existingUser.setEmail(newEmail);
        usersByEmail.put(newEmail, existingUser);
    }

    public void setMinUserAge(int minUserAge) {
        this.minUserAge = minUserAge;
    }
}