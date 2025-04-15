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
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

@NoArgsConstructor
public class EvmSender implements OpenDidSender {

  private static final Logger logger = Logger.getLogger(EvmSender.class.getName());

  @Override
  public byte[] sendTransaction(ServerInformation serverInformation, ContractData data)
      throws BlockChainException {

    validateInputParameters(serverInformation, data);

    var ethereumServerInformation = (EvmServerInformation) serverInformation;
    var ethereumContractData = (EvmContractData) data;

    try (Web3j web3j = createWeb3jClient(ethereumServerInformation)) {
      var credentials = Credentials.create(ethereumContractData.getPrivateKey());
      var gasProvider = new StaticGasProvider(
          BigInteger.valueOf(ethereumServerInformation.getGasPrice()),
          BigInteger.valueOf(ethereumServerInformation.getGasLimit())
      );
      var transactionManager = new FastRawTransactionManager(
          web3j, credentials, new PollingTransactionReceiptProcessor(web3j, 1000, 15));

      Function function = new Function(
          ethereumContractData.getFunctionName(), ethereumContractData.getInputParameters(),
          ethereumContractData.getOutputParameters()
      );
      String encodedFunction = FunctionEncoder.encode(function);
      var nonce = getAvailableNonce(web3j, credentials.getAddress());

      Transaction transaction = Transaction.createFunctionCallTransaction(
          credentials.getAddress(), nonce, gasProvider.getGasPrice(), gasProvider.getGasLimit(),
          ethereumContractData.getContractAddress(), BigInteger.ZERO, encodedFunction
      );

//      EthEstimateGas gasEstimate = web3j.ethEstimateGas(transaction)
//          .send();
//      if (gasEstimate.hasError()) {
//        throw new TransactionException("Cannot estimate gas: " + gasEstimate.getError()
//            .getMessage());
//      }

//      BigInteger gasLimit = gasEstimate.getAmountUsed()
//          .multiply(BigInteger.valueOf(2));
      BigInteger gasLimit = BigInteger.valueOf(ethereumServerInformation.getGasLimit());

      RawTransaction rawTransaction = RawTransaction.createTransaction(
          nonce, gasProvider.getGasPrice(), gasLimit, ethereumContractData.getContractAddress(),
          BigInteger.ZERO, encodedFunction
      );

      long chainId = web3j.ethChainId()
          .send()
          .getChainId()
          .longValue();
      byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
      String hexValue = Numeric.toHexString(signedMessage);

      EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue)
          .sendAsync()
          .get();
      assert transactionResponse != null;
      if (transactionResponse.hasError()) {
        throw new Exception("Transaction failed: " + transactionResponse.getError()
            .getMessage());
      }

//      TransactionReceipt receipt = web3j.ethGetTransactionReceipt(
//              transactionResponse.getTransactionHash())
//          .send()
//          .getTransactionReceipt()
//          .orElseThrow(() -> new RuntimeException("Transaction receipt not found."));
//
//      if (!receipt.isStatusOK()) {
//        throw new Exception("Contract upgrade failed. Status: " + receipt.getStatus());
//      }
      logger.info("Transaction response hash : " + transactionResponse.getTransactionHash());
      logger.info("Transaction json RPC : " + transactionResponse.getJsonrpc());
      logger.info("Transaction response id : " + transactionResponse.getId());
      logger.info("Transaction response error : " + transactionResponse.getError());
      return transactionResponse.getResult()
          .getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new BlockChainException(BlockchainErrorCode.TRANSACTION_ERROR, e);
    }
  }

  private void validateInputParameters(ServerInformation serverInformation, ContractData data)
      throws IllegalArgumentException {
    if (!(serverInformation instanceof EvmServerInformation)) {
      throw new IllegalArgumentException("ServerInformation은 EvmServerInformation 타입이어야 합니다.");
    }
    if (!(data instanceof EvmContractData)) {
      throw new IllegalArgumentException("ContractData는 EvmContractData 타입이어야 합니다.");
    }
  }

  private Web3j createWeb3jClient(EvmServerInformation ethInfo) {
    return Web3j.build(new HttpService(ethInfo.getNetworkURL()));
  }

  private BigInteger getAvailableNonce(Web3j web3j, String address)
      throws IOException, TransactionException {
    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
            address, DefaultBlockParameterName.LATEST)
        .send();

    if (ethGetTransactionCount.hasError()) {
      throw new TransactionException("Cannot get nonce: " + ethGetTransactionCount.getError()
          .getMessage());
    }

    return ethGetTransactionCount.getTransactionCount();
  }
