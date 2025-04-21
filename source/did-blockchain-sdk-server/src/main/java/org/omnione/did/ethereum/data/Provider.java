package org.omnione.did.ethereum.data;

import lombok.Getter;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;

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

  public Provider(Utf8String did, Utf8String certVcReference) {
    super(did, certVcReference);
    // Initialize the fields
    this.did = did.getValue();
    this.certVcReference = certVcReference.getValue();
  }

}
