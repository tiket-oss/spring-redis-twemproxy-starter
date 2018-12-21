package com.tiket.tix.common.spring.redis.connection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author zakyalvan
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AcquireRedisConnectionIT {
    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Test
    public void givenConnectionFactory_whenRealConnectionEstablished_thenPingSucceed() {
        RedisConnection connection = connectionFactory.getConnection();
        String pong = connection.ping();
        assertThat(pong, equalTo("PONG"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    public static class AdditionalConfiguration {

    }
}
