package com.tiket.tix.common.spring.redis.autoconfigure;

import com.tiket.tix.common.spring.redis.autoconfigure.ExtendedRedisProperties.ConnectionPool;
import com.tiket.tix.common.spring.redis.connection.ExtendedJedisConnectionFactory;
import com.tiket.tix.common.spring.redis.connection.FailOverCapableConnectionFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * An extended {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for extending
 * default redis auto-configuration, to enable connections fail-over.
 *
 * @author zakyalvan
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "tiket.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ExtendedRedisProperties.class)
public class ExtendedRedisAutoConfiguration {
    @Bean
    ExtendedConnectionFactoryBeanPostProcessor connectionFactoryBeanPostProcessor() {
        ExtendedConnectionFactoryBeanPostProcessor postProcessor = new ExtendedConnectionFactoryBeanPostProcessor();
        return postProcessor;
    }

    /**
     * Configure fail over capabilities related components.
     */
    @Configuration
    public static class FailOverCapabilityConfiguration {
        private final ExtendedRedisProperties properties;

        public FailOverCapabilityConfiguration(ExtendedRedisProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean
        FailOverCapableConnectionFactory redisConnectionFactory() {
            final Set<RedisConnectionFactory> connectionFactories = new HashSet<>();

            properties.getConnections().forEach((name, connection) -> {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                if (connection.getPool() != null) {
                    ConnectionPool connectionPool = connection.getPool();

                    poolConfig.setMaxTotal(connectionPool.getMaxActive());
                    poolConfig.setMinIdle(connectionPool.getMinIdle());
                    poolConfig.setMaxIdle(connectionPool.getMaxIdle());
                    poolConfig.setMaxWaitMillis(connectionPool.getMaxWait());

                    poolConfig.setTestOnBorrow(connectionPool.isTestOnBorrow());
                    poolConfig.setTestOnCreate(connectionPool.isTestOnCreate());
                    poolConfig.setTestOnReturn(connectionPool.isTestOnReturn());
                    poolConfig.setTestWhileIdle(connectionPool.isTestWhileIdle());
                }

                // Do not set client name for JedisConnectionFactory. Twemproxy currently does not support 'CLIENT' command.
                ExtendedJedisConnectionFactory factory = new ExtendedJedisConnectionFactory(poolConfig);
                factory.setHostName(connection.getHost());
                factory.setPort(connection.getPort());
                factory.setDatabase(connection.getDatabase());
                factory.setUseSsl(connection.isSsl());
                factory.setTimeout(connection.getTimeout());

                JedisShardInfo shardInfo = new JedisShardInfo(connection.getHost(), connection.getPort(), connection.getTimeout(), connection.isSsl());
                factory.setShardInfo(shardInfo);

                if (StringUtils.hasText(connection.getPassword())) {
                    factory.setPassword(connection.getPassword());
                }

                factory.setValidateConnection(properties.isValidateConnections());

                // Initialize connection factory.
                factory.afterPropertiesSet();

                connectionFactories.add(factory);
            });

            FailOverCapableConnectionFactory connectionFactory = new FailOverCapableConnectionFactory(connectionFactories);
            return connectionFactory;
        }
    }
}
