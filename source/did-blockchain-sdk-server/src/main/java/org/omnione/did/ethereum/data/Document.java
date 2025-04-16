package org.omnione.did.ethereum.data;

import java.util.List;
import lombok.Getter;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

@Getter
public class Document extends DynamicStruct {

  public final List<String> context;
  public final String id;
  public final String controller;
  public final String created;
  public final String updated;
  public final String versionId;
  public final Boolean deactivated;
  public final List<VerificationMethod> verificationMethod;
  public final List<String> assertionsMethod;
  public final List<String> authentication;
  public final List<String> keyAgreement;
  public final List<String> capabilityInvocation;
  public final List<String> capabilityDelegation;
  public final List<Service> services;

  public Document(
      List<String> context, String id, String controller, String created, String updated,
      String versionId, Boolean deactivated, List<VerificationMethod> verificationMethod,
      List<String> assertionsMethod, List<String> authentication, List<String> keyAgreement,
      List<String> capabilityInvocation, List<String> capabilityDelegation, List<Service> services
  ) {
    super(
        new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(context, org.web3j.abi.datatypes.Utf8String.class)
        ), new org.web3j.abi.datatypes.Utf8String(id),
        new org.web3j.abi.datatypes.Utf8String(controller),
        new org.web3j.abi.datatypes.Utf8String(created),
        new org.web3j.abi.datatypes.Utf8String(updated),
        new org.web3j.abi.datatypes.Utf8String(versionId),
        new org.web3j.abi.datatypes.Bool(deactivated),
        new org.web3j.abi.datatypes.DynamicArray<>(VerificationMethod.class, verificationMethod),
        new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(assertionsMethod, org.web3j.abi.datatypes.Utf8String.class)
        ),
        new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(authentication, org.web3j.abi.datatypes.Utf8String.class)
        ),
        new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(keyAgreement, org.web3j.abi.datatypes.Utf8String.class)
        ), new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(
                capabilityInvocation,
                org.web3j.abi.datatypes.Utf8String.class
            )
        ), new org.web3j.abi.datatypes.DynamicArray<>(
            org.web3j.abi.datatypes.Utf8String.class,
            org.web3j.abi.Utils.typeMap(
                capabilityDelegation,
                org.web3j.abi.datatypes.Utf8String.class
            )
        ), new org.web3j.abi.datatypes.DynamicArray<>(Service.class, services)
    );
    // Initialize the fields
    this.context = context;
    this.id = id;
    this.controller = controller;
    this.created = created;
    this.updated = updated;
    this.versionId = versionId;
    this.deactivated = deactivated;
    this.verificationMethod = verificationMethod;
    this.assertionsMethod = assertionsMethod;
    this.authentication = authentication;
    this.keyAgreement = keyAgreement;
    this.capabilityInvocation = capabilityInvocation;
    this.capabilityDelegation = capabilityDelegation;
    this.services = services;
  }

  public Document(
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> context, Utf8String id,
      Utf8String controller, Utf8String created, Utf8String updated, Utf8String versionId,
      Bool deactivated,
      @Parameterized(type = VerificationMethod.class) DynamicArray<VerificationMethod> verificationMethod,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> assertionsMethod,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> authentication,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> keyAgreement,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> capabilityInvocation,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> capabilityDelegation,
      @Parameterized(type = Service.class) DynamicArray<Service> services
  ) {
    super(
        context, id, controller, created, updated, versionId, deactivated, verificationMethod,
        assertionsMethod, authentication, keyAgreement, capabilityInvocation, capabilityDelegation,
        services
    );
    // Initialize the fields
    this.context = context.getValue()
        .stream()
        .map(v -> v.getValue())
        .toList();
    this.id = id.getValue();
    this.controller = controller.getValue();
    this.created = created.getValue();
    this.updated = updated.getValue();
    this.versionId = versionId.getValue();
    this.deactivated = deactivated.getValue();
    this.verificationMethod = verificationMethod.getValue();
    this.assertionsMethod = assertionsMethod.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
    this.authentication = authentication.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
    this.keyAgreement = keyAgreement.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
    this.capabilityInvocation = capabilityInvocation.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
    this.capabilityDelegation = capabilityDelegation.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
    this.services = services.getValue();
  }
}
