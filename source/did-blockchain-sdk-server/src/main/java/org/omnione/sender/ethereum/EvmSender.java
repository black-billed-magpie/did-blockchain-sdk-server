package org.omnione.sender.ethereum;

import java.math.BigInteger;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.sender.ContractData;
import org.omnione.sender.OpenDidSender;
import org.omnione.sender.ServerInformation;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

@Slf4j
@NoArgsConstructor
public class EvmSender implements OpenDidSender {

  @Override
  public byte[] sendTransaction(ServerInformation serverInformation, ContractData data) throws Exception {
    validateInputParameters(serverInformation, data);

    EvmServerInformation ethInfo = (EvmServerInformation) serverInformation;
    EvmContractData ethData = (EvmContractData) data;

    Web3j web3j = createWeb3jClient(ethInfo);

    try {
      Credentials credentials = Credentials.create(ethData.getPrivateKey());
      BigInteger nonce = getNonce(web3j, credentials.getAddress());

      RawTransaction rawTransaction = createRawTransaction(ethInfo, ethData, nonce);
      String transactionHash = signAndSendTransaction(web3j, rawTransaction, ethInfo.getChainId(), credentials);
      return transactionHash.getBytes();
    } finally {
      web3j.shutdown();
    }
  }

  private void validateInputParameters(ServerInformation serverInformation, ContractData data) throws Exception {
    if (!(serverInformation instanceof EvmServerInformation)) {
      throw new IllegalArgumentException("ServerInformation은 EvmServerInformation 타입이어야 합니다.");
    }
    if (!(data instanceof EvmContractData)) {
      throw new IllegalArgumentException("ContractData는 EvmContractData 타입이어야 합니다.");
    }
  }

  private Web3j createWeb3jClient(EvmServerInformation ethInfo) {
    return Web3j.build(
        new HttpService(ethInfo.getNetworkURL(),
            true)
    );
  }

  private BigInteger getNonce(Web3j web3j, String address) throws Exception {
    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
        address, DefaultBlockParameterName.LATEST).send();

    if (ethGetTransactionCount.hasError()) {
      throw new Exception("Cannot get nonce: " + ethGetTransactionCount.getError().getMessage());
    }

    return ethGetTransactionCount.getTransactionCount();
  }

  private RawTransaction createRawTransaction(EvmServerInformation ethInfo,
      EvmContractData ethData,
      BigInteger nonce) {
    if (ethData.getContractAddress() == null) {
      throw new UnsupportedOperationException("일반 이더 전송은 아직 구현되지 않았습니다.");
    }

    return RawTransaction.createTransaction(
        nonce,
        BigInteger.valueOf(ethInfo.getGasPrice()),
        BigInteger.valueOf(ethInfo.getGasLimit()),
        ethData.getContractAddress(),
        ethData.getEthValue() != null ? new BigInteger(ethData.getEthValue()) : BigInteger.ZERO,
        ethData.getData()
    );
  }

  private String signAndSendTransaction(Web3j web3j, RawTransaction rawTransaction,
      long chainId, Credentials credentials) throws Exception {
    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
    String hexValue = Numeric.toHexString(signedMessage);

    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

    if (ethSendTransaction.hasError()) {
      throw new Exception("트랜잭션 전송 실패: " + ethSendTransaction.getError().getMessage());
    }

    return ethSendTransaction.getTransactionHash();
  }
}
