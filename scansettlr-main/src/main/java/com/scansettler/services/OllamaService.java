package com.scansettler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OllamaService
{
    private final static String BASE_URL = "http://host.docker.internal:11434";
    private final static String MODEL_NAME = "mistral";
    private final static String PROMPT = """
            The given text is an output from an OCR which scanned a receipt.
            Give me a list of items that were on the receipt and the amount paid for them.
            The response must be just the items in a JSON structure (name: amount).
            The items will be either in Polish or English language.
            
            Text:
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OllamaChatModel model = OllamaChatModel.builder()
            .baseUrl(BASE_URL)
            .modelName(MODEL_NAME)
            .build();

    public Map<String, String> extractItemsFromText(String text)
    {
        UserMessage userMessage = UserMessage.from(TextContent.from(PROMPT + text));

        OllamaChatRequestParameters params = OllamaChatRequestParameters.builder()
                .modelName(MODEL_NAME)
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(userMessage)
                .parameters(params)
                .build();

        ChatResponse response = model.doChat(chatRequest);
        String json = response.aiMessage().text();

        try
        {
            return objectMapper.readValue(json, new TypeReference<>() {});
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse JSON from LLM response: " + json, e);
        }
    }
}
