package redistlstc;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RedisWithTlsCustomTest {

    private static final int PORT = 6379;

    @Container
    public GenericContainer redis =
            new GenericContainer(
                    new ImageFromDockerfile("redistls", false)
                            .withFileFromClasspath("./gen-test-certs.sh", "docker/gen-test-certs.sh")
                            .withFileFromClasspath("Dockerfile", "docker/Dockerfile"))
                    .withExposedPorts(PORT)
                    .withCommand(
                            "redis-server",
                            "--tls-port", Integer.toString(PORT),
                            "--port", "0",
                            "--tls-cert-file", "/tests/tls/redis.crt",
                            "--tls-key-file", "/tests/tls/redis.key",
                            "--tls-ca-cert-file", "/tests/tls/ca.crt");

    @Test
    public void testSetAndGet() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {

        var keyStore = KeyStore.getInstance("pkcs12");
        redis.copyFileFromContainer("/tests/tls/redis.p12", inputStream -> {
            keyStore.load(inputStream, "secret".toCharArray());
            return null;
        });

        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "secret".toCharArray());

        var trustStore = KeyStore.getInstance("pkcs12");
        redis.copyFileFromContainer("/tests/tls/ca.p12", inputStream -> {
            trustStore.load(inputStream, "secret".toCharArray());
            return null;
        });

        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        try (Jedis jedis = new Jedis(redis.getHost(), redis.getMappedPort(PORT), true, sslContext.getSocketFactory(), sslContext.getDefaultSSLParameters(), null)) {
            assertThat(jedis.hset("foo", "bar", "baz")).isEqualTo(1);
            assertThat(jedis.hget("foo", "bar")).isEqualTo("baz");
        }

    }

}
