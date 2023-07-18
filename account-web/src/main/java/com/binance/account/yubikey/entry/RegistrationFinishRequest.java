package com.binance.account.yubikey.entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegistrationFinishRequest implements Serializable {

    private static final long serialVersionUID = -288905831284598872L;

    private String requestId;

    private final PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;

    @JsonCreator
    public RegistrationFinishRequest(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("credential") PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential
    ) {
        this.requestId = requestId;
        this.credential = credential;
    }
}
