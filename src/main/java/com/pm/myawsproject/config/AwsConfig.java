package com.pm.myawsproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    private final URI localstackEndpoint = URI.create(System.getenv()
            .getOrDefault("AWS_ENDPOINT", "http://localhost:4566"));
    private final Region region = Region.US_EAST_1;

    private StaticCredentialsProvider getCredentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")
        );
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(localstackEndpoint)
                .region(region)
                .credentialsProvider(getCredentials())
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(localstackEndpoint)
                .region(region)
                .credentialsProvider(getCredentials())
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .endpointOverride(localstackEndpoint)
                .region(region)
                .credentialsProvider(getCredentials())
                .build();
    }

    @Bean
    @Primary
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(localstackEndpoint)
                .region(region)
                .credentialsProvider(getCredentials())
                .build();
    }
}