package com.scansettler.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

@Service
public class OcrService
{
    private final static Logger LOG = LoggerFactory.getLogger(OcrService.class);

    @Value("${OCR_SERVER_URL:http://ocr-server:8000}")
    private String ocrServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractTextFromImage(File imageFile)
    {
        final String url = ocrServerUrl + "/ocr";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imageFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try
        {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
            {
                String text = (String) response.getBody().get("text");
                LOG.info(text);
                return (String) response.getBody().get("text");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Error calling OCR service: " + e.getMessage();
        }

        return "OCR failed";
    }
}
