package com.pm.myawsproject.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentProcessor {
    @SqsListener(value = "document-processing-queue")
    public void listenToDocumentQueue(String messageBody) {

        log.info(
                "📬 [MODERN ASYNCHRONOUS PROCESSING] New message received from queue: {}",
                messageBody
        );

        processDocument(messageBody);
    }

    private void processDocument(String messageBody) {
        try {

            Thread.sleep(3000);

            log.info(
                    "Document has been fully prepared in the background."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.error("Processing was interrupted:", e);
        }
    }
}