package com.pm.myawsproject.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentListener {

    @SqsListener("document-processing-queue")
    public void listen(String message) {

        System.out.println("Message received from main queue: " + message);

        if (message.contains("manual_test")) {

            System.out.println("Throwing exception for testing purposes...");

            throw new RuntimeException(
                    "Document could not be processed! (DLQ Test)"
            );
        }

        System.out.println("Document processed successfully.");
    }
}