package com.myorg.construct;

import com.myorg.props.DynamoDBTableProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamoDBTableConstruct extends Construct {
    private final Table table;

    public DynamoDBTableConstruct(Construct scope, String id, DynamoDBTableProps props) {
        super(scope, id);


        Table.Builder tableBuilder = Table.Builder.create(this, "Table")
                .tableName(props.getTableName())
                .partitionKey(props.getPartitionKey())
                .billingMode(props.getBillingMode())//todo check
                .removalPolicy(RemovalPolicy.DESTROY);

        // Add sort key if present
        if (props.getSortKey() != null) {
            tableBuilder.sortKey(props.getSortKey());
        }

        this.table = tableBuilder.build();

        // Apply Local Secondary Indexes after table is built
        for (software.amazon.awscdk.services.dynamodb.LocalSecondaryIndexProps lsi : props.getLsis()) {
            this.table.addLocalSecondaryIndex(lsi);
        }

        // Apply Global Secondary Indexes after table is built
        for (software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps gsi : props.getGsis()) {
            this.table.addGlobalSecondaryIndex(gsi);
        }
    }

    public Table getTable() { return table; }
    public String getTableName() { return table.getTableName(); }
    public String getTableArn() { return table.getTableArn(); }
}
