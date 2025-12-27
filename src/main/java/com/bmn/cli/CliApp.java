package com.bmn.cli;

import com.bmn.adapter.InMemContextLoader;
import com.bmn.adapter.OpenAI;
import com.bmn.core.Orchestrator;
import com.bmn.core.model.Agent;
import com.bmn.core.model.AgentInput;
import com.bmn.core.port.ContextLoader;
import com.bmn.core.port.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.UUID;

public class CliApp {

    static Logger logger = LoggerFactory.getLogger(CliApp.class);

    public static void main(String[] args) {
        Agent agent = new Agent(
                "agent_test",
                "Document Agent",
                ""
        );

        String apiKey =  System.getenv("OPEN_AI_KEY");
        LLMClient llm = new OpenAI(apiKey, "gpt-4o-mini", "text-embedding-3-small");

        ContextLoader contextLoader = new InMemContextLoader();

        Orchestrator orchestrator = new Orchestrator(llm, contextLoader);

        Scanner scanner = new Scanner(System.in);
        String contextId = UUID.randomUUID().toString();

        while (true) {
            logger.info("Type some thing: ");
            String input = scanner.nextLine();

            if (input.equals("/exit")) {
                break;
            }

            orchestrator.process(agent, new AgentInput(contextId, input), output -> {
                logger.info("[{}] {}", output.getType(), output.getContent());
            });
        }
    }
}
