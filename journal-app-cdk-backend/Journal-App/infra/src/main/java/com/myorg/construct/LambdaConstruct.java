package com.myorg.construct;

import com.myorg.props.LambdaProps;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.Duration;
import software.constructs.Construct;

public class LambdaConstruct extends Construct {

    private final Function lambdaFunction;

    public LambdaConstruct(Construct scope, String id, LambdaProps props) {
        super(scope, id);

        this.lambdaFunction = Function.Builder.create(this, "Lambda")
                .runtime(Runtime.JAVA_17)
                .handler(props.getHandler())
                .code(Code.fromAsset("application/target/journal-app-application-0.1.jar"))
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .environment(props.getEnvironment())
                .build();
    }

    public Function getLambdaFunction() {
        return lambdaFunction;
    }
}



