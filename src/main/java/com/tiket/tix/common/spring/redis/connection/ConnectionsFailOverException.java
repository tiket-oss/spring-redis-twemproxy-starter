package com.tiket.tix.common.spring.redis.connection;

import org.springframework.dao.DataAccessException;

/**
 * Exception to be thrown when no fail over redis connection factory failed.
 *
 * @author zakyalvan
 */
public class ConnectionsFailOverException extends DataAccessException {
    public ConnectionsFailOverException() {
        super("Fail over redis connection factory failed, no fail over candidates found");
    }

    public ConnectionsFailOverException(String msg) {
        super(msg);
    }

    public ConnectionsFailOverException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
