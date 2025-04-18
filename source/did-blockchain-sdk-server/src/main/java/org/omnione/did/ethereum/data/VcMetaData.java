package org.omnione.did.ethereum.data;

import lombok.Getter;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

@Getter
public class VcMetaData extends DynamicStruct {

  private final String id;
  private final Provider issuer;
  private final String subject;
  private final CredentialSchema credentialSchema;
  private final String status;
  private final String issuanceDate;
  private final String validFrom;
  private final String validUntil;
  private final String formatVersion;
  private final String language;

  public VcMetaData(
      String id, Provider issuer, String subject, CredentialSchema credentialSchema, String status,
      String issuanceDate, String validFrom, String validUntil, String formatVersion,
      String language
  ) {
    super(
        new Utf8String(id), new DynamicStruct(issuer), new Utf8String(subject),
        new DynamicStruct(credentialSchema), new Utf8String(status), new Utf8String(issuanceDate),
        new Utf8String(validFrom), new Utf8String(validUntil), new Utf8String(formatVersion),
        new Utf8String(language)
    );

    this.id = id;
    this.issuer = issuer;
    this.subject = subject;
    this.credentialSchema = credentialSchema;
    this.status = status;
    this.issuanceDate = issuanceDate;
    this.validFrom = validFrom;
    this.validUntil = validUntil;
    this.formatVersion = formatVersion;
    this.language = language;
  }

  public VcMetaData(
      @Parameterized(type = Utf8String.class) Utf8String id,
      @Parameterized(type = Provider.class) Provider issuer,
      @Parameterized(type = Utf8String.class) Utf8String subject,
      @Parameterized(type = CredentialSchema.class) CredentialSchema credentialSchema,
      @Parameterized(type = Utf8String.class) Utf8String status,
      @Parameterized(type = Utf8String.class) Utf8String issuanceDate,
      @Parameterized(type = Utf8String.class) Utf8String validFrom,
      @Parameterized(type = Utf8String.class) Utf8String validUntil,
      @Parameterized(type = Utf8String.class) Utf8String formatVersion,
      @Parameterized(type = Utf8String.class) Utf8String language
  ) {
    super(
        id, new DynamicStruct(issuer), subject, new DynamicStruct(credentialSchema), status,
        issuanceDate, validFrom, validUntil, formatVersion, language
    );

    this.id = id.getValue();
    this.issuer = issuer;
    this.subject = subject.getValue();
    this.credentialSchema = credentialSchema;
    this.status = status.getValue();
    this.issuanceDate = issuanceDate.getValue();
    this.validFrom = validFrom.getValue();
    this.validUntil = validUntil.getValue();
    this.formatVersion = formatVersion.getValue();
    this.language = language.getValue();
  }
}
