package com.red.yogaback;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest(classes = YogabackApplicationTests.class)
//@ActiveProfiles("test")
class YogabackApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(YogabackApplicationTests.class);

    @Test
    void contextLoads(){
        log.info("테스트 코드 시작");

    }

}
