package com.tiket.tix.common.spring.redis.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

/**
 * Extension of {@link JedisConnectionFactory}, adding send 'AUTH' command on each retrieval of
 * {@link JedisConnection}.
 *
 * @author zakyalvan
 */
public class ExtendedJedisConnectionFactory extends JedisConnectionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedJedisConnectionFactory.class);

    /**
     * Flag determine whether send 'AUTH' command required when {@link #getConnection()} call.
     */
    private boolean authRequired = false;

    /**
     * Flag whether to validate (send ping command) on each {@link #getConnection()} call.
     */
    private boolean validateConnection = false;

    public ExtendedJedisConnectionFactory() {
    }

    public ExtendedJedisConnectionFactory(JedisShardInfo shardInfo) {
        super(shardInfo);
    }
    public ExtendedJedisConnectionFactory(JedisPoolConfig poolConfig) {
        super(poolConfig);
    }
    public ExtendedJedisConnectionFactory(RedisSentinelConfiguration sentinelConfig) {
        super(sentinelConfig);
    }
    public ExtendedJedisConnectionFactory(RedisSentinelConfiguration sentinelConfig, JedisPoolConfig poolConfig) {
        super(sentinelConfig, poolConfig);
    }
    public ExtendedJedisConnectionFactory(RedisClusterConfiguration clusterConfig) {
        super(clusterConfig);
    }
    public ExtendedJedisConnectionFactory(RedisClusterConfiguration clusterConfig, JedisPoolConfig poolConfig) {
        super(clusterConfig, poolConfig);
    }

    public boolean isValidateConnection() {
        return validateConnection;
    }
    public void setValidateConnection(boolean validateConnection) {
        this.validateConnection = validateConnection;
    }

    @Override
    protected JedisConnection postProcessConnection(JedisConnection connection) {
        if(authRequired) {
            LOGGER.debug("Auth required, send 'AUTH' command to server");
            Jedis nativeConnection = connection.getNativeConnection();
            String authResponse = nativeConnection.auth(getPassword());
        }
        if(validateConnection) {
            LOGGER.debug("Revalidate required, validate using 'PING' command");
            connection.ping();
        }
        return connection;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if(StringUtils.hasText(getPassword())) {
            this.authRequired = true;
        }
    }
}
