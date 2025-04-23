package org.omnione.sender.ethereum;

import java.math.BigInteger;
import java.util.List;
import org.omnione.generated.OpenDID;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

public class EvmReadOnlyTransaction {

  private final EvmContractData evmContractData;

  public EvmReadOnlyTransaction(EvmContractData evmContractData) {
    this.evmContractData = evmContractData;
  }

  public String send(Web3j web3j, BigInteger gasPrice, BigInteger gasLimit,
                     ContractFunctionName contractFunctionName, List<Object> args
  ) throws Exception {

    Credentials credentials = Credentials.create(this.evmContractData.getPrivateKey());

    var transactionManager = new ReadonlyTransactionManager(
        web3j,
        credentials.getAddress()
    );


    var gasProvider = new StaticGasProvider(
        gasPrice,
        gasLimit
    );

    var openDID = OpenDID.load(
        this.evmContractData.getContractAddress(),
        web3j,
        transactionManager,
        gasProvider
    );

    return switch (contractFunctionName) {
      case FUNC_GET_DOCUMENT -> openDID.getDidDoc((String) args.get(0))
          .send();
      default -> throw new IllegalArgumentException(
          "Unsupported contract function name: " + contractFunctionName);
    };
  }

}
