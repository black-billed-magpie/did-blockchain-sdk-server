package org.omnione.did.ethereum.data;

import lombok.Getter;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

@Getter
public class Provider extends DynamicStruct {

  private final String did;
  private final String certVcReference;

  public Provider(String did, String certVcReference) {
    super(
        new org.web3j.abi.datatypes.Utf8String(did),
        new org.web3j.abi.datatypes.Utf8String(certVcReference)
    );
    // Initialize the fields
    this.did = did;
    this.certVcReference = certVcReference;
  }

  public Provider(
      @Parameterized(type = Utf8String.class) Utf8String did,
      @Parameterized(type = Utf8String.class) Utf8String certVcReference
  ) {
    super(did, certVcReference);
    // Initialize the fields
    this.did = did.getValue();
    this.certVcReference = certVcReference.getValue();
  }

}
