package com.example.testblefido2.webauthn.models;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import com.example.testblefido2.webauthn.exceptions.VirgilException;
import com.example.testblefido2.webauthn.fido.ctap2.CtapSuccessOutputStream;
import com.example.testblefido2.webauthn.fido.ctap2.Messages;

public final class DummyAttestation extends AttestationObject  {
    @Override
    public byte[] asCBOR() throws VirgilException {
        CtapSuccessOutputStream baos = new CtapSuccessOutputStream();
        try {
            new CborEncoder(baos).encode(new CborBuilder()
                    .addMap()
                    .put(Messages.MAKE_CREDENTIAL_RESPONSE_AUTH_DATA, new byte[37])
                    .put(Messages.MAKE_CREDENTIAL_RESPONSE_FMT, Messages.AttestationType.SELF.format)
                    .putMap(Messages.MAKE_CREDENTIAL_RESPONSE_ATT_STMT)
                    .put("alg", Messages.COSE_ID_ES256)
                    .put("sig", new byte[0])
                    .end()
                    .end()
                    .build()
            );
        } catch (CborException e) {
            throw new VirgilException("couldn't serialize to cbor", e);
        }

        return baos.toByteArray();
    }
}
