package redistlstc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

// @Disabled
@Testcontainers
class RedisWithTlsDefaultTest {

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
    public void testSetAndGet() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        var redisCertFile = tmpDir.resolve("redis.p12");
        var caCertFile = tmpDir.resolve("ca.p12");

        redis.copyFileFromContainer("/tests/tls/redis.p12", redisCertFile.toString());
        redis.copyFileFromContainer("/tests/tls/ca.p12", caCertFile.toString());

        var keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new FileInputStream(redisCertFile.toString()), "secret".toCharArray());

        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "secret".toCharArray());

        var trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(new FileInputStream(caCertFile.toString()), "secret".toCharArray());

        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        var jedis = new Jedis(redis.getHost(), redis.getMappedPort(PORT), true, sslContext.getSocketFactory(), sslContext.getDefaultSSLParameters(), null);
        assertThat(jedis.hset("foo", "bar", "baz")).isEqualTo(1);
        assertThat(jedis.hget("foo", "bar")).isEqualTo("baz");

    }

}
