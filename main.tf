provider "aws" {
  region                      = "us-east-1"
  access_key                  = "mock_key"
  secret_key                  = "mock_secret"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3       = "http://localhost:4566"
    sqs      = "http://localhost:4566"
    dynamodb = "http://localhost:4566"
  }
}

# 1. S3 Bucket
resource "aws_s3_bucket" "document_bucket" {
  bucket = "user-documents-bucket"
}

# 2. SQS Queue
resource "aws_sqs_queue" "notification_queue" {
  name = "document-notification-queue"
}

# 3. DynamoDB Table
resource "aws_dynamodb_table" "document_meta" {
  name         = "DocumentMetadata"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "documentId"

  attribute {
    name = "documentId"
    type = "S" # String
  }
}