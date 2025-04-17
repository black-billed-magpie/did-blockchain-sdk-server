package org.omnione.did.ethereum;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.omnione.did.ContractApi;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.InvokedDidDoc;
import org.omnione.did.data.model.enums.did.DidDocStatus;
import org.omnione.did.data.model.enums.vc.RoleType;
import org.omnione.did.data.model.enums.vc.VcStatus;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.did.ethereum.data.Document;
import org.omnione.did.ethereum.data.Service;
import org.omnione.did.ethereum.data.VerificationMethod;
import org.omnione.exception.BlockChainException;
import org.omnione.exception.BlockchainErrorCode;
import org.omnione.sender.BlockChainType;
import org.omnione.sender.SenderFactory;
import org.omnione.sender.ethereum.ContractFunctionName;
import org.omnione.sender.ethereum.EvmContractData;
import org.omnione.sender.ethereum.EvmSender;
import org.omnione.sender.ethereum.EvmServerInformation;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint8;

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

    var document = convertJsonToDocument(invokedDidDoc.getDidDoc());
    List<Type> inputParams = List.of(document);
    List<TypeReference<?>> outputParams = Collections.emptyList();
    contractData.setTransactionDetails(
        ContractFunctionName.FUNC_REGISTDIDDOC, inputParams,
        outputParams
    );

    byte[] result = send(contractData);
    if (result == null) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR, new Error("Transaction failed"));
    }

    LOG.info("Transaction successful: " + new String(result));
  }

  private Document convertJsonToDocument(String json) {
    DidDocument didDocument = new DidDocument();
    didDocument.fromJson(json);

    DynamicArray<Utf8String> context = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getContext(), Utf8String.class)
    );

    Utf8String id = new Utf8String(didDocument.getId());
    Utf8String controller = new Utf8String(didDocument.getController());
    Utf8String created = new Utf8String(didDocument.getCreated());
    Utf8String updated = new Utf8String(didDocument.getUpdated());
    Utf8String versionId = new Utf8String(didDocument.getVersionId());
    Bool deactivated = new Bool(didDocument.getDeactivated());
    List<VerificationMethod> verificationMethodList = didDocument.getVerificationMethod()
        .stream()
        .map(
            value -> new VerificationMethod(
                value.getId(), new BigInteger(value.getType()), value.getController(),
                value.getPublicKeyMultibase(), new BigInteger(String.valueOf(value.getAuthType()))
            ))
        .toList();
    DynamicArray<VerificationMethod> verificationMethod = new DynamicArray<>(
        VerificationMethod.class, verificationMethodList);
    DynamicArray<Utf8String> assertionsMethod = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getAssertionMethod(), Utf8String.class)
    );
    DynamicArray<Utf8String> authentication = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getAuthentication(), Utf8String.class)
    );
    DynamicArray<Utf8String> keyAgreement = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getKeyAgreement(), Utf8String.class)
    );
    DynamicArray<Utf8String> capabilityInvocation = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getCapabilityInvocation(), Utf8String.class)
    );
    DynamicArray<Utf8String> capabilityDelegation = new DynamicArray<>(
        Utf8String.class,
        org.web3j.abi.Utils.typeMap(didDocument.getCapabilityDelegation(), Utf8String.class)
    );
    List<Service> servicesList = didDocument.getService()
        .stream()
        .map(value -> new Service(value.getId(), value.getType(), value.getServiceEndpoint()))
        .toList();
    DynamicArray<Service> services = new DynamicArray<>(Service.class, servicesList);

    return new Document(
        context, id, controller, created, updated, versionId, deactivated, verificationMethod,
        assertionsMethod, authentication, keyAgreement, capabilityInvocation, capabilityDelegation,
        services
    );
  }

  @Override
  public Object getDidDoc(String didKeyUrl) throws BlockChainException {

    List<Type> inputParams = List.of(new Utf8String(didKeyUrl));
    List<TypeReference<?>> outputParams = List.of(new TypeReference<Document>() {
    });
    contractData.setTransactionDetails(
        ContractFunctionName.FUNC_GET_DOCUMENT, inputParams,
        outputParams
    );

    byte[] documentBytes = send(contractData);
    if (documentBytes == null) {
      throw new BlockChainException(
          BlockchainErrorCode.TRANSACTION_ERROR, new Error("Transaction failed"));

    }
    List<Type> decodedData = FunctionReturnDecoder.decode(
        new String(documentBytes), Utils.convert(outputParams));

    List<Type> decodedDynamicStruct = ((DynamicStruct) decodedData.get(0)).getValue();
    DidDocument didDocument = new DidDocument();
    didDocument.setContext(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(0)));
    didDocument.setId(((Utf8String) decodedDynamicStruct.get(1)).getValue());
    didDocument.setController(((Utf8String) decodedDynamicStruct.get(2)).getValue());
    didDocument.setCreated(((Utf8String) decodedDynamicStruct.get(3)).getValue());
    didDocument.setUpdated(((Utf8String) decodedDynamicStruct.get(4)).getValue());
    didDocument.setVersionId(((Utf8String) decodedDynamicStruct.get(5)).getValue());
    didDocument.setDeactivated(((Bool) decodedDynamicStruct.get(6)).getValue());
    didDocument.setVerificationMethod(
        mapVerificationMethod((DynamicArray<DynamicStruct>) decodedDynamicStruct.get(7)));
    didDocument.setAssertionMethod(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(8)));
    didDocument.setAuthentication(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(9)));
    didDocument.setKeyAgreement(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(10)));
    didDocument.setCapabilityInvocation(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(11)));
    didDocument.setCapabilityDelegation(
        mapDynamicArrayToList((DynamicArray<Utf8String>) decodedDynamicStruct.get(12)));
    didDocument.setService(mapServices((DynamicArray<DynamicStruct>) decodedDynamicStruct.get(13)));

    return didDocument;
  }

  private List<String> mapDynamicArrayToList(DynamicArray<Utf8String> dynamicArray) {
    return dynamicArray.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
  }

  private List<org.omnione.did.data.model.did.VerificationMethod> mapVerificationMethod(
      DynamicArray<DynamicStruct> verificationMethodStruct
  ) {
    return verificationMethodStruct.getValue()
        .stream()
        .map(item -> {
          List<Type> values = item.getValue();
          org.omnione.did.data.model.did.VerificationMethod method = new org.omnione.did.data.model.did.VerificationMethod();
          method.setId(((Utf8String) values.get(0)).getValue());
          method.setType(String.valueOf(((Uint8) values.get(1)).getValue()));
          method.setController(((Utf8String) values.get(2)).getValue());
          method.setPublicKeyMultibase(((Utf8String) values.get(3)).getValue());
          method.setAuthType(((Uint8) values.get(4)).getValue()
              .intValue());
          return method;
        })
        .toList();
  }

  private List<org.omnione.did.data.model.did.Service> mapServices(
      DynamicArray<DynamicStruct> servicesStruct
  ) {
    return servicesStruct.getValue()
        .stream()
        .map(item -> {
          List<Type> values = item.getValue();
          org.omnione.did.data.model.did.Service service = new org.omnione.did.data.model.did.Service();
          service.setId(((Utf8String) values.get(0)).getValue());
          service.setType(((Utf8String) values.get(1)).getValue());
          service.setServiceEndpoint(
              mapDynamicArrayToList((DynamicArray<Utf8String>) values.get(2)));

          return service;
        })
        .toList();
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
