package com.example.adoptions;

import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public class TestAdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }
}

@Configuration
@Testcontainers
class TestContainersConfiguration {

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> postgreSQLContainer() {
        var image = DockerImageName.parse("pgvector/pgvector:pg16")
                .asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer<>(image);
    }

    static class SchedulingContainer extends GenericContainer<SchedulingContainer> {
        SchedulingContainer() {
            super(DockerImageName.parse("scheduling"));
            addExposedPort(8081);
            setWaitStrategy(Wait.forHttp("/sse"));
            setLogConsumers(List.of(new Slf4jLogConsumer(LoggerFactory.getLogger(getClass()))));
        }
    }

    @Bean
    SchedulingContainer adoptionService() {
        return new SchedulingContainer();
    }

    @Bean
    DynamicPropertyRegistrar adoptionServiceProperties(SchedulingContainer container) {
        return (properties) ->
                properties.add("adoption-service.url", () -> "http://localhost:" + container.getFirstMappedPort());
    }
}

@Configuration
class DataInitializer {

    @RestartScope
    @Bean
    ApplicationRunner initializerRunner(VectorStore vectorStore, DogRepository dogRepository) {
        return args -> {
            // Not great. I want to init the vectorStore with data, but with auto-restart and the Postgres Container RestartScope, we only want to do it once, not on every reload
            if (System.getProperty("INIT") == null) {
                System.setProperty("INIT", "true");
                // we do this here instead of in the schema.sql because when multiple test classes run, the schema.sql gets run each time, resulting in duplicates
                dogRepository.saveAll(
                        List.of(
                                // note: 0 seems to be the magic number to cause an insert and get the DB sequence to assign an id
                                new Dog(0, "Jasper", null, "A grey Shih Tzu known for being protective."),
                                new Dog(0, "Toby", null, "A grey Doberman known for being playful."),
                                new Dog(0, "Nala", null, "A spotted German Shepherd known for being loyal."),
                                new Dog(0, "Penny", null, "A white Great Dane known for being protective."),
                                new Dog(0, "Bella", null, "A golden Poodle known for being calm."),
                                new Dog(0, "Willow", null, "A brindle Great Dane known for being calm."),
                                new Dog(0, "Daisy", null, "A spotted Poodle known for being affectionate."),
                                new Dog(0, "Mia", null, "A grey Great Dane known for being loyal."),
                                new Dog(0, "Molly", null, "A golden Chihuahua known for being curious."),
                                new Dog(0, "Ruby", null, "A white Great Dane known for being protective."),
                                new Dog(0, "Prancer", null, "A demonic, neurotic, man hating, animal hating, children hating dogs that look like gremlins.")
                        ));

                var doguments = dogRepository.findAll().stream().map(dog -> new Document("id: %s, name: %s, description: %s".formatted(dog.id(), dog.name(), dog.description())))
                        .toList();
                vectorStore.add(doguments);
            }

        };
    }
}
