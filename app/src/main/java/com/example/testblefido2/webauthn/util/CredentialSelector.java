package com.example.testblefido2.webauthn.util;

import java.util.List;

import com.example.testblefido2.webauthn.models.PublicKeyCredentialSource;

public interface CredentialSelector {
    PublicKeyCredentialSource selectFrom(List<PublicKeyCredentialSource> credentialList);
}
