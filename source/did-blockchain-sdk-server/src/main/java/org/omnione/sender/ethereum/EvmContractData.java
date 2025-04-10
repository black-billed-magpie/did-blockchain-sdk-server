package org.omnione.sender.ethereum;

import java.io.IOException;
import java.util.Properties;
import lombok.Getter;
import org.omnione.sender.ContractData;

@Getter
public class EvmContractData extends ContractData {

  private final String contractAddress;
  private final String privateKey;
  private String functionName;
  private Object[] parameters;
  private String ethValue;
  private String data;

  public EvmContractData(String resourcePath) throws IOException {
    super();

    Properties properties = loadProperties(resourcePath);

    this.contractAddress = properties.getProperty("evm.contract.address");
    this.privateKey = properties.getProperty("evm.contract.privateKey");
  }

  public void setTransactionDetails(
      String functionName, Object[] parameters, String ethValue, String data) {
    this.functionName = functionName;
    this.parameters = parameters;
    this.ethValue = ethValue;
    this.data = data;
  }
}
