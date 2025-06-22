package com.aston.app.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.aston.app.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testSaveUser() {

        User user = new User(null, "Test User", "test@example.com", 30, LocalDateTime.now());

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull(); // Проверяем, что ID был присвоен
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testFindByEmail() {
        User user = new User(null, "Test User", "test@example.com", 30, LocalDateTime.now());
        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findByEmail("test@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Test User");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testDeleteUser() {
        User user = new User(null, "Test User", "test@example.com", 30, LocalDateTime.now());
        User userRepositorySave = userRepository.save(user);

        userRepository.delete(userRepositorySave);

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByEmail("test@example.com")).isNull();
    }

    @Test
    public void testFindAllUsers() {
        long initialCount = userRepository.count();

        User user1 = new User(null, "User 1", "user1@example.com", 25, LocalDateTime.now());
        User user2 = new User(null, "User 2", "user2@example.com", 35, LocalDateTime.now());
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize((int) (initialCount + 2));
        assertThat(users)
                .filteredOn(user -> user.getEmail().equals("user1@example.com") || user.getEmail().equals("user2@example.com"))
                .hasSize(2)
                .extracting("name")
                .contains("User 1", "User 2");
    }
}