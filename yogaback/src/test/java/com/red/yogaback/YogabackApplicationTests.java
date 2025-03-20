package com.red.yogaback;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource("classpath:application-test.yml")
@SpringBootTest
class YogabackApplicationTests {

    @Test
    void contextLoads(){

    }

}
