package com.pm.myawsproject.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final S3Client s3Client;
    private final DynamoDbClient dynamoDbClient;
    private final SqsClient sqsClient; // Added SQS client

    private static final String BUCKET_NAME = "my-test-bucket";
    private static final String TABLE_NAME = "documents-metadata";
    private static final String QUEUE_NAME = "document-processing-queue"; // Queue name

    @Value("${aws.endpoint:http://localhost:4566}")
    private String awsEndpoint;

    private String queueUrl;

    @PostConstruct
    public void initInfrastructure() {

        initS3Bucket();
        initDynamoDbTable();
        initSqsQueue();
    }

    public String handleDocumentUpload(String fileName, byte[] fileBytes) {

        String documentId = UUID.randomUUID().toString();

        String s3Key = documentId + "_" + fileName;

        try {

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(s3Key)
                            .build(),
                    RequestBody.fromBytes(fileBytes)
            );

            log.info("File uploaded to S3. Key: {}", s3Key);


            String s3Url = String.format(
                    "%s/%s/%s",
                    awsEndpoint,
                    BUCKET_NAME,
                    s3Key
            );

            Map<String, AttributeValue> item = new HashMap<>();

            item.put(
                    "documentId",
                    AttributeValue.builder().s(documentId).build()
            );

            item.put(
                    "fileName",
                    AttributeValue.builder().s(fileName).build()
            );

            item.put(
                    "s3Url",
                    AttributeValue.builder().s(s3Url).build()
            );

            item.put(
                    "uploadTimestamp",
                    AttributeValue.builder()
                            .n(String.valueOf(System.currentTimeMillis()))
                            .build()
            );

            dynamoDbClient.putItem(
                    PutItemRequest.builder()
                            .tableName(TABLE_NAME)
                            .item(item)
                            .build()
            );

            log.info("Metadata saved to DynamoDB. ID: {}", documentId);


            String messageBody = String.format(
                    "{\"documentId\":\"%s\", \"fileName\":\"%s\"}",
                    documentId,
                    fileName
            );

            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(messageBody)
                            .build()
            );

            log.info(
                    "Processing message sent to SQS: {}",
                    messageBody
            );

            return documentId;

        } catch (Exception e) {

            log.error("Error occurred during document upload:", e);

            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Map<String, String> getDocumentMetadata(String documentId) {

        Map<String, AttributeValue> key = new HashMap<>();

        key.put(
                "documentId",
                AttributeValue.builder().s(documentId).build()
        );

        Map<String, AttributeValue> item =
                dynamoDbClient.getItem(
                        GetItemRequest.builder()
                                .tableName(TABLE_NAME)
                                .key(key)
                                .build()
                ).item();

        Map<String, String> result = new HashMap<>();

        if (item != null && !item.isEmpty()) {

            result.put(
                    "documentId",
                    item.get("documentId").s()
            );

            result.put(
                    "fileName",
                    item.get("fileName").s()
            );

            result.put(
                    "s3Url",
                    item.get("s3Url").s()
            );

            result.put(
                    "uploadTimestamp",
                    item.get("uploadTimestamp").n()
            );
        }

        return result;
    }


    private void initS3Bucket() {

        try {

            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );

        } catch (NoSuchBucketException e) {

            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );

            log.info(
                    "New S3 bucket created: {}",
                    BUCKET_NAME
            );
        }
    }

    private void initDynamoDbTable() {

        try {

            dynamoDbClient.describeTable(
                    DescribeTableRequest.builder()
                            .tableName(TABLE_NAME)
                            .build()
            );

        } catch (ResourceNotFoundException e) {

            CreateTableRequest createTableRequest =
                    CreateTableRequest.builder()
                            .tableName(TABLE_NAME)
                            .keySchema(
                                    KeySchemaElement.builder()
                                            .attributeName("documentId")
                                            .keyType(KeyType.HASH)
                                            .build()
                            )
                            .attributeDefinitions(
                                    AttributeDefinition.builder()
                                            .attributeName("documentId")
                                            .attributeType(ScalarAttributeType.S)
                                            .build()
                            )
                            .provisionedThroughput(
                                    ProvisionedThroughput.builder()
                                            .readCapacityUnits(5L)
                                            .writeCapacityUnits(5L)
                                            .build()
                            )
                            .build();

            dynamoDbClient.createTable(createTableRequest);

            log.info(
                    "New DynamoDB table created: {}",
                    TABLE_NAME
            );
        }
    }

    private void initSqsQueue() {

        try {

            queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder()
                            .queueName(QUEUE_NAME)
                            .build()
            ).queueUrl();

            log.info(
                    "SQS queue already exists: {}",
                    queueUrl
            );

        } catch (QueueDoesNotExistException e) {

            queueUrl = sqsClient.createQueue(
                    CreateQueueRequest.builder()
                            .queueName(QUEUE_NAME)
                            .build()
            ).queueUrl();

            log.info(
                    "New SQS queue created: {}",
                    queueUrl
            );

        } catch (Exception e) {
            log.error(
                    "Error occurred while creating SQS queue:",
                    e
            );
        }
    }
}