package com.pm.myawsproject.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class DocumentMetadata {
    private String documentId;
    private String fileName;
    private String s3Url;
    private Long uploadTimestamp;

    @DynamoDbPartitionKey
    public String getDocumentId() {
        return documentId;
    }
}