package redistlstc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class RedisWithTlsCustomViaProperties {

    private static final int PORT = 6379;

    @TempDir
    private Path tmpDir;

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
    public void testSetAndGet() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

        var keyStore = KeyStore.getInstance("pkcs12");
        redis.copyFileFromContainer("/tests/tls/redis.p12", inputStream -> {
            keyStore.load(inputStream, "secret".toCharArray());
            return null;
        });
        var keyStoreFile = tmpDir.resolve("keystore.p12");
        keyStore.store(Files.newOutputStream(keyStoreFile), "secret".toCharArray());

        var trustStore = KeyStore.getInstance("pkcs12");
        redis.copyFileFromContainer("/tests/tls/ca.p12", inputStream -> {
            trustStore.load(inputStream, "secret".toCharArray());
            return null;
        });
        var trustStoreFile = tmpDir.resolve("truststore.p12");
        trustStore.store(Files.newOutputStream(trustStoreFile), "secret".toCharArray());

        System.setProperty("javax.net.ssl.keyStore", keyStoreFile.toString());
        System.setProperty("javax.net.ssl.keyStorePassword", "secret");
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "secret");

        try (Jedis jedis = new Jedis(redis.getHost(), redis.getMappedPort(PORT), true)) {
            assertThat(jedis.hset("foo", "bar", "baz")).isEqualTo(1);
            assertThat(jedis.hget("foo", "bar")).isEqualTo("baz");
        }

    }

}
