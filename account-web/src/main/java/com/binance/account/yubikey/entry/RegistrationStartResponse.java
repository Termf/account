package com.binance.account.yubikey.entry;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegistrationStartResponse implements Serializable {

    private static final long serialVersionUID = 408207523400265385L;

    private String requestId;

    private Long userId;

    private String origin;

    private String credentialNickname;

    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;
}
