package com.example.testblefido2.old.webauthn.models;

import com.example.testblefido2.old.webauthn.exceptions.VirgilException;

public abstract class AuthenticatorResult {
    public abstract byte[] asCBOR() throws VirgilException;
}
