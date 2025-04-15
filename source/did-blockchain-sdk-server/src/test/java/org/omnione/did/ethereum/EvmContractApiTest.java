package org.omnione.did.ethereum;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.omnione.did.data.model.did.InvokedDidDoc;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.enums.vc.RoleType;
import org.omnione.did.data.model.provider.Provider;
import org.omnione.exception.BlockChainException;
import org.omnione.sender.ethereum.EvmServerInformation;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

class EvmContractApiTest {

  private final static Logger LOG = LoggerFactory.getLogger(EvmContractApiTest.class);
  private final String RESOURCE_PATH = "/Users/mykim/Workspace/OpenSource/did-blockchain-sdk-server/source/did-blockchain-sdk-server/src/test/resources/application-test.properties";
  private final String JSON_DOCUMNET_PATH = "/Users/mykim/Workspace/OpenSource/did-blockchain-sdk-server/source/did-blockchain-sdk-server/src/test/resources/data/document.json";
  private ECKeyPair keyPair = null;
  private EvmServerInformation serverInformation;


  @BeforeEach
  void setUp()
      throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    this.serverInformation = new EvmServerInformation(RESOURCE_PATH);
    this.keyPair = Keys.createEcKeyPair();
  }

  @Test
  void registerDidDoc() throws IOException, BlockChainException {
    EvmContractApi evmContractApi = new EvmContractApi(RESOURCE_PATH);

    InvokedDidDoc document = newInvokedDidDocument();

    Assertions.assertDoesNotThrow(() -> {
      evmContractApi.registDidDoc(document, RoleType.APP_PROVIDER);
    });
  }

  private InvokedDidDoc newInvokedDidDocument() {
    String document = readDocument();

    assert document != null;
    JsonObject obj = Json.createReader(new StringReader(document))
        .readObject();
    Proof proof = newProof();
    Provider provider = newProvider(
        obj.get("id")
            .toString(), "certVcRef"
    );
    String nonce = generateNonce();

    return new InvokedDidDoc(document, proof, provider, nonce);
  }

  private String readDocument() {
    try {
      return new String(Files.readAllBytes(Paths.get(JSON_DOCUMNET_PATH))); // 파일 내용을 문자열로 읽기
    } catch (IOException e) {
      LOG.error(e, () -> "Failed to read document from path: " + JSON_DOCUMNET_PATH); // 에러 로그 추가
      return null; // 예외 발생 시 null 반환
    }
  }

  private Proof newProof() {
    Proof proof = new Proof();
    proof.setType("Secp256k1Signature2018");
    proof.setCreated(getCurrentTimeInIsoFormat());
    proof.setProofPurpose("verificationMethod");
    proof.setVerificationMethod("did:example:123456789abcdefghi?versionId=1#key1");

    byte[] messageHash = Hash.sha3(proof.toJson()
        .getBytes());
    Sign.SignatureData signature = Sign.signMessage(messageHash, keyPair, false);
    proof.setProofValue(Hex.toHexString(signature.getV()) + Hex.toHexString(signature.getR()) +
                        Hex.toHexString(signature.getS()));

    return proof;
  }

  private String getCurrentTimeInIsoFormat() {
    return DateTimeFormatter.ISO_INSTANT.format(Instant.now()
        .atOffset(ZoneOffset.UTC));
  }

  private Provider newProvider(String did, String certVcRef) {
    Provider provider = new Provider();
    provider.setDid(did);
    provider.setCertVcRef(certVcRef);

    return provider;
  }

  private String generateNonce() {
    // Generate a random nonce (for example, using UUID)
    return java.util.UUID.randomUUID()
        .toString();
  }

  @Test
  void getDocumentTest() throws IOException {
    Web3j web3j = Web3j.build(new HttpService("http://10.48.17.200:50010")); // Besu RPC 주소

    // 2. 함수 정의
    Function function = new Function(
        "getDidDoc", List.of(new Utf8String("did:example:123456789abcdefghi")),
        List.of(new TypeReference<Utf8String>() {
        })
    );

    String encodedFunction = FunctionEncoder.encode(function);

    // 3. 트랜잭션 생성 (eth_call)
    String fromAddress = "0xFE3B557E8Fb62b89F4916B721be55cEb828dBd73"; // 아무 valid한 주소
    String contractAddress = "0x9B8397f1B0FEcD3a1a40CdD5E8221Fa461898517";

    Transaction ethCallTx = Transaction.createEthCallTransaction(
        fromAddress, contractAddress,
        encodedFunction
    );

    // 4. 함수 호출
    EthCall response = web3j.ethCall(ethCallTx, DefaultBlockParameterName.LATEST)
        .send();

    // 5. 결과 디코딩
    LOG.info(() -> "Response revert reason: " + response.getRevertReason());
    String value = response.getValue();
    List<Type> result = FunctionReturnDecoder.decode(value, function.getOutputParameters());
    Utf8String didDoc = (Utf8String) result.get(0);

    System.out.println("📄 DID Document: " + didDoc.getValue());

  }
}