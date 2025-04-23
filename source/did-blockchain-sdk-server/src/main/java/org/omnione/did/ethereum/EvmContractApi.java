package org.omnione.did.ethereum;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import org.apache.logging.log4j.util.Strings;
import org.omnione.did.ContractApi;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.InvokedDidDoc;
import org.omnione.did.data.model.enums.did.DidDocStatus;
import org.omnione.did.data.model.enums.vc.RoleType;
import org.omnione.did.data.model.enums.vc.VcStatus;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.exception.BlockChainException;
import org.omnione.exception.BlockchainErrorCode;
import org.omnione.generated.OpenDID;
import org.omnione.sender.ethereum.EvmContractData;
import org.omnione.sender.ethereum.EvmServerInformation;
import org.omnione.util.DidKeyUrlParser;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.StaticGasProvider;

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

    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      Credentials credentials = Credentials.create(contractData.getPrivateKey());
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      StaticGasProvider gasProvider = new StaticGasProvider(
          gasPrice,
          gasLimit
      );

      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          credentials,
          gasProvider
      );

      var document = convertJsonToDocument(invokedDidDoc.getDidDoc());

      contract.registDidDoc(document);
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  private OpenDID.Document convertJsonToDocument(String json) {
    DidDocument didDocument = new DidDocument();
    didDocument.fromJson(json);

    var verificationMethodList = didDocument.getVerificationMethod()
        .stream()
        .map(value -> new OpenDID.VerificationMethod(
            value.getId(),
            new BigInteger(value.getType()),
            value.getController(),
            value.getPublicKeyMultibase(),
            new BigInteger(String.valueOf(value.getAuthType()))
        ))
        .toList();
    var servicesList = didDocument.getService()
        .stream()
        .map(value -> new OpenDID.Service(
            value.getId(),
            value.getType(),
            value.getServiceEndpoint()
        ))
        .toList();
    return new OpenDID.Document(
        didDocument.getContext(),
        didDocument.getId(),
        didDocument.getController(),
        didDocument.getCreated(),
        didDocument.getUpdated(),
        didDocument.getVersionId(),
        didDocument.getDeactivated(),
        verificationMethodList,
        didDocument.getAssertionMethod(),
        didDocument.getAuthentication(),
        didDocument.getKeyAgreement(),
        didDocument.getCapabilityInvocation(),
        didDocument.getCapabilityDelegation(),
        servicesList
    );
  }

  @Override
  public Object getDidDoc(String didKeyUrl) throws BlockChainException {

    DidKeyUrlParser parser = new DidKeyUrlParser(didKeyUrl);

    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      ReadonlyTransactionManager readonlyTransactionManager = new ReadonlyTransactionManager(
          web3j,
          contractData.getContractAddress()
      );
      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          readonlyTransactionManager,
          new StaticGasProvider(
              gasPrice,
              gasLimit
          )
      );

      LOG.info("encoded data : " + contract.getDidDoc(parser.getDid())
          .encodeFunctionCall());

      return contract.getDidDoc(parser.getDid())
          .send();

    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  @Override
  public Object updateDidDocStatus(String didKeyUrl, DidDocStatus didDocStatus)
      throws BlockChainException {

    if (didDocStatus == DidDocStatus.TERMINATED) {
      throw new IllegalArgumentException("TERMINATED status requires a terminated time");
    }
    DidKeyUrlParser parser = new DidKeyUrlParser(didKeyUrl);
    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      Credentials credentials = Credentials.create(contractData.getPrivateKey());
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      StaticGasProvider gasProvider = new StaticGasProvider(
          gasPrice,
          gasLimit
      );

      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          credentials,
          gasProvider
      );

      var versionId = didDocStatus == DidDocStatus.REVOKED ? Strings.EMPTY : parser.getVersionId();
      return contract.updateDidDocStatusInService(
              parser.getDid(),
              didDocStatus.getRawValue(),
              versionId
          )
          .send()
          .getStatus();
    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  @Override
  public Object updateDidDocStatus(String didKeyUrl, DidDocStatus didDocStatus,
                                   LocalDateTime terminatedTime
  ) throws BlockChainException {

    DidKeyUrlParser parser = new DidKeyUrlParser(didKeyUrl);
    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      Credentials credentials = Credentials.create(contractData.getPrivateKey());
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      StaticGasProvider gasProvider = new StaticGasProvider(
          gasPrice,
          gasLimit
      );

      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          credentials,
          gasProvider
      );

      return contract.updateDidDocStatusRevocation(
              parser.getDid(),
              didDocStatus.getRawValue(),
              terminatedTime.format(DateTimeFormatter.ISO_DATE_TIME)
          )
          .send()
          .getStatus();
    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  @Override
  public void registVcMetadata(VcMeta vcMeta) throws BlockChainException {

    var vcMetaData = convertVcMetaToVcMetaData(vcMeta);

    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      Credentials credentials = Credentials.create(contractData.getPrivateKey());
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      StaticGasProvider gasProvider = new StaticGasProvider(
          gasPrice,
          gasLimit
      );

      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          credentials,
          gasProvider
      );

      contract.registVcMetaData(vcMetaData);
    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  private OpenDID.VcMeta convertVcMetaToVcMetaData(VcMeta vcMeta) {
    return new OpenDID.VcMeta(
        vcMeta.getId(),
        new OpenDID.Provider(
            vcMeta.getIssuer()
                .getDid(),
            vcMeta.getIssuer()
                .getCertVcRef()
        ),
        vcMeta.getSubject(),
        new OpenDID.CredentialSchemaLibrary_CredentialSchema(
            vcMeta.getCredentialSchema()
                .getId(),
            vcMeta.getCredentialSchema()
                .getType()
        ),
        vcMeta.getStatus(),
        vcMeta.getIssuanceDate(),
        vcMeta.getValidFrom(),
        vcMeta.getValidUntil(),
        vcMeta.getFormatVersion(),
        vcMeta.getLanguage()
    );
  }

  @Override
  public Object getVcMetadata(String vcId) throws BlockChainException {
    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      ReadonlyTransactionManager readonlyTransactionManager = new ReadonlyTransactionManager(
          web3j,
          contractData.getContractAddress()
      );
      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          readonlyTransactionManager,
          new StaticGasProvider(
              gasPrice,
              gasLimit
          )
      );

      return contract.getVcmetaData(vcId)
          .send();
    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }

  @Override
  public void updateVcStatus(String vcId, VcStatus vcStatus) throws BlockChainException {
    try (Web3j web3j = Web3j.build(new HttpService(serverInformation.getNetworkURL()))) {
      Credentials credentials = Credentials.create(contractData.getPrivateKey());
      BigInteger gasPrice = web3j.ethGasPrice()
          .send()
          .getGasPrice();
      BigInteger gasLimit = BigInteger.valueOf(10000000L);
      StaticGasProvider gasProvider = new StaticGasProvider(
          gasPrice,
          gasLimit
      );

      var contract = OpenDID.load(
          contractData.getContractAddress(),
          web3j,
          credentials,
          gasProvider
      );

      contract.updateVcStats(
          vcId,
          vcStatus.getRawValue()
      );
    } catch (ContractCallException e) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR,
          new Error("Contract call error: " + e.getMessage())
      );
    } catch (Exception e) {
      throw new BlockChainException(
          BlockchainErrorCode.CONNECTION_ERROR,
          new Error("Network error: " + e.getMessage())
      );
    }
  }
}
