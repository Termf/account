package com.binance.account.yubikey.entry;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
public class AuthenticateStartResponse implements Serializable {

    private static final long serialVersionUID = -7192787767035891847L;

    private String requestId;

    private Long userId;

    private String origin;

    private PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions;

    private AssertionRequest request;
}
