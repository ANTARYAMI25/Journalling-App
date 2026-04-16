package com.myorg.props;

import software.amazon.awscdk.services.dynamodb.*;

import java.util.ArrayList;
import java.util.List;

public class DynamoDBTableProps {
    private String tableName;
    private Attribute partitionKey;
    private Attribute sortKey;
    private BillingMode billingMode;
    private List<LocalSecondaryIndexProps> lsis;
    private List<GlobalSecondaryIndexProps> gsis;


    public DynamoDBTableProps(String tableName, String partitionKeyName, AttributeType partitionKeyType) {
        this.tableName = tableName;
        this.partitionKey = Attribute.builder()
                .name(partitionKeyName)
                .type(partitionKeyType)
                .build();
        this.billingMode = BillingMode.PAY_PER_REQUEST;
        this.lsis = new ArrayList<>();
        this.gsis = new ArrayList<>();
    }


    public void setSortKey(String sortKeyName, AttributeType sortKeyType) {
        this.sortKey = Attribute.builder()
                .name(sortKeyName)
                .type(sortKeyType)
                .build();
    }

    //
    public void setBillingMode(BillingMode billingMode) {
        this.billingMode = billingMode;
    }

    // Add LSI
    public void addLsi(String indexName, String sortKeyName, AttributeType sortKeyType) {
        this.lsis.add(LocalSecondaryIndexProps.builder()
                .indexName(indexName)
                .sortKey(Attribute.builder()
                        .name(sortKeyName)
                        .type(sortKeyType)
                        .build())
                .projectionType(ProjectionType.ALL)  // ALL, KEYS_ONLY, INCLUDE
                .build());
    }

    // Add GSI
    public void addGsi(String indexName, String partitionKeyName, AttributeType partitionKeyType) {
        this.gsis.add(GlobalSecondaryIndexProps.builder()
                .indexName(indexName)
                .partitionKey(Attribute.builder()
                        .name(partitionKeyName)
                        .type(partitionKeyType)
                        .build())
                .projectionType(ProjectionType.ALL)
                .build());
    }

    // Add GSI with sort key
    public void addGsi(String indexName,
                       String partitionKeyName, AttributeType partitionKeyType,
                       String sortKeyName, AttributeType sortKeyType) {
        this.gsis.add(GlobalSecondaryIndexProps.builder()
                .indexName(indexName)
                .partitionKey(Attribute.builder()
                        .name(partitionKeyName)
                        .type(partitionKeyType)
                        .build())
                .sortKey(Attribute.builder()
                        .name(sortKeyName)
                        .type(sortKeyType)
                        .build())
                .projectionType(ProjectionType.ALL)
                .build());
    }

    // Getters
    public String getTableName() {
        return tableName;
    }

    public Attribute getPartitionKey() {
        return partitionKey;
    }

    public Attribute getSortKey() {
        return sortKey;
    }

    public BillingMode getBillingMode() {
        return billingMode;
    }

    public List<LocalSecondaryIndexProps> getLsis() {
        return lsis;
    }

    public List<GlobalSecondaryIndexProps> getGsis() {
        return gsis;
    }
}
