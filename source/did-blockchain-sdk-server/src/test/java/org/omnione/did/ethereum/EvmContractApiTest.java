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
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

class EvmContractApiTest {

  private final static Logger LOG = LoggerFactory.getLogger(EvmContractApiTest.class);
  private final String RESOURCE_PATH = "/Users/mykim/Workspace/OpenSource/did-blockchain-sdk-server/source/did-blockchain-sdk-server/src/test/resources/application-test.properties";
  private final String JSON_DOCUMNET_PATH = "/Users/mykim/Workspace/OpenSource/did-blockchain-sdk-server/source/did-blockchain-sdk-server/src/test/resources/data/document.json";
  private ECKeyPair keyPair = null;


  @BeforeEach
  void setUp()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
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
  void getDidDoc() throws IOException {
    EvmContractApi evmContractApi = new EvmContractApi(RESOURCE_PATH);

    Assertions.assertDoesNotThrow(() -> {
      var result = evmContractApi.getDidDoc("did:example:123456789abcdefghi?versionId=1");
    });
  }
}