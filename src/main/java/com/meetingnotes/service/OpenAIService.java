package com.meetingnotes.service;

import com.meetingnotes.config.OpenAIProperties;
import com.meetingnotes.exception.AIProcessingException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAiService openAiService;
    private final OpenAIProperties openAIProperties;

    public String generateSummary(String transcription) {
        try {
            log.info("Generating summary for meeting transcription");
            String prompt = String.format("""
                    Please provide a concise summary of the following meeting transcription in 2-3 paragraphs:
                    
                    %s
                    """, transcription);
            
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error generating summary", e);
            throw new AIProcessingException("Failed to generate meeting summary", e);
        }
    }

    public String extractKeyPoints(String transcription) {
        try {
            log.info("Extracting key points from meeting transcription");
            String prompt = String.format("""
                    Extract the main key points discussed in this meeting. Format as a numbered list:
                    
                    %s
                    """, transcription);
            
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error extracting key points", e);
            throw new AIProcessingException("Failed to extract key points", e);
        }
    }

    public String extractActionItems(String transcription) {
        try {
            log.info("Extracting action items from meeting transcription");
            String prompt = String.format("""
                    Extract action items from this meeting. For each action item, provide:
                    - Title
                    - Description
                    - Assigned To (if mentioned)
                    - Due Date (if mentioned)
                    Format as JSON array.
                    
                    %s
                    """, transcription);
            
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error extracting action items", e);
            throw new AIProcessingException("Failed to extract action items", e);
        }
    }

    public String identifyPriorities(String transcription) {
        try {
            log.info("Identifying priorities from meeting transcription");
            String prompt = String.format("""
                    Identify the main priorities discussed in this meeting. For each priority, provide:
                    - Title
                    - Description
                    - Level (HIGH, MEDIUM, or LOW)
                    Format as JSON array.
                    
                    %s
                    """, transcription);
            
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error identifying priorities", e);
            throw new AIProcessingException("Failed to identify priorities", e);
        }
    }

    private String callOpenAI(String prompt) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", prompt));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(openAIProperties.getModel())
                    .messages(messages)
                    .temperature(openAIProperties.getTemperature())
                    .maxTokens(openAIProperties.getMaxTokens())
                    .build();

            var response = openAiService.createChatCompletion(request);
            return response.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new AIProcessingException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }
}