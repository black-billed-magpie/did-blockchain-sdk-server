package org.omnione.did.ethereum.data;

import lombok.Getter;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

@Getter
public class CredentialSchema extends DynamicStruct {
  private final String url;
  private final String credentialSchemaType;


  public CredentialSchema(String url, String credentialSchemaType) {
    super(new Utf8String(url), new Utf8String(credentialSchemaType));
    this.url = url;
    this.credentialSchemaType = credentialSchemaType;
  }

  public CredentialSchema(
      @Parameterized(type = Utf8String.class) Utf8String url,
      @Parameterized(type = Utf8String.class) Utf8String credentialSchemaType
  ) {
    super(url, credentialSchemaType);
    this.url = url.getValue();
    this.credentialSchemaType = credentialSchemaType.getValue();
  }

}
