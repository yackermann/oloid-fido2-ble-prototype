package com.example.testblefido2.webauthn.models;

import com.example.testblefido2.webauthn.exceptions.VirgilException;

public abstract class AuthenticatorResult {
    public abstract byte[] asCBOR() throws VirgilException;
}
