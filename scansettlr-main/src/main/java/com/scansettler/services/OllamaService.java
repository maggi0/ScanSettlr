package com.scansettler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class OllamaService
{
    private final static String BASE_URL = "http://host.docker.internal:11434";
    private final static String MODEL_NAME = "mistral";
    private final static String PROMPT = """
        Poniższy tekst jest wynikiem działania OCR na paragonie.
        
        Wyodrębnij z tekstu wszystkie zakupione produkty lub usługi oraz kwotę zapłaconą za każdą z nich.
        
        Zasady:
        - Zwracaj wyłącznie rzeczywiste produkty lub usługi znajdujące się na paragonie.
        - Pomijaj nazwę i dane sklepu, adres, NIP, numer paragonu, datę, godzinę oraz informacje dotyczące płatności.
        - Pomijaj informacje o podatku VAT, stawkach VAT oraz podsumowania podatkowe.
        - Pomijaj sumy paragonu, takie jak „SUMA”, „RAZEM”, „DO ZAPŁATY”, „TOTAL” i podobne.
        - Suma całego paragonu nigdy nie może zostać uznana za produkt.
        - Pomijaj pojedyncze litery, symbole i fragmenty będące prawdopodobnymi błędami OCR.
        - Pomijaj oznaczenia kategorii lub stawek podatkowych występujące przy produktach lub cenach.
        - Nazwa produktu i jego cena mogą znajdować się w osobnych wierszach. Połącz je w jeden produkt.
        - Jeżeli przy produkcie podana jest ilość, jako kwotę podaj całkowitą kwotę zapłaconą za ten produkt, a nie cenę jednostkową.
        - Zachowaj nazwę produktu możliwie blisko oryginalnego tekstu.
        - Popraw oczywiste błędy OCR w nazwach produktów, jeżeli można jednoznacznie określić prawidłową nazwę.
        - Produkty mogą być w języku polskim lub angielskim.
        - Kwoty zapisuj jako liczby, bez symbolu waluty.
        - Jako separator części dziesiętnej używaj kropki zamiast przecinka.
        
        Odpowiedź musi być wyłącznie poprawnym JSON-em w następującym formacie:
        
        {
          "produkt1": "kwota1",
          "produkt2": "kwota2"
        }
        
        Jeżeli nie uda się znaleźć żadnych produktów, zwróć:
        
        {}
        
        Nie dodawaj żadnych wyjaśnień, komentarzy, formatowania Markdown ani tekstu poza JSON-em.
        
        Tekst OCR:
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OllamaChatModel model = OllamaChatModel.builder()
            .baseUrl(BASE_URL)
            .modelName(MODEL_NAME)
            .temperature(0.0)          // stop creative invention of products
            .topK(1)
            .repeatPenalty(1.0)
            .responseFormat(ResponseFormat.JSON)   // Ollama format=json -> no fences
            .timeout(Duration.ofMinutes(5))
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
