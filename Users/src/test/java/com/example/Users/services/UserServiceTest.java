package com.example.Users.services;

import com.example.Users.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @Autowired
    private UserService userService;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("example@example.com");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setBirthDate(LocalDate.of(2000, 3, 4));
    }

    @AfterEach
    public void cleanUp(){
        userService.getUsersByEmail().clear();
    }

    @Test
    void testCreateUser() {
        assertTrue(userService.createUser(user));
        assertFalse(userService.createUser(user));
    }

    @Test
    void isUserOldEnough() {
        userService.setMinUserAge(18);

        assertTrue(userService.isUserOldEnough(LocalDate.of(2000, 11, 2)));
        assertFalse(userService.isUserOldEnough(LocalDate.of(2020, 11, 2)));
    }

    @Test
    void updatePartialUser_updateFirstName() {

        userService.getUsersByEmail().put(user.getEmail(), user);

        User updatedUser = new User();
        updatedUser.setFirstName("NewFirstName");

        User resultUser = userService.updatePartialUser(user.getEmail(), updatedUser);

        assertEquals("NewFirstName", resultUser.getFirstName());
    }

    @Test
    void updatePartialUser_updateLastName() {
        userService.getUsersByEmail().put(user.getEmail(), user);

        User updatedUser = new User();
        updatedUser.setLastName("NewLastName");

        User resultUser = userService.updatePartialUser(user.getEmail(), updatedUser);

        assertEquals("NewLastName", resultUser.getLastName());
    }

    @Test
    void updatePartialUser_updateEmail() {
        userService.getUsersByEmail().put(user.getEmail(), user);

        User updatedUser = new User();
        updatedUser.setEmail("newemail@example.com");

        User resultUser = userService.updatePartialUser(user.getEmail(), updatedUser);

        assertEquals("newemail@example.com", resultUser.getEmail());
    }

    @Test
    void updatePartialUser_updateBirthDate() {
        userService.getUsersByEmail().put(user.getEmail(), user);

        LocalDate newBirthDate = LocalDate.of(1990, 1, 1);
        User updatedUser = new User();
        updatedUser.setBirthDate(newBirthDate);

        User resultUser = userService.updatePartialUser(user.getEmail(), updatedUser);

        assertEquals(newBirthDate, resultUser.getBirthDate());
    }


    @Test
    void updateUser_ifUserDoesNotExists() {
        User updatedUser = new User("newemail@gmail.com", "NewFirstName", "NewLastName"
                , LocalDate.of(1999, 1, 30));

        User resultUser = userService.updateUser("someemail@gmail.com", updatedUser);

        assertNull(resultUser);

    }

    @Test
    void updateUser() {
        userService.getUsersByEmail().put(user.getEmail(), user);

        User updatedUser = new User("newemail@gmail.com", "NewFirstName", "NewLastName"
                , LocalDate.of(1999, 1, 30));

        User resultUser = userService.updateUser(user.getEmail(), updatedUser);

        assertEquals("newemail@gmail.com", resultUser.getEmail());
        assertEquals("NewFirstName", resultUser.getFirstName());
        assertEquals("NewLastName", resultUser.getLastName());
        assertEquals(user.getBirthDate(), resultUser.getBirthDate());
    }

    @Test
    void deleteUser() {
        userService.getUsersByEmail().put(user.getEmail(), user);

        assertTrue(userService.deleteUser(user.getEmail()));
        assertFalse(userService.deleteUser(user.getEmail()));
    }

    @Test
    void getUsersByBirthDateRange() {

        User user1 = new User("otheremail@gmai.com", "Bob", "Bobenko", LocalDate.of(1999, 10, 31));
        User user2 = new User("someemail@gmai.com", "Ron", "Ronchuk", LocalDate.of(2001, 10, 31));

        userService.getUsersByEmail().put(user.getEmail(), user);
        userService.getUsersByEmail().put(user1.getEmail(), user1);
        userService.getUsersByEmail().put(user2.getEmail(), user2);

        List<User> userList = userService.getUsersByBirthDateRange(LocalDate.of(1999, 12, 8), LocalDate.of(2001, 6, 22));

        assertEquals(1, userList.size());
    }

    @Test
    void isValidEmail() {
        // Перевірка коректних email
        assertTrue(userService.isValidEmail("test@example.com"));
        assertTrue(userService.isValidEmail("john.doe@example.co.uk"));
        assertTrue(userService.isValidEmail("user1234@example.com.ua"));

        assertFalse(userService.isValidEmail("not_an_email"));
        assertFalse(userService.isValidEmail("user@example"));
        assertFalse(userService.isValidEmail("user@.com"));
        assertFalse(userService.isValidEmail("user@example..com"));
        assertFalse(userService.isValidEmail("@example.com"));
        assertFalse(userService.isValidEmail("user.example.com"));
    }

}