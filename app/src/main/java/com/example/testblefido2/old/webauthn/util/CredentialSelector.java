package com.example.testblefido2.old.webauthn.util;

import java.util.List;

import com.example.testblefido2.old.webauthn.models.PublicKeyCredentialSource;

public interface CredentialSelector {
    PublicKeyCredentialSource selectFrom(List<PublicKeyCredentialSource> credentialList);
}
