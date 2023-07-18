package com.binance.account.yubikey.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.Data;

import java.io.Serializable;

@Data
public class AssertionFinishRequest implements Serializable {

    private String requestId;

    private boolean deregister;

    private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;

    public AssertionFinishRequest(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("credential") PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential
    ) {
        this.requestId = requestId;
        this.credential = credential;
    }
}
