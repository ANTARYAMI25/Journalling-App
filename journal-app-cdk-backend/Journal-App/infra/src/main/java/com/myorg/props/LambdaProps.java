package com.myorg.props;

import java.util.Map;

public class LambdaProps {

    private final String handler;
    private final Map<String, String> environment;

    public LambdaProps(String handler, Map<String, String> environment) {
        this.handler = handler;
        this.environment = environment;
    }

    public String getHandler() {
        return handler;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }
}
