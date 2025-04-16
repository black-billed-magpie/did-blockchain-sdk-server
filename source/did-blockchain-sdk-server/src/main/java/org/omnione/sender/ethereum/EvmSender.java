package org.omnione.sender.ethereum;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import lombok.NoArgsConstructor;
import org.omnione.exception.BlockChainException;
import org.omnione.exception.BlockchainErrorCode;
import org.omnione.sender.ContractData;
import org.omnione.sender.OpenDidSender;
import org.omnione.sender.ServerInformation;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

@NoArgsConstructor
public class EvmSender implements OpenDidSender {

  private static final Logger logger = Logger.getLogger(EvmSender.class.getName());

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

    validateInputParameters(serverInformation, data);

    var ethereumServerInformation = (EvmServerInformation) serverInformation;
    var ethereumContractData = (EvmContractData) data;

    try (Web3j web3j = createWeb3jClient(ethereumServerInformation)) {
      if (ethereumContractData.getIsView()) {
        logger.info("Executing view function...");
        return executeViewFunction(web3j, ethereumContractData);
      } else {
        logger.info("Executing transaction function...");
        return executeTransactionFunction(web3j, ethereumServerInformation, ethereumContractData);
      }
    } catch (Exception e) {
      logger.severe("Transaction process failed: " + e.getMessage());
      throw new BlockChainException(BlockchainErrorCode.TRANSACTION_ERROR, e);
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

  /**
   * Executes a view function on the Ethereum blockchain.
   *
   * @param web3j        the web3 client
   * @param contractData the contract data
   * @return the result of the view function as a byte array
   * @throws IOException if an error occurs during the function execution
   */
  private byte[] executeViewFunction(Web3j web3j, EvmContractData contractData) throws IOException {
    logger.info("Executing view function: " + contractData.getFunctionName());
    Function function = createFunction(contractData);
    String encodedFunction = FunctionEncoder.encode(function);

    var tx = Transaction.createEthCallTransaction(
        contractData.getContractAddress(), contractData.getContractAddress(), encodedFunction);
    EthCall response = web3j.ethCall(tx, DefaultBlockParameterName.LATEST)
        .send();

    if (response.isReverted()) {
      logger.warning("View function reverted. Reason: " + response.getRevertReason());
      return response.getRevertReason()
          .getBytes(StandardCharsets.UTF_8);
    }
    logger.info("View function executed successfully.");
    return response.getValue()
        .getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Executes a transaction function on the Ethereum blockchain.
   *
   * @param web3j             the web3 client
   * @param serverInformation the Ethereum server information
   * @param contractData      the contract data
   * @return the result of the transaction as a byte array
   * @throws Exception if an error occurs during the transaction
   */
  private byte[] executeTransactionFunction(
      Web3j web3j, EvmServerInformation serverInformation, EvmContractData contractData)
      throws Exception {
    logger.info("Starting transaction execution for function: " + contractData.getFunctionName());

    // Credentials 생성
    logger.info("Creating credentials from private key...");
    var credentials = Credentials.create(contractData.getPrivateKey());
    logger.info("Credentials created for address: " + credentials.getAddress());

    // GasProvider 설정
    logger.info("Setting up gas provider with gas price: " + serverInformation.getGasPrice() +
                " and gas limit: " + serverInformation.getGasLimit());
    var gasProvider = new StaticGasProvider(
        BigInteger.valueOf(serverInformation.getGasPrice()),
        BigInteger.valueOf(serverInformation.getGasLimit())
    );

    // Function 생성
    logger.info("Creating function object for contract interaction...");
    Function function = createFunction(contractData);
    logger.info("Function object created: " + function.getName());
    String encodedFunction = FunctionEncoder.encode(function);
    logger.info("Encoded function: " + encodedFunction);

    // Nonce 가져오기
    logger.info("Retrieving nonce for address: " + credentials.getAddress());
    BigInteger nonce = getAvailableNonce(web3j, credentials.getAddress());
    logger.info("Nonce retrieved: " + nonce);

    // Chain ID 가져오기
    logger.info("Retrieving chain ID from the Ethereum network...");
    long chainId = web3j.ethChainId()
        .send()
        .getChainId()
        .longValue();
    logger.info("Chain ID retrieved: " + chainId);

    // RawTransaction 생성
    logger.info("Creating raw transaction...");
    var rawTransaction = RawTransaction.createTransaction(
        chainId, nonce, gasProvider.getGasLimit(), contractData.getContractAddress(),
        BigInteger.ZERO, encodedFunction, BigInteger.ZERO, BigInteger.ZERO
    );
    logger.info("Raw transaction created successfully.");

    // 트랜잭션 서명
    logger.info("Signing the transaction...");
    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
    String hexValue = Numeric.toHexString(signedMessage);
    logger.info("Transaction signed. Hex value: " + hexValue);

    // 트랜잭션 전송
    logger.info("Sending the transaction to the Ethereum network...");
    EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue)
        .sendAsync()
        .get();

    // 트랜잭션 결과 확인
    if (transactionResponse.hasError()) {
      logger.severe("Transaction failed with error: " + transactionResponse.getError()
          .getMessage());
      throw new Exception("Transaction failed: " + transactionResponse.getError()
          .getMessage());
    }

    logger.info("Transaction executed successfully. Transaction hash: " +
                transactionResponse.getTransactionHash());
    logger.info("Transaction response raw data: " + transactionResponse.getRawResponse());

    return transactionResponse.getResult()
        .getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Creates a function object for the Ethereum transaction.
   *
   * @param contractData the contract data
   * @return the function object
   */
  private Function createFunction(EvmContractData contractData) {
    logger.info("Creating function object for: " + contractData.getFunctionName());

    if (contractData.getInputParameters() == null || contractData.getOutputParameters() == null) {
      throw new IllegalArgumentException("Input and output parameters cannot be null.");
    }

    return new Function(
        contractData.getFunctionName(), contractData.getInputParameters(),
        contractData.getOutputParameters()
    );
  }

  /**
   * Retrieves the available nonce for the given Ethereum address.
   *
   * @param web3j   the web3 client
   * @param address the Ethereum address
   * @return the available nonce
   * @throws IOException          if an error occurs while retrieving the nonce
   * @throws TransactionException if the nonce retrieval fails
   */
  private BigInteger getAvailableNonce(Web3j web3j, String address)
      throws IOException, TransactionException {
    logger.info("Retrieving available nonce for address: " + address);
    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
            address, DefaultBlockParameterName.LATEST)
        .send();

    if (ethGetTransactionCount.hasError()) {
      logger.severe("Failed to retrieve nonce: " + ethGetTransactionCount.getError()
          .getMessage());
      throw new TransactionException("Cannot get nonce: " + ethGetTransactionCount.getError()
          .getMessage());
    }

    logger.info("Nonce retrieved successfully: " + ethGetTransactionCount.getTransactionCount());
    return ethGetTransactionCount.getTransactionCount();
  }
}