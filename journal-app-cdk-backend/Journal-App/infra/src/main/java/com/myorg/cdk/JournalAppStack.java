package com.myorg.cdk;

import com.myorg.construct.DynamoDBTableConstruct;
import com.myorg.props.DynamoDBTableProps;
import com.myorg.construct.LambdaConstruct;
import com.myorg.props.LambdaProps;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.GatewayResponse;
import software.amazon.awscdk.services.apigateway.ResponseType;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;
import java.util.Map;

public class JournalAppStack extends Stack {
    public JournalAppStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public JournalAppStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create DynamoDB Mapping Table
        DynamoDBTableProps mappingTableProps = new DynamoDBTableProps(
                "MappingTable",
                "clientId",
                AttributeType.STRING
        );

        mappingTableProps.setSortKey("therapistId", AttributeType.STRING);

        // Add Global Secondary Index
        mappingTableProps.addGsi("TherapistMappingIndex",
                "therapistId", AttributeType.STRING,
                "clientId", AttributeType.STRING);

        // Create the table construct
        DynamoDBTableConstruct mappingTable = new DynamoDBTableConstruct(this, "MappingTable", mappingTableProps);

        // Create DynamoDB Sessions Table
        DynamoDBTableProps sessionsTableProps = new DynamoDBTableProps(
                "SessionsTable",
                "sessionId",
                AttributeType.STRING
        );

        sessionsTableProps.setSortKey("therapistId", AttributeType.STRING);

        // Add Local Secondary Index: clientId
        sessionsTableProps.addLsi("ClientIdIndex",
                "clientId", AttributeType.STRING);

        // Add Global Secondary Index: therapistId and time
        sessionsTableProps.addGsi("TherapistTimeIndex",
                "therapistId", AttributeType.STRING,
                "time", AttributeType.STRING);

        // Add Global Secondary Index: sessionsBySpecializationIndex (title and sessionId)
        sessionsTableProps.addGsi("sessionsBySpecializationIndex",
                "title", AttributeType.STRING,
                "sessionId", AttributeType.STRING);

        DynamoDBTableConstruct sessionsTable = new DynamoDBTableConstruct(this, "SessionsTable", sessionsTableProps);

        // Create DynamoDB Messages Table
        DynamoDBTableProps messagesTableProps = new DynamoDBTableProps(
                "MessagesTable",
                "messageId",
                AttributeType.STRING
        );

        messagesTableProps.setSortKey("conversationId", AttributeType.STRING);

        // Add Global Secondary Index: conversationId and timestamp
        messagesTableProps.addGsi("messagesByConversation",
                "conversationId", AttributeType.STRING,
                "timestamp", AttributeType.STRING);

        DynamoDBTableConstruct messagesTable = new DynamoDBTableConstruct(this, "MessagesTable", messagesTableProps);

        // Create API Gateway REST API
        RestApi api = RestApi.Builder.create(this, "JournalAppApi")
                .restApiName("Journal App API")
                .description("REST API for Journal Application")
                .build();

        GatewayResponse.Builder.create(this, "MissingAuthTokenResponse")
                .restApi(api)
                .type(ResponseType.MISSING_AUTHENTICATION_TOKEN)
                .statusCode("404")
                .responseHeaders(Map.of("Access-Control-Allow-Origin", "'*'"))
                .templates(Map.of(
                        "application/json",
                        "{\"error\": \"Route not found. Check that clientId and therapistId are present in the path.\"}"
                ))
                .build();


        GatewayResponse.Builder.create(this, "AccessDeniedResponse")
                .restApi(api)
                .type(ResponseType.ACCESS_DENIED)
                .statusCode("403")
                .responseHeaders(Map.of("Access-Control-Allow-Origin", "'*'"))
                .templates(Map.of(
                        "application/json",
                        "{\"error\": \"Access denied.\"}"
                ))
                .build();

        // Create /mapping resource
        Resource mappingResource = api.getRoot().addResource("mapping");

        // Create /mapping/clients resource
        Resource clientsResource = mappingResource.addResource("clients");

        // Create /mapping/clients/{clientId} resource
        Resource clientIdResource = clientsResource.addResource("{clientId}");

        // Create /mapping/clients/{clientId}/therapists resource
        Resource clientTherapistsResource = clientIdResource.addResource("therapists");

        // Create /mapping/therapists resource
        Resource therapistsResource = mappingResource.addResource("therapists");

        // Create /mapping/therapists/{therapistId} resource
        Resource therapistIdResource = therapistsResource.addResource("{therapistId}");

        // Create /mapping/therapists/{therapistId}/clients resource
        Resource therapistClientsResource = therapistIdResource.addResource("clients");

        // Create /mapping/therapists/{therapistId}/clients/{clientId} resource
        Resource therapistClientIdResource = therapistClientsResource.addResource("{clientId}");

        // Create /mapping/clients/{clientId}/therapists/{therapistId} resource
        Resource clientTherapistIdResource = clientTherapistsResource.addResource("{therapistId}");

        // Environment variables for all Lambda functions
        Map<String, String> lambdaEnv = new HashMap<>();
        lambdaEnv.put("MAPPING_TABLE_NAME", mappingTable.getTableName());

        //  GetClientTherapistsHandler - GET /mapping/clients/{clientId}/therapists
        LambdaProps getClientTherapistsProps = new LambdaProps(
                "com.myorg.mapping.handler.GetClientTherapistsHandler",
                lambdaEnv
        );
        LambdaConstruct getClientTherapistsLambda = new LambdaConstruct(this, "GetClientTherapistsFunction", getClientTherapistsProps);
        mappingTable.getTable().grantReadData(getClientTherapistsLambda.getLambdaFunction());

        clientTherapistsResource.addMethod("GET",
                new LambdaIntegration(getClientTherapistsLambda.getLambdaFunction()));

        //  GetTherapistClientsHandler - GET /mapping/therapists/{therapistId}/clients
        LambdaProps getTherapistClientsProps = new LambdaProps(
                "com.myorg.mapping.handler.GetTherapistClientsHandler",
                lambdaEnv
        );
        LambdaConstruct getTherapistClientsLambda = new LambdaConstruct(this, "GetTherapistClientsFunction", getTherapistClientsProps);
        mappingTable.getTable().grantReadData(getTherapistClientsLambda.getLambdaFunction());

        therapistClientsResource.addMethod("GET",
                new LambdaIntegration(getTherapistClientsLambda.getLambdaFunction()));

        //  CreateClientTherapistMappingHandler - POST /mapping/clients/{clientId}/therapists/{therapistId}
        LambdaProps createClientTherapistProps = new LambdaProps(
                "com.myorg.mapping.handler.CreateClientTherapistMappingHandler",
                lambdaEnv
        );
        LambdaConstruct createClientTherapistLambda = new LambdaConstruct(this, "CreateClientTherapistMappingFunction", createClientTherapistProps);
        mappingTable.getTable().grantReadWriteData(createClientTherapistLambda.getLambdaFunction());

        clientTherapistIdResource.addMethod("POST",
                new LambdaIntegration(createClientTherapistLambda.getLambdaFunction()));

        //  GetMappingDetailsHandler - GET /mapping/clients/{clientId}/therapists/{therapistId}
        LambdaProps getMappingDetailsProps = new LambdaProps(
                "com.myorg.mapping.handler.GetMappingDetailsHandler",
                lambdaEnv
        );
        LambdaConstruct getMappingDetailsLambda = new LambdaConstruct(this, "GetMappingDetailsFunction", getMappingDetailsProps);
        mappingTable.getTable().grantReadData(getMappingDetailsLambda.getLambdaFunction());

        clientTherapistIdResource.addMethod("GET",
                new LambdaIntegration(getMappingDetailsLambda.getLambdaFunction()));

        //  UpdateMappingStatusHandler - PUT /mapping/clients/{clientId}/therapists/{therapistId}
        LambdaProps updateMappingStatusProps = new LambdaProps(
                "com.myorg.mapping.handler.UpdateMappingStatusHandler",
                lambdaEnv
        );
        LambdaConstruct updateMappingStatusLambda = new LambdaConstruct(this, "UpdateMappingStatusFunction", updateMappingStatusProps);
        mappingTable.getTable().grantReadWriteData(updateMappingStatusLambda.getLambdaFunction());

        clientTherapistIdResource.addMethod("PUT",
                new LambdaIntegration(updateMappingStatusLambda.getLambdaFunction()));

        //  DeleteClientTherapistMappingHandler - DELETE /mapping/clients/{clientId}/therapists/{therapistId}
        LambdaProps deleteClientTherapistProps = new LambdaProps(
                "com.myorg.mapping.handler.DeleteClientTherapistMappingHandler",
                lambdaEnv
        );
        LambdaConstruct deleteClientTherapistLambda = new LambdaConstruct(this, "DeleteClientTherapistFunction", deleteClientTherapistProps);
        mappingTable.getTable().grantReadWriteData(deleteClientTherapistLambda.getLambdaFunction());

        clientTherapistIdResource.addMethod("DELETE",
                new LambdaIntegration(deleteClientTherapistLambda.getLambdaFunction()));

        //  TherapistRequestClientMappingHandler - POST /mapping/therapists/{therapistId}/clients/{clientId}
        LambdaProps therapistRequestMappingProps = new LambdaProps(
                "com.myorg.mapping.handler.TherapistRequestClientMappingHandler",
                lambdaEnv
        );
        LambdaConstruct therapistRequestMappingLambda = new LambdaConstruct(this, "TherapistRequestMappingFunction", therapistRequestMappingProps);
        mappingTable.getTable().grantReadWriteData(therapistRequestMappingLambda.getLambdaFunction());

        therapistClientIdResource.addMethod("POST",
                new LambdaIntegration(therapistRequestMappingLambda.getLambdaFunction()));

        //  TherapistRemoveClientMappingHandler - DELETE /mapping/therapists/{therapistId}/clients/{clientId}
        LambdaProps therapistRemoveClientProps = new LambdaProps(
                "com.myorg.mapping.handler.TherapistRemoveClientMappingHandler",
                lambdaEnv
        );
        LambdaConstruct therapistRemoveClientLambda = new LambdaConstruct(this, "TherapistRemoveClientFunction", therapistRemoveClientProps);
        mappingTable.getTable().grantReadWriteData(therapistRemoveClientLambda.getLambdaFunction());

        therapistClientIdResource.addMethod("DELETE",
                new LambdaIntegration(therapistRemoveClientLambda.getLambdaFunction()));

        // ========== SESSION ENDPOINTS ==========

        // Create /session resource
        Resource sessionResource = api.getRoot().addResource("session");

        // Create /session/therapists resource
        Resource sessionTherapistsResource = sessionResource.addResource("therapists");

        // Create /session/therapists/{therapistId} resource
        Resource sessionTherapistIdResource = sessionTherapistsResource.addResource("{therapistId}");

        // Create /session/search resource (declared before {sessionId} so literal path takes priority)
        Resource sessionSearchResource = sessionResource.addResource("search");

        // Create /session/{sessionId} resource
        Resource sessionIdResource = sessionResource.addResource("{sessionId}");

        // Create /session/{sessionId}/therapists/{therapistId} resource
        Resource sessionIdTherapistResource = sessionIdResource.addResource("therapists");
        Resource sessionIdTherapistIdResource = sessionIdTherapistResource.addResource("{therapistId}");

        // Create /session/{sessionId}/clients/{clientId} resource
        Resource sessionIdClientsResource = sessionIdResource.addResource("clients");
        Resource sessionIdClientIdResource = sessionIdClientsResource.addResource("{clientId}");

        // Environment variables for Session Lambda functions
        Map<String, String> sessionLambdaEnv = new HashMap<>();
        sessionLambdaEnv.put("SESSION_TABLE_NAME", sessionsTable.getTableName());

        //  CreateSessionHandler - POST /session/therapists/{therapistId}
        LambdaProps createSessionProps = new LambdaProps(
                "com.myorg.session.handler.CreateSessionHandler",
                sessionLambdaEnv
        );
        LambdaConstruct createSessionLambda = new LambdaConstruct(this, "CreateSessionFunction", createSessionProps);
        sessionsTable.getTable().grantReadWriteData(createSessionLambda.getLambdaFunction());

        sessionTherapistIdResource.addMethod("POST",
                new LambdaIntegration(createSessionLambda.getLambdaFunction()));

