package redistlstc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@Testcontainers
class RedisWithoutTlsTest {

    private static final int PORT = 6379;

    private Jedis jedis;

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:7"))
            .withExposedPorts(PORT);

    @BeforeEach
    public void setUp() {
        var address = redis.getHost();
        var port    = redis.getMappedPort(PORT);
        jedis = new Jedis(address, port);
    }

    @Test
    public void testSetAndGet() {
        jedis.hset("test", "foo", "bar");
        jedis.hget("test", "foo");
        assertThat(jedis.hget("test", "foo")).isEqualTo("bar");
    }

}
