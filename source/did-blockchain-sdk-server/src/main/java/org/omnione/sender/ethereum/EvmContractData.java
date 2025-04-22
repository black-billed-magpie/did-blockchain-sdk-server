package org.omnione.sender.ethereum;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import lombok.Getter;
import org.omnione.sender.ContractData;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

@Getter
public class EvmContractData extends ContractData {

  private final String contractAddress;
  private final String privateKey;
  private ContractFunctionName functionName;
  private List<Type> inputParameters;
  private List<TypeReference<?>> outputParameters;
  private Boolean isView;

  public EvmContractData(String resourcePath) throws IOException {
    super();

    Properties properties = loadProperties(resourcePath);

    this.contractAddress = properties.getProperty("evm.contract.address");
    this.privateKey = properties.getProperty("evm.contract.privateKey");
  }

  public void setTransactionDetails(ContractFunctionName contractFunctionName,
                                    List<Type> inputParameters,
                                    List<TypeReference<?>> outputParameters
  ) {
    this.functionName = contractFunctionName;
    this.inputParameters = inputParameters;
    this.outputParameters = outputParameters;
    this.isView = contractFunctionName.getIsViewFunction();
  }
}
