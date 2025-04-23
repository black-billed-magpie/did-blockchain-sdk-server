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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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
        EvmReadOnlyTransaction evmReadOnlyTransaction =
            new EvmReadOnlyTransaction(ethereumContractData);
        String response = evmReadOnlyTransaction.send(
            web3j,
            BigInteger.valueOf(ethereumServerInformation.getGasPrice()),
            BigInteger.valueOf(ethereumServerInformation.getGasLimit()),
            ethereumContractData.getFunctionName(),
            ethereumContractData.getInputParameters()
        );

        return response.getBytes(CHARSET);

      } else {
        logger.info("Executing transaction function...");
        EvmInvokeTransaction evmInvokeTransaction = new EvmInvokeTransaction(ethereumContractData);
        logger.info("Sending transaction...");

        TransactionReceipt response = evmInvokeTransaction.send(
            web3j,
            BigInteger.valueOf(ethereumServerInformation.getGasPrice()),
            BigInteger.valueOf(ethereumServerInformation.getGasLimit()),
            ethereumContractData.getFunctionName(),
            ethereumContractData.getInputParameters()
        );
        logger.info("Transaction sent successfully.");
        if (response.isStatusOK()) {
          logger.info("Transaction executed successfully. Transaction hash: " +
                      response.getTransactionHash());
          return response.getStatus()
              .getBytes(CHARSET);
        } else {
          logger.warning("Transaction failed with status: " + response.getStatus());
          return response.getStatus()
              .getBytes(CHARSET);
        }
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
}
