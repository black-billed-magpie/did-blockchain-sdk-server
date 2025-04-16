package org.omnione.sender.ethereum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractFunctionName {

  FUNC_REGISTDIDDOC("registDidDoc", false), FUNC_GET_DOCUMENT("getDidDoc", true);

  private final String functionName;
  private final Boolean isViewFunction;

}
