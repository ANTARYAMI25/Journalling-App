package com.myorg.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class JournalAppApp {
    public static void main(final String[] args) {
        App app = new App();

        new JournalAppStack(app, "JournalAppStack", StackProps.builder()
                .env(Environment.builder()
                        .account("843302972701")
                        .region("ap-south-1")
                        .build())
                .build());

        app.synth();
    }
}

