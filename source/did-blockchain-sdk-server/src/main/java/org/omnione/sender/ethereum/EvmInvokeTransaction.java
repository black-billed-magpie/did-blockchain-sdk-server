package org.omnione.sender.ethereum;

import java.math.BigInteger;
import java.util.List;
import org.omnione.generated.OpenDID;
import org.omnione.generated.OpenDID.VcMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;

public class EvmInvokeTransaction {

  private static final Logger logger = LoggerFactory.getLogger(EvmInvokeTransaction.class);

  private final EvmContractData evmContractData;

  public EvmInvokeTransaction(EvmContractData evmContractData) {
    this.evmContractData = evmContractData;
  }

  public TransactionReceipt send(Web3j web3j, BigInteger gasPrice, BigInteger gasLimit,
                                 ContractFunctionName contractFunctionName, List<Object> args
  ) throws Exception {

    logger.info(
        "Starting transaction with function: {}",
        contractFunctionName
    );
    logger.debug(
        "Gas price: {}, Gas limit: {}",
        gasPrice,
        gasLimit
    );
    logger.debug(
        "Arguments: {}",
        args
    );

    Credentials credentials = Credentials.create(evmContractData.getPrivateKey());
    logger.debug(
        "Loaded credentials for address: {}",
        credentials.getAddress()
    );

    var gasProvider = new StaticGasProvider(
        gasPrice,
        gasLimit
    );
    logger.debug(
        "Created gas provider with gas price: {} and gas limit: {}",
        gasPrice,
        gasLimit
    );

    var openDID = OpenDID.load(
        this.evmContractData.getContractAddress(),
        web3j,
        credentials,
        gasProvider
    );
    logger.info(
        "Loaded contract at address: {}",
        this.evmContractData.getContractAddress()
    );

    try {
      return switch (contractFunctionName) {
        case FUNC_REGIST_DID_DOCUMENT -> {
          logger.info("Calling FUNC_REGIST_DID_DOCUMENT");
          yield openDID.registDidDoc((OpenDID.Document) args.get(0))
              .send();
        }
        case FUNC_UPDATE_DID_DOC_STATUS_IN_SERVICE -> {
          logger.info("Calling FUNC_UPDATE_DID_DOC_STATUS_IN_SERVICE");
          yield openDID.updateDidDocStatusInService(
                  (String) args.get(0),
                  (String) args.get(1),
                  (String) args.get(2)
              )
              .send();
        }
        case FUNC_UPDATE_DID_DOC_STATUS_REVOCATION -> {
          logger.info("Calling FUNC_UPDATE_DID_DOC_STATUS_REVOCATION");
          yield openDID.updateDidDocStatusRevocation(
                  (String) args.get(0),
                  (String) args.get(1),
                  (String) args.get(2)
              )
              .send();
        }
        case FUNC_REGIST_VC_METADATA -> {
          logger.info("Calling FUNC_REGIST_VC_METADATA");
          yield openDID.registVcMetaData((VcMeta) args.get(0))
              .send();
        }
        default -> throw new IllegalArgumentException(
            "Unsupported contract function name: " + contractFunctionName);
      };
    } catch (Exception e) {
      logger.error(
          "Error occurred while sending transaction: {}",
          e.getMessage(),
          e
      );
      throw e;
    }
  }
}
