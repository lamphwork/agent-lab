package com.bmn.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenAIConfig {

//    @Value("${OPEN_API_KEY}")
//    private String openAIKey;
//
//    @Bean
//    @Primary
//    ChatModel openAIChatModel() {
//        OpenAiApi api = OpenAiApi.builder().apiKey(openAIKey).build();
//        return OpenAiChatModel.builder().openAiApi(api).build();
//    }
//
    @Bean
    ChatClient openAIChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

}
