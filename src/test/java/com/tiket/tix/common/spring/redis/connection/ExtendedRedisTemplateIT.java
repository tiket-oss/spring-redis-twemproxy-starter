package com.tiket.tix.common.spring.redis.connection;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author zakyalvan
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExtendedRedisTemplateIT {
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Test
    public void givenRestTemplate_whenSettingAndGettingOneValue_thenMustConsistent() {
        final String value = RandomStringUtils.randomAlphanumeric(200);
        String output = redisTemplate.execute((RedisConnection connection) -> {
            String key = UUID.randomUUID().toString();

            connection.set(key.getBytes(), value.getBytes());
            String stored = new String(connection.get(key.getBytes()));
            connection.expire(key.getBytes(), 0);
            return stored;
        });
        assertThat(output, equalTo(value));
    }

    @Test
    public void givenRestTemplate_whenSettingAndGettingValueStream_thenMustAlwaysConsistent() throws Exception {
        final Set<TestKeyValue> keyValues = new HashSet<>();
        final TestSubscriber<TestKeyValue> testSubscriber = new TestSubscriber<>();
        final CountDownLatch concurrentLatch = new CountDownLatch(1);

        Flowable.interval(5, TimeUnit.MILLISECONDS)
                .map(tick -> TestKeyValue.builder().value(RandomStringUtils.randomAlphabetic(100)).build())
                // Side effect for testing consistencies.
                .doOnNext(keyValue -> keyValues.add(keyValue))
                .flatMap(keyValue -> {
                    return Flowable.fromCallable(() -> redisTemplate.execute((RedisConnection connection) -> {
                        final byte[] keyBytes = keyValue.getKey().toString().getBytes();
                        final byte[] valueBytes = keyValue.getValue().getBytes();

                        connection.set(keyBytes, valueBytes);
                        return keyValue;
                    })).subscribeOn(Schedulers.io());
                })
                .take(10_000)
                .doOnComplete(() -> concurrentLatch.countDown())
                .subscribe(testSubscriber);

        testSubscriber.assertSubscribed();
        concurrentLatch.await();

        await().timeout(10, TimeUnit.MINUTES)
                .until(() -> testSubscriber.isTerminated(), is(true));
    }

    @Getter
    public static class TestKeyValue implements Serializable {
        private final UUID key;
        private final String value;

        @Builder
        protected TestKeyValue(UUID key, String value) {
            if (key == null) {
                this.key = UUID.randomUUID();
            } else {
                this.key = key;
            }

            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TestKeyValue that = (TestKeyValue) o;

            return new EqualsBuilder()
                    .append(key, that.key)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(key)
                    .toHashCode();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class AdditionalConfiguration {

    }
}