        //  UpdateSessionHandler - PUT /session/{sessionId}/therapists/{therapistId}
        LambdaProps updateSessionProps = new LambdaProps(
                "com.myorg.session.handler.UpdateSessionHandler",
                sessionLambdaEnv
        );
        LambdaConstruct updateSessionLambda = new LambdaConstruct(this, "UpdateSessionFunction", updateSessionProps);
        sessionsTable.getTable().grantReadWriteData(updateSessionLambda.getLambdaFunction());

        sessionIdTherapistIdResource.addMethod("PUT",
                new LambdaIntegration(updateSessionLambda.getLambdaFunction()));

        //  GetAllAvailableSessionsHandler - GET /session
        LambdaProps getAllAvailableSessionsProps = new LambdaProps(
                "com.myorg.session.handler.GetAllAvailableSessionsHandler",
                sessionLambdaEnv
        );
        LambdaConstruct getAllAvailableSessionsLambda = new LambdaConstruct(this, "GetAllAvailableSessionsFunction", getAllAvailableSessionsProps);
        sessionsTable.getTable().grantReadData(getAllAvailableSessionsLambda.getLambdaFunction());

        sessionResource.addMethod("GET",
                new LambdaIntegration(getAllAvailableSessionsLambda.getLambdaFunction()));

        //  RequestSessionAppointmentHandler - PUT /session/{sessionId}/clients/{clientId}
        LambdaProps requestSessionAppointmentProps = new LambdaProps(
                "com.myorg.session.handler.RequestSessionAppointmentHandler",
                sessionLambdaEnv
        );
        LambdaConstruct requestSessionAppointmentLambda = new LambdaConstruct(this, "RequestSessionAppointmentFunction", requestSessionAppointmentProps);
        sessionsTable.getTable().grantReadWriteData(requestSessionAppointmentLambda.getLambdaFunction());

        sessionIdClientIdResource.addMethod("PUT",
                new LambdaIntegration(requestSessionAppointmentLambda.getLambdaFunction()));

        //  SearchSessionsByNotesHandler - GET /session/search
        LambdaProps searchSessionsByNotesProps = new LambdaProps(
                "com.myorg.session.handler.SearchSessionsByNotesHandler",
                sessionLambdaEnv
        );
        LambdaConstruct searchSessionsByNotesLambda = new LambdaConstruct(this, "SearchSessionsByNotesFunction", searchSessionsByNotesProps);
        sessionsTable.getTable().grantReadData(searchSessionsByNotesLambda.getLambdaFunction());

        sessionSearchResource.addMethod("GET",
                new LambdaIntegration(searchSessionsByNotesLambda.getLambdaFunction()));

        // ========== MESSAGE ENDPOINTS ==========

        // Create /messages resource
        Resource messagesResource = api.getRoot().addResource("messages");

        // Environment variables for Message Lambda functions
        Map<String, String> messageLambdaEnv = new HashMap<>();
        messageLambdaEnv.put("MESSAGE_TABLE_NAME", messagesTable.getTableName());

        //  SendMessageHandler - POST /messages
        LambdaProps sendMessageProps = new LambdaProps(
                "com.myorg.message.handler.SendMessageHandler",
                messageLambdaEnv
        );
        LambdaConstruct sendMessageLambda = new LambdaConstruct(this, "SendMessageFunction", sendMessageProps);
        messagesTable.getTable().grantWriteData(sendMessageLambda.getLambdaFunction());

        messagesResource.addMethod("POST",
                new LambdaIntegration(sendMessageLambda.getLambdaFunction()));

        //  GetMessageHistoryHandler - GET /messages
        LambdaProps getMessageHistoryProps = new LambdaProps(
                "com.myorg.message.handler.GetMessageHistoryHandler",
                messageLambdaEnv
        );
        LambdaConstruct getMessageHistoryLambda = new LambdaConstruct(this, "GetMessageHistoryFunction", getMessageHistoryProps);
        messagesTable.getTable().grantReadData(getMessageHistoryLambda.getLambdaFunction());

        messagesResource.addMethod("GET",
                new LambdaIntegration(getMessageHistoryLambda.getLambdaFunction()));
    }
}
