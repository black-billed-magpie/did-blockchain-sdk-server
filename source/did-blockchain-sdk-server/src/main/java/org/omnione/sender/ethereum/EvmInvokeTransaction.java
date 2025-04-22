package org.omnione.sender.ethereum;

import java.math.BigInteger;
import java.util.List;
import org.omnione.generated.OpenDID;
import org.omnione.generated.OpenDID.Document;
import org.omnione.generated.OpenDID.VcMeta;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.StaticGasProvider;

public class EvmInvokeTransaction implements EvmTransaction {

  private final EvmContractData evmContractData;

  public EvmInvokeTransaction(EvmContractData evmContractData
  ) {
    this.evmContractData = evmContractData;
  }

  @Override
  public Object send(Web3j web3j, BigInteger gasPrice, BigInteger gasLimit,
                     ContractFunctionName contractFunctionName, List<Type> args
  ) throws Exception {

    Credentials credentials = Credentials.create(evmContractData.getPrivateKey());
    var gasProvider = new StaticGasProvider(
        gasPrice,
        gasLimit
    );
    var openDID = OpenDID.load(
        this.evmContractData.getContractAddress(),
        web3j,
        credentials,
        gasProvider
    );

    return switch (contractFunctionName) {
      case FUNC_REGIST_DID_DOCUMENT -> openDID.registDidDoc((Document) args.get(0)
              .getValue())
          .send();
      case FUNC_UPDATE_DID_DOC_STATUS_IN_SERVICE -> openDID.updateDidDocStatusInService(
              (String) args.get(0)
                  .getValue(),
              (String) args.get(1)
                  .getValue(),
              (String) args.get(2)
                  .getValue()
          )
          .send();
      case FUNC_UPDATE_DID_DOC_STATUS_REVOCATION -> openDID.updateDidDocStatusRevocation(
              (String) args.get(0)
                  .getValue(),
              (String) args.get(1)
                  .getValue(),
              (String) args.get(2)
                  .getValue()
          )
          .send();
      case FUNC_REGIST_VC_METADATA -> openDID.registVcMetaData((VcMeta) args.get(0)
              .getValue())
          .send();

      default -> throw new IllegalArgumentException(
          "Unsupported contract function name: " + contractFunctionName);
    };
  }
}
