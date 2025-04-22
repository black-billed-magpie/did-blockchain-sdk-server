package org.omnione.sender.ethereum;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import lombok.NoArgsConstructor;
import org.omnione.exception.BlockChainException;
import org.omnione.exception.BlockchainErrorCode;
import org.omnione.sender.ContractData;
import org.omnione.sender.OpenDidSender;
import org.omnione.sender.ServerInformation;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@NoArgsConstructor
public class EvmSender implements OpenDidSender {

  private static final Logger logger = Logger.getLogger(EvmSender.class.getName());
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  /**
   * Sends a transaction to the Ethereum blockchain.
   *
   * @param serverInformation the server information containing network details
   * @param data              the contract data to be sent
   * @return the result of the transaction as a byte array
   * @throws BlockChainException if an error occurs during the transaction
   */
  @Override
  public byte[] sendTransaction(ServerInformation serverInformation, ContractData data)
      throws BlockChainException {
    logger.info("Starting transaction process...");

    validateInputParameters(
        serverInformation,
        data
    );

    var ethereumServerInformation = (EvmServerInformation) serverInformation;
    var ethereumContractData = (EvmContractData) data;

    try (Web3j web3j = createWeb3jClient(ethereumServerInformation)) {

      if (ethereumContractData.getIsView()) {
        logger.info("Executing view function...");
        EvmReadOnlyTransaction evmReadOnlyTransaction = new EvmReadOnlyTransaction(
            ethereumServerInformation,
            ethereumContractData
        );
        DynamicStruct response = evmReadOnlyTransaction.send(
            web3j,
            BigInteger.valueOf(ethereumServerInformation.getGasPrice()),
            BigInteger.valueOf(ethereumServerInformation.getGasLimit()),
            ethereumContractData.getFunctionName(),
            ethereumContractData.getInputParameters()
        );

        return response.getValue()
            .get(0)
            .getValue()
            .toString()
            .getBytes(CHARSET);

      } else {
        logger.info("Executing transaction function...");
        EvmInvokeTransaction evmInvokeTransaction = new EvmInvokeTransaction(ethereumContractData);
        Object response = evmInvokeTransaction.send(
            web3j,
            BigInteger.valueOf(ethereumServerInformation.getGasPrice()),
            BigInteger.valueOf(ethereumServerInformation.getGasLimit()),
            ethereumContractData.getFunctionName(),
            ethereumContractData.getInputParameters()
        );

        return response.toString()
            .getBytes(CHARSET);
      }
    } catch (Exception e) {
      logger.severe("Transaction process failed: " + e.getMessage());
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          e
      );
    }
  }


  /**
   * Validates the input parameters for the transaction.
   *
   * @param serverInformation the server information
   * @param data              the contract data
   * @throws IllegalArgumentException if the input parameters are invalid
   */
  private void validateInputParameters(ServerInformation serverInformation, ContractData data)
      throws IllegalArgumentException {
    logger.info("Validating input parameters...");
    if (!(serverInformation instanceof EvmServerInformation)) {
      throw new IllegalArgumentException("ServerInformation must be of type EvmServerInformation.");
    }
    if (!(data instanceof EvmContractData)) {
      throw new IllegalArgumentException("ContractData must be of type EvmContractData.");
    }
  }

  /**
   * Creates a web3 client for interacting with the Ethereum network.
   *
   * @param ethInfo the Ethereum server information
   * @return a Web3j client instance
   */
  private Web3j createWeb3jClient(EvmServerInformation ethInfo) {
    logger.info("Creating Web3j client for network URL: " + ethInfo.getNetworkURL());
    return Web3j.build(new HttpService(ethInfo.getNetworkURL()));
  }

  //  /**
  //   * Executes a view function on the Ethereum blockchain.
  //   *
  //   * @param web3j        the web3 client
  //   * @param contractData the contract data
  //   * @return the result of the view function as a byte array
  //   * @throws IOException if an error occurs during the function execution
  //   */
  //  private byte[] executeViewFunction(Web3j web3j, EvmServerInformation serverInformation,
  //                                     EvmContractData contractData
  //  ) throws IOException {
  //    logger.info("Executing view function: " + contractData.getFunctionName());
  //    Function function = createFunction(contractData);
  //    String encodedFunction = FunctionEncoder.encode(function);
  //
  //    ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(
  //        web3j,
  //        contractData.getContractAddress()
  //    );
  //
  //    var gasProvider = new StaticGasProvider(
  //        BigInteger.valueOf(serverInformation.getGasPrice()),
  //        BigInteger.valueOf(serverInformation.getGasLimit())
  //    );
  //
  //    var tx = Transaction.createEthCallTransaction(
  //        contractData.getContractAddress(),
  //        contractData.getContractAddress(),
  //        encodedFunction
  //    );
  //    EthCall response = web3j.ethCall(
  //            tx,
  //            DefaultBlockParameterName.LATEST
  //        )
  //        .send();
  //
  //    if (response.isReverted()) {
  //      String revertReason = response.getRevertReason();
  //      if (revertReason == null) {
  //        logger.warning("View function reverted without a reason.");
  //        return "View function reverted without a reason.".getBytes(CHARSET);
  //      }
  //      logger.warning("View function reverted. Reason: " + revertReason);
  //      return revertReason.getBytes(CHARSET);
  //    }
  //    logger.info("View function executed successfully.");
  //    return response.getValue()
  //        .getBytes(CHARSET);
  //  }

  //  /**
  //   * Executes a transaction function on the Ethereum blockchain.
  //   *
  //   * @param web3j             the web3 client
  //   * @param serverInformation the Ethereum server information
  //   * @param contractData      the contract data
  //   * @return the result of the transaction as a byte array
  //   * @throws Exception if an error occurs during the transaction
  //   */
  //  private byte[] executeTransactionFunction(Web3j web3j, EvmServerInformation serverInformation,
  //                                            EvmContractData contractData
  //  ) throws Exception {
  //    logger.info("Starting transaction execution for function: " + contractData.getFunctionName());
  //
  //    // Credentials 생성
  //    logger.info("Creating credentials from private key...");
  //    var credentials = Credentials.create(contractData.getPrivateKey());
  //    logger.info("Credentials created for address: " + credentials.getAddress());
  //
  //    // GasProvider 설정
  //    logger.info("Setting up gas provider with gas price: " + serverInformation.getGasPrice() +
  //                " and gas limit: " + serverInformation.getGasLimit());
  //    var gasProvider = new StaticGasProvider(
  //        BigInteger.valueOf(serverInformation.getGasPrice()),
  //        BigInteger.valueOf(serverInformation.getGasLimit())
  //    );
  //
  //    // Function 생성
  //    logger.info("Creating function object for contract interaction...");
  //    Function function = createFunction(contractData);
  //    logger.info("Function object created: " + function.getName());
  //    String encodedFunction = FunctionEncoder.encode(function);
  //    logger.info("Encoded function: " + encodedFunction);
  //
  //    // Nonce 가져오기
  //    logger.info("Retrieving nonce for address: " + credentials.getAddress());
  //    BigInteger nonce = getAvailableNonce(
  //        web3j,
  //        credentials.getAddress()
  //    );
  //    logger.info("Nonce retrieved: " + nonce);
  //
  //    // Chain ID 가져오기
  //    logger.info("Retrieving chain ID from the Ethereum network...");
  //    long chainId = web3j.ethChainId()
  //        .send()
  //        .getChainId()
  //        .longValue();
  //    logger.info("Chain ID retrieved: " + chainId);
  //
  //    // RawTransaction 생성
  //    logger.info("Creating raw transaction...");
  //    var rawTransaction = RawTransaction.createTransaction(
  //        chainId,
  //        nonce,
  //        gasProvider.getGasLimit(),
  //        contractData.getContractAddress(),
  //        BigInteger.ZERO,
  //        encodedFunction,
  //        BigInteger.ZERO,
  //        BigInteger.ZERO
  //    );
  //    logger.info("Raw transaction created successfully.");
  //
  //    // 트랜잭션 서명
  //    logger.info("Signing the transaction...");
  //    byte[] signedMessage = TransactionEncoder.signMessage(
  //        rawTransaction,
  //        chainId,
  //        credentials
  //    );
  //    String hexValue = Numeric.toHexString(signedMessage);
  //    logger.info("Transaction signed. Hex value: " + hexValue);
  //
  //    // 트랜잭션 전송
  //    logger.info("Sending the transaction to the Ethereum network...");
  //    EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue)
  //        .sendAsync()
  //        .get();
  //
  //    // 트랜잭션 결과 확인
  //    if (transactionResponse.hasError()) {
  //      logger.severe("Transaction failed with error: " + transactionResponse.getError()
  //          .getMessage());
  //      throw new Exception("Transaction failed: " + transactionResponse.getError()
  //          .getMessage());
  //    }
  //
  //    logger.info("Transaction executed successfully. Transaction hash: " +
  //                transactionResponse.getTransactionHash());
  //    logger.info("Transaction response raw data: " + transactionResponse.getRawResponse());
  //
  //    return transactionResponse.getResult()
  //        .getBytes(CHARSET);
  //  }

  //  /**
  //   * Creates a function object for the Ethereum transaction.
  //   *
  //   * @param contractData the contract data
  //   * @return the function object
  //   */
  //  private Function createFunction(EvmContractData contractData) {
  //    logger.info("Creating function object for: " + contractData.getFunctionName());
  //
  //    if (contractData.getInputParameters() == null || contractData.getOutputParameters() == null) {
  //      throw new IllegalArgumentException("Input and output parameters cannot be null.");
  //    }
  //
  //    return new Function(
  //        contractData.getFunctionName()
  //            .getFunctionName(),
  //        contractData.getInputParameters(),
  //        contractData.getOutputParameters()
  //    );
  //  }

  //  /**
  //   * Retrieves the available nonce for the given Ethereum address.
  //   *
  //   * @param web3j   the web3 client
  //   * @param address the Ethereum address
  //   * @return the available nonce
  //   * @throws IOException          if an error occurs while retrieving the nonce
  //   * @throws TransactionException if the nonce retrieval fails
  //   */
  //  private BigInteger getAvailableNonce(Web3j web3j, String address)
  //      throws IOException, TransactionException {
  //    logger.info("Retrieving available nonce for address: " + address);
  //    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
  //            address,
  //            DefaultBlockParameterName.LATEST
  //        )
  //        .send();
  //
  //    if (ethGetTransactionCount.hasError()) {
  //      logger.severe("Failed to retrieve nonce: " + ethGetTransactionCount.getError()
  //          .getMessage());
  //      throw new TransactionException("Cannot get nonce: " + ethGetTransactionCount.getError()
  //          .getMessage());
  //    }
  //
  //    logger.info("Nonce retrieved successfully: " + ethGetTransactionCount.getTransactionCount());
  //    return ethGetTransactionCount.getTransactionCount();
  //  }
  //}
}
