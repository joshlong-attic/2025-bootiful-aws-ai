package com.example.adoptions;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// todo eventually well use MCP to move the appointment functionality out to another MCP service


@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }
}

@Configuration
@ImportRuntimeHints(ConversationalConfiguration.Hints.class)
class ConversationalConfiguration {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            var values = MemberCategory.values();
            for (var c : new Class<?>[]{ConverseResponse.class, ConverseRequest.class ,
                    InvokeModelRequest.class , InvokeModelResponse.class}) {
                hints.reflection().registerType(c, values);
            }
        }
    }


    // add this <em>after</em> showing the
    @Bean
    McpSyncClient mcpClient() {
        var mcpClient = McpClient
                .sync(new HttpClientSseClientTransport("http://localhost:8081"))
                .build();
        mcpClient.initialize();
        return mcpClient;
    }


    @Bean
    ChatClient chatClient(
            DogRepository repository,
            VectorStore vectorStore,
//            DogAdoptionAppointmentScheduler scheduler ,
            McpSyncClient mcpSyncClient,
            ChatClient.Builder builder) {

        if (false)
        repository.findAll().forEach(dog -> {
            var dogument = new Document("id: %s, name: %s, description: %s".formatted(dog.id(), dog.name(), dog.description()));
            vectorStore.add(List.of(dogument));
        });

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Atlanta, Antwerp, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        return builder
                .defaultSystem(system)
                .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClient))
//                .defaultTools(scheduler)
                .build();
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}


/*

@Component
class DogAdoptionAppointmentScheduler {

    private final ObjectMapper om;

    DogAdoptionAppointmentScheduler(ObjectMapper om) {
        this.om = om;
    }

    @Tool(description = "schedule an appointment to adopt a dog" +
            " at the Pooch Palace dog adoption agency")
    String scheduleDogAdoptionAppointment(
            @ToolParam(description = "the id of the dog") int id,
            @ToolParam(description = "the name of the dog") String name)
            throws Exception {
        var instant = Instant.now().plus(3, ChronoUnit.DAYS);
        System.out.println("confirming the appointment: " + instant);
        return om.writeValueAsString(instant);
    }
}
*/


@Controller
@ResponseBody
class ConversationalController {

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final Map<Long, PromptChatMemoryAdvisor> chatMemory = new ConcurrentHashMap<>();

    // todo move questionAnswerAdvisor back to ChatClient
    ConversationalController(VectorStore vectorStore, ChatClient chatClient) {
        this.chatClient = chatClient;
        this.questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
    }

    // http --form POST http://localhost:8080/2/inquire question="Do you have any neurotic dogs?"
    @PostMapping("/{id}/inquire")
    String inquire(@PathVariable Long id, @RequestParam String question) {
        var promptChatMemoryAdvisor = this.chatMemory
                .computeIfAbsent(id, key -> PromptChatMemoryAdvisor.builder(new InMemoryChatMemory()).build());
        return this.chatClient
                .prompt()
                .user(question)
                .advisors(this.questionAnswerAdvisor, promptChatMemoryAdvisor)
                .call()
                .content();
    }
}