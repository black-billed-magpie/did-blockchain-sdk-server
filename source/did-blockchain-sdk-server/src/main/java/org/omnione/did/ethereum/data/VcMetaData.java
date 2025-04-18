package org.omnione.did.ethereum.data;

import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

public class VcMetaData extends DynamicStruct {

  private final String id;
  private final Provider issuer;
  private final String subject;

  public VcMetaData(String id, Provider issuer, String subject) {
    super(new Utf8String(id), new DynamicStruct(issuer), new Utf8String(subject));

    this.id = id;
    this.issuer = issuer;
    this.subject = subject;
  }

  public VcMetaData(
      @Parameterized(type = Utf8String.class) Utf8String id,
      @Parameterized(type = Provider.class) Provider issuer,
      @Parameterized(type = Utf8String.class) Utf8String subject
  ) {
    super(id, new DynamicStruct(issuer), subject);

    this.id = id.getValue();
    this.issuer = issuer;
    this.subject = subject.getValue();
  }
}
