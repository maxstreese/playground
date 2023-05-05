plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("net.logstash.logback:logstash-logback-encoder:7.3")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("redis.clients:jedis:4.3.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:testcontainers:1.18.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("redistlstc.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
