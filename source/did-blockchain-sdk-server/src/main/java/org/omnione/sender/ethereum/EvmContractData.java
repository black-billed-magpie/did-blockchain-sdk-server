package org.omnione.sender.ethereum;

import lombok.Builder;
import lombok.Getter;
import org.omnione.sender.ContractData;

@Getter
@Builder
public class EvmContractData extends ContractData {

  private String contractAddress;
  private String privateKey;
  private String functionName;
  private Object[] parameters;
  private String ethValue;
  private String data;
}
