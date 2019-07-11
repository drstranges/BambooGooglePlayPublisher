package com.drextended.gppublisher.bamboo.util;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

import java.io.IOException;

public class RequestInitializer implements HttpRequestInitializer {

    private final HttpRequestInitializer initializer;

    public RequestInitializer(HttpRequestInitializer credential) {
        this.initializer = credential;
    }

    public void initialize(HttpRequest httpRequest) throws IOException {
        initializer.initialize(httpRequest);
        httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
        httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
    }
}
