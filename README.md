# README

## Introduction

This module provided extension to default ```spring-boot-starter-data-redis```, enabling fail over multiple ```RedisConnectionFactory```, i.e. select other valid connection factory when current connection factory fail.

## Usage

This section explain how to use this module.

Include this starter into your project

```xml
    <dependency>
        <groupId>com.tiket.oss.spring.starters</groupId>
        <artifactId>spring-redis-twemproxy-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
```

### Using Auto Configuration

By using auto configuration, you only need to provide settings in ```application.properties```. Please note, this starter auto configure one ```RedisConnectionFactory``` extension (```com.tiket.tix.common.spring.redis.connection.FailOverCapableConnectionFactory```) if there is no bean of this type configured manually.

> To disable this module auto configuration support, set property ```tiket.redis.enabled``` to ```false``` in ```application.properties```.

Following snippets shows available settings.

```properties

## Disable di module auto-configuration, default is true/enabled.
tiket.redis.enabled=true

## Flag whether to validate connection (ping server).
tiket.redis.validate-connections=false

## This create one RedisConnectionFactory with name 'first-node'
tiket.redis.connections.first-node.host=localhost
tiket.redis.connections.first-node.port=22121
## Connection timeout in milliseconds
tiket.redis.connections.first-node.timeout=5000
tiket.redis.connections.first-node.pool.maxActive=8
tiket.redis.connections.first-node.pool.minIdle=2
tiket.redis.connections.first-node.pool.maxIdle=4
tiket.redis.connections.first-node.pool.maxWait=1000

## This create one RedisConnectionFactory with name 'second-node'
tiket.redis.connections.second-node.port=22122
tiket.redis.connections.second-node.pool.maxActive=8
tiket.redis.connections.second-node.pool.minIdle=2
tiket.redis.connections.second-node.pool.maxIdle=4
tiket.redis.connections.second-node.pool.maxWait=1000

## This create one RedisConnectionFactory with name 'second-node'
tiket.redis.connections.third-node.port=22123
tiket.redis.connections.third-node.pool.maxActive=8
tiket.redis.connections.third-node.pool.minIdle=2
tiket.redis.connections.third-node.pool.maxIdle=4
tiket.redis.connections.third-node.pool.maxWait=1000

```

This settings will provide fail over capability against three redis (or twemproxy) node. Please note, you can use any string name relevant to your application.

### Using Manual Configuration

Manual configuration mean you have to create bean of type ```com.tiket.tix.common.spring.redis.connection.FailOverCapableConnectionFactory``` manually. Provide collection of ```RedisConnectionFactory```

Following snippets show how to configure manually

```java

@Configuration
public class RedisConfiguration {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new FailOverCapableConnectionFactory(Arrays.asList(firstTwemproxyRedis(), secondTwemproxyRedis(), thirdTwemproxyRedis()));
    }
    
    // You can create this without adding to spring's context (no @Bean), by don't forget to call ```afterPropertySet``` after configuring.
    @Bean
    public RedisConnectionFactory firstTwemproxyRedis() {
        JedisConnectionFactory connectionFactory;
        // Instantiate and configure this delegate connection factory.
        return connectionFactory;
    }
    
    // You can create this without adding to spring's context (no @Bean), by don't forget to call ```afterPropertySet``` after configuring.
    @Bean
    public RedisConnectionFactory secondTwemproxyRedis() {
        JedisConnectionFactory connectionFactory;
        // Instantiate and configure this delegate connection factory.
        return connectionFactory;
    }
    
    // You can create this without adding to spring's context (no @Bean), by don't forget to call ```afterPropertySet``` after configuring.
    @Bean
    public RedisConnectionFactory thirdTwemproxyRedis() {
        JedisConnectionFactory connectionFactory;
        // Instantiate and configure this delegate connection factory.
        return connectionFactory;
    }
}

```

> Creating ```FailOverCapableConnectionFactory``` manually, disable autoconfiguration of this type.

## Todo

- Revalidate or reconnect stale ```RedisConnectionFactory``` after fail over process. Currently, failed ```RedisConnectionFactory``` is destroyed.
- Enable selection of driver, jedis or lettuce. Currently only jedis supported.
