package com.tiket.tix.common.spring.redis.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zakyalvan
 */
public class FailOverCapableConnectionFactory implements RedisConnectionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailOverCapableConnectionFactory.class);

    private final Queue<RedisConnectionFactory> failOverFactories = new ConcurrentLinkedQueue<>();

    private RedisConnectionFactory currentFactory;

    private final Object selectionMonitor = new Object();

    private final AtomicBoolean failOverProgressed = new AtomicBoolean(false);

    private final boolean reconnectStaled = false;

    private final ScheduledExecutorService reconnectExecutors = Executors.newScheduledThreadPool(10);

    public FailOverCapableConnectionFactory(Set<RedisConnectionFactory> delegateCandidates) {
        Assert.notEmpty(delegateCandidates, "No delegate redis connection factories provided");
        failOverFactories.addAll(delegateCandidates);
    }

    /**
     * Elect current {@link RedisConnectionFactory} to be used.
     */
    protected void selectConnectionFactory() {
        synchronized (selectionMonitor) {
            if (failOverFactories.isEmpty()) {
                throw new ConnectionsFailOverException();
            }

            failOverProgressed.set(true);
            if (currentFactory != null && reconnectStaled) {
                LOGGER.trace("Handle reconnect staled");


            }
            currentFactory = failOverFactories.poll();
            failOverProgressed.set(false);
        }
    }

    /**
     * Mark current {@link RedisConnectionFactory factory} as slated connection factory.
     * Here we schedule for validating the connection factory before permanently dropping.
     */
    protected void invalidConnectionFactory(RedisConnectionFactory connectionFactory) {
        this.currentFactory = null;
    }

    @Override
    public RedisConnection getConnection() {
        while (failOverProgressed.get()) {
            LOGGER.info("Fail over in progress");
        }

        RedisConnection connection = null;

        do {
            if (currentFactory == null) {
                selectConnectionFactory();
            }

            try {
                RedisConnection candidate = currentFactory.getConnection();
                if (candidate.isClosed()) {
                    continue;
                }
                connection = candidate;
            }
            catch (RedisConnectionFailureException ex) {
                LOGGER.error("Error on retrieve redis connection", ex);
                invalidConnectionFactory(currentFactory);
            }
        }
        while (!failOverFactories.isEmpty() && connection == null);

        if (failOverFactories.isEmpty() && connection == null) {
            throw new ConnectionsFailOverException("No more redis-connection-factory to fail over");
        }

        return connection;
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        while (!failOverProgressed.get()) ;

        if (currentFactory == null) {
            selectConnectionFactory();
        }

        return currentFactory.getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        while (!failOverProgressed.get()) ;

        if (currentFactory == null) {
            selectConnectionFactory();
        }

        return currentFactory.getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        while (!failOverProgressed.get()) ;

        if (currentFactory == null) {
            selectConnectionFactory();
        }

        return currentFactory.getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return currentFactory.translateExceptionIfPossible(ex);
    }
}
