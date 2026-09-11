package com.scansettler.controllers;

import com.scansettler.models.ReceiptItem;
import com.scansettler.services.OcrService;
import com.scansettler.services.OllamaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/receipt")
public record ReceiptController(OllamaService ollamaService, OcrService ocrService)
{
    private final static Logger LOG = LoggerFactory.getLogger(ReceiptController.class);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ReceiptItem> readReceipt(@RequestParam("file") MultipartFile file) throws IOException
    {
        LOG.info("POST /receipt");

        File tempFile = File.createTempFile("receipt-", ".tmp");
        file.transferTo(tempFile);

        String ocrText = ocrService.extractTextFromImage(tempFile);

        tempFile.delete();

        return ollamaService.extractItemsFromText(ocrText);
    }
}
