package com.luisramirez.booknest;

import static org.assertj.core.api.Assertions.assertThat;

import com.luisramirez.booknest.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BooknestApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
    }
}
