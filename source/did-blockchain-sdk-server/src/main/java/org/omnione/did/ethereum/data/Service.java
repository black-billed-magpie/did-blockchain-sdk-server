package org.omnione.did.ethereum.data;

import java.util.List;
import lombok.Getter;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.reflection.Parameterized;

@Getter
public class Service extends DynamicStruct {

  private final String id;
  private final String serviceEndpoint;
  private final List<String> serviceType;

  public Service(String id, String serviceEndpoint, List<String> serviceType) {
    super(
        new Utf8String(id), new Utf8String(serviceEndpoint),
        new DynamicArray<>(Utf8String.class, Utils.typeMap(serviceType, Utf8String.class))
    );
    // Initialize the fields
    this.id = id;
    this.serviceEndpoint = serviceEndpoint;
    this.serviceType = serviceType;
  }

  public Service(
      Utf8String id, Utf8String serviceEndpoint,
      @Parameterized(type = Utf8String.class) DynamicArray<Utf8String> serviceType
  ) {
    super(id, serviceEndpoint, serviceType);
    // Initialize the fields
    this.id = id.getValue();
    this.serviceEndpoint = serviceEndpoint.getValue();
    this.serviceType = serviceType.getValue()
        .stream()
        .map(Utf8String::getValue)
        .toList();
  }
}
