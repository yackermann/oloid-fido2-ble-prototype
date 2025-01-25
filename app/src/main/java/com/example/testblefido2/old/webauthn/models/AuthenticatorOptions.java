package com.example.testblefido2.old.webauthn.models;

import co.nstant.in.cbor.model.Map;
import com.example.testblefido2.old.webauthn.fido.ctap2.Messages.RequestCommandCTAP2;
import com.example.testblefido2.old.webauthn.exceptions.CtapException;

public abstract class AuthenticatorOptions {
    public abstract AuthenticatorOptions fromCBor(Map inputMap);
    public abstract void areWellFormed() throws CtapException;
    public final RequestCommandCTAP2 action;

    public AuthenticatorOptions(RequestCommandCTAP2 action) {
        this.action = action;
    }
}
