package com.tiket.tix.common.spring.redis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zakyalvan
 */
@ConfigurationProperties(prefix = "tiket.redis")
public class ExtendedRedisProperties {
    private boolean enabled = true;

    /**
     * Connection settings.
     */
    @NestedConfigurationProperty
    private Map<String, RedisConnection> connections = new HashMap<>();

    /**
     * Flag whether to validate connection.
     */
    private boolean validateConnections = false;

    public Map<String, RedisConnection> getConnections() {
        return connections;
    }

    public boolean isValidateConnections() {
        return validateConnections;
    }

    public void setValidateConnections(boolean validateConnections) {
        this.validateConnections = validateConnections;
    }

    public static class RedisConnection {
        /**
         * Database index used by the connection factory.
         */
        private int database = 0;

        /**
         * Redis url, which will overrule host, port and password if set.
         */
        private String url;

        /**
         * Redis server host.
         */
        private String host = "localhost";

        /**
         * Login password of the redis server.
         */
        private String password;

        /**
         * Redis server port.
         */
        private int port = 6379;

        /**
         * Enable SSL.
         */
        private boolean ssl;

        /**
         * Connection timeout in milliseconds.
         */
        private int timeout = 4000;

        private ConnectionPool pool;

        private ConnectionRevalidate revalidate;

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public ConnectionPool getPool() {
            return pool;
        }

        public void setPool(ConnectionPool pool) {
            this.pool = pool;
        }

        public ConnectionRevalidate getRevalidate() {
            return revalidate;
        }

        public void setRevalidate(
                ConnectionRevalidate revalidate) {
            this.revalidate = revalidate;
        }
    }

    public static class ConnectionPool {

        /**
         * Max number of "idle" connections in the pool. Use a negative value to indicate
         * an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 0;

        /**
         * Max number of connections that can be allocated by the pool at a given time.
         * Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time (in milliseconds) a connection allocation should block
         * before throwing an exception when the pool is exhausted. Use a negative value
         * to block indefinitely.
         */
        private int maxWait = -1;

        private boolean testOnBorrow = false;

        private boolean testOnCreate = false;

        private boolean testOnReturn = false;

        private boolean testWhileIdle = false;

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public boolean isTestOnCreate() {
            return testOnCreate;
        }

        public void setTestOnCreate(boolean testOnCreate) {
            this.testOnCreate = testOnCreate;
        }

        public boolean isTestOnReturn() {
            return testOnReturn;
        }

        public void setTestOnReturn(boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        public boolean isTestWhileIdle() {
            return testWhileIdle;
        }

        public void setTestWhileIdle(boolean testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
        }
    }

    public static class ConnectionRevalidate {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
