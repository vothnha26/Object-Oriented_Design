package com.alotra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SepayConfig {

    @Value("${sepay.apiKey:}")
    private String apiKey;

    @Value("${sepay.secret:}")
    private String secret;

    @Value("${sepay.apiBaseUrl:https://api.sepay.vn/}")
    private String apiBaseUrl;

    @Value("${sepay.publicBaseUrl:}")
    private String publicBaseUrl;

    @Value("${sepay.callbackPath:/api/payments/webhook}")
    private String callbackPath;

    @Value("${sepay.webhookSecret:}")
    private String webhookSecret;

    // VietQR configuration for bank transfer QR codes
    @Value("${payment.settle.bank-code:VCB}")
    private String bankCode;

    @Value("${payment.settle.account:}")
    private String bankAccount;

    @Value("${payment.settle.account-name:}")
    private String bankAccountName;

    public String getApiKey() { return apiKey; }
    public String getSecret() { return secret; }
    public String getApiBaseUrl() { return apiBaseUrl; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public String getCallbackPath() { return callbackPath; }
    public String getWebhookSecret() { return webhookSecret; }
    public String getBankCode() { return bankCode; }
    public String getBankAccount() { return bankAccount; }
    public String getBankAccountName() { return bankAccountName; }
}
