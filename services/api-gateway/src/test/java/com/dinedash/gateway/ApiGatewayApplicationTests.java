package com.dinedash.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "dinedash.jwt.secret=ZGluZWRhc2gtdGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2g=")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
