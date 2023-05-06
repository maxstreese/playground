package redistlstc;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RedisWithoutTlsTest {

    private static final int PORT = 6379;

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:7"))
            .withExposedPorts(PORT);

    @Test
    public void testSetAndGet() {
        try (Jedis jedis = new Jedis(redis.getHost(), redis.getMappedPort(PORT))) {
            assertThat(jedis.hset("foo", "bar", "baz")).isEqualTo(1);
            assertThat(jedis.hget("foo", "bar")).isEqualTo("baz");
        }
    }

}
