package org.omnione.did.ethereum;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.omnione.did.ContractApi;
import org.omnione.did.data.model.did.InvokedDidDoc;
import org.omnione.did.data.model.enums.did.DidDocStatus;
import org.omnione.did.data.model.enums.vc.RoleType;
import org.omnione.did.data.model.enums.vc.VcStatus;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.exception.BlockChainException;
import org.omnione.exception.BlockchainErrorCode;
import org.omnione.sender.BlockChainType;
import org.omnione.sender.SenderFactory;
import org.omnione.sender.ethereum.ContractFunctionName;
import org.omnione.sender.ethereum.EvmContractData;
import org.omnione.sender.ethereum.EvmSender;
import org.omnione.sender.ethereum.EvmServerInformation;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

public class EvmContractApi implements ContractApi {

  private final Logger LOG = Logger.getLogger(EvmContractApi.class.getName());
  private final EvmServerInformation serverInformation;
  private EvmContractData contractData;

  public EvmContractApi(String resourcePath) throws IOException {
    this.serverInformation = new EvmServerInformation(resourcePath);
    this.contractData = new EvmContractData(resourcePath);
  }

  @Override
  public void registDidDoc(InvokedDidDoc invokedDidDoc, RoleType roleType)
      throws BlockChainException {

    List<Type> inputParams = List.of();
    List<TypeReference<?>> outputParams = Collections.emptyList();
    contractData.setTransactionDetails(
        ContractFunctionName.FUNC_REGISTDIDDOC, inputParams,
        outputParams, null, null
    );

    byte[] result = send(contractData);
    if (result == null) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR, new Error("Transaction failed"));
    }

    LOG.info("Transaction successful: " + new String(result));
  }

  @Override
  public Object getDidDoc(String didKeyUrl) throws BlockChainException {
    return null;
  }

  @Override
  public Object updateDidDocStatus(String didKeyUrl, DidDocStatus didDocStatus)
      throws BlockChainException {
    return null;
  }

  @Override
  public Object updateDidDocStatus(
      String didKeyUrl, DidDocStatus didDocStatus,
      LocalDateTime terminatedTime
  ) throws BlockChainException {
    return null;
  }

  @Override
  public void registVcMetadata(VcMeta vcMeta) throws BlockChainException {

  }

  @Override
  public Object getVcMetadata(String vcId) throws BlockChainException {
    return null;
  }

  @Override
  public void updateVcStatus(String vcId, VcStatus vcStatus) throws BlockChainException {

  }

  private byte[] send(EvmContractData evmContractData) throws BlockChainException {
    EvmSender sender = (EvmSender) SenderFactory.getSender(BlockChainType.EVM);
    return sender.sendTransaction(this.serverInformation, evmContractData);
  }
}
