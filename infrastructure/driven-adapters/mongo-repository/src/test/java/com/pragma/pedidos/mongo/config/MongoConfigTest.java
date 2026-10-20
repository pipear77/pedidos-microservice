package com.pragma.pedidos.mongo.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ssl.SslBundles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MongoConfigTest {

    @Test
    void mongoConfigBeansCreation() {
        MongoConfig config = new MongoConfig();
        MongoDBSecret secret = config.dbSecret("mongodb://localhost:27017/test");
        assertNotNull(secret);

        SslBundles sslBundles = mock(SslBundles.class);
        var connectionDetails = config.mongoProperties(secret, sslBundles);
        assertNotNull(connectionDetails);
    }
}