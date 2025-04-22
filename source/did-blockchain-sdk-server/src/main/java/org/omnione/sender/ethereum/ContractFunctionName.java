package org.omnione.sender.ethereum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractFunctionName {

  FUNC_REGIST_DID_DOCUMENT(
      "registDidDoc",
      false
  ), FUNC_UPDATE_DID_DOC_STATUS_IN_SERVICE(
      "updateDidDocStatusInService",
      false
  ), FUNC_UPDATE_DID_DOC_STATUS_REVOCATION(
      "updateDidDocStatusRevocation",
      false
  ), FUNC_GET_DOCUMENT(
      "getDidDoc",
      true
  ), FUNC_REGIST_VC_METADATA(
      "registVcMetaData",
      false
  ), FUNC_GET_VC_METADATA(
      "getVcmetaData",
      true
  ), FUNC_UPDATE_VC_STATS(
      "updateVcStats",
      false
  );

  private final String functionName;
  private final Boolean isViewFunction;

}
