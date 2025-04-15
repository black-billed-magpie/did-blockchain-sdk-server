package org.omnione.did.ethereum.data;

import java.math.BigInteger;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint8;

public class VerificationMethod extends DynamicStruct {

  private final String id;
  private final BigInteger keyType;
  private final String controller;
  private final String publicKeyMultibase;
  private final BigInteger authType;

  public VerificationMethod(
      String id, BigInteger keyType, String controller, String publicKeyMultibase,
      BigInteger authType
  ) {
    super(
        new org.web3j.abi.datatypes.Utf8String(id),
        new org.web3j.abi.datatypes.generated.Uint8(keyType),
        new org.web3j.abi.datatypes.Utf8String(controller),
        new org.web3j.abi.datatypes.Utf8String(publicKeyMultibase),
        new org.web3j.abi.datatypes.generated.Uint8(authType)
    );

    this.id = id;
    this.keyType = keyType;
    this.controller = controller;
    this.publicKeyMultibase = publicKeyMultibase;
    this.authType = authType;
  }

  public VerificationMethod(
      Utf8String id, Uint8 KeyType, Utf8String controller, Utf8String publicKeyMultibase,
      Uint8 authType
  ) {
    super(id, KeyType, controller, publicKeyMultibase, authType);

    this.id = id.getValue();
    this.keyType = KeyType.getValue();
    this.controller = controller.getValue();
    this.publicKeyMultibase = publicKeyMultibase.getValue();
    this.authType = authType.getValue();
  }
}