//
//  private RawTransaction createRawTransaction(EvmContractData data) {
//    String encodedFunction = FunctionEncoder.encode(
//        new Function(
//            data.getFunctionName(), data.getInputParameters(),
//            data.getOutputParameters()
//        ));
//
//
//  }

//  @Override
//  public byte[] sendTransaction(ServerInformation serverInformation, ContractData data)
//      throws BlockChainException {
//    validateInputParameters(serverInformation, data);
//
//    EvmServerInformation ethInfo = (EvmServerInformation) serverInformation;
//    EvmContractData ethData = (EvmContractData) data;
//
//    Web3j web3j = createWeb3jClient(ethInfo);
//    Credentials credentials = Credentials.create(ethData.getPrivateKey());
//
//    try {
//      BigInteger nonce = getNonce(web3j, credentials.getAddress());
//
//      RawTransaction rawTransaction = createRawTransaction(ethInfo, ethData, nonce);
//      String transactionHash = signAndSendTransaction(
//          web3j, rawTransaction, ethInfo.getChainId(), credentials);
//      return transactionHash.getBytes();
//    } catch (Exception e) {
//      throw new BlockChainException(BlockchainErrorCode.TRANSACTION_ERROR, e);
//    } finally {
//      web3j.shutdown();
//    }
//  }
//
//  private BigInteger getNonce(Web3j web3j, String address) throws Exception {
//    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
//            address, DefaultBlockParameterName.LATEST)
//        .send();
//
//    if (ethGetTransactionCount.hasError()) {
//      throw new Exception("Cannot get nonce: " + ethGetTransactionCount.getError()
//          .getMessage());
//    }
//
//    return ethGetTransactionCount.getTransactionCount();
//  }
//
//  private RawTransaction createRawTransaction(
//      Credentials credentials, EvmServerInformation ethInfo,
//      EvmContractData ethData, BigInteger nonce
//  ) {
//    if (ethData.getContractAddress() == null) {
//      throw new UnsupportedOperationException("일반 이더 전송은 아직 구현되지 않았습니다.");
//    }
//
//    // create encoded function data for contract call
//    String encodedFunction = FunctionEncoder.encode(
//        new Function(
//            ethData.getFunctionName(), ethData.getInputParameters(),
//            ethData.getOutputParameters()
//        ));
//
//    // Config EIP-1559 transaction
//    BigInteger gasLimit = BigInteger.valueOf(ethInfo.getGasLimit());
//    BigInteger gasPrice = BigInteger.valueOf(ethInfo.getGasPrice());
//    BigInteger maxPriorityFeePerGas = BigInteger.valueOf(ethInfo.getMaxPriorityFeePerGas());
//    BigInteger maxFeePerGas = BigInteger.valueOf(ethInfo.getMaxFeePerGas());
//    long chainId = ethInfo.getChainId();
//
//    Transaction tx = Transaction.createFunctionCallTransaction(
//        credentials.getAddress(),
//        nonce,
//        gasPrice,
//        gasLimit,
//        ethData.getContractAddress(),
//        encodedFunction
//    );
//
//    return RawTransaction.createTransaction(
//        nonce,
//        gasLimit,
//        ethData.getContractAddress(),
//        encodedFunction,
//        BigInteger.ZERO,
//        maxPriorityFeePerGas,
//        maxFeePerGas
//    );
//
//    return null;
//  }
//
//  private String signAndSendTransaction(
//      Web3j web3j, RawTransaction rawTransaction, long chainId,
//      Credentials credentials
//  ) throws Exception {
//    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
//    String hexValue = Numeric.toHexString(signedMessage);
//
//    logger.info("Signed transaction: " + hexValue);
//    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue)
//        .send();
//
//    if (ethSendTransaction.hasError()) {
//      throw new Exception("트랜잭션 전송 실패: " + ethSendTransaction.getError()
//          .getMessage());
//    }
//
//    return ethSendTransaction.getTransactionHash();
//  }
}
