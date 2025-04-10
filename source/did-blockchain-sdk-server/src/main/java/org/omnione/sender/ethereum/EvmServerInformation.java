package org.omnione.sender.ethereum;

import java.io.IOException;
import java.util.Properties;
import lombok.Builder;
import lombok.Getter;
import org.omnione.sender.ServerInformation;

@Getter
@Builder
public class EvmServerInformation extends ServerInformation {

  private String networkURL;
  private long chainId;
  private long gasLimit;
  private long gasPrice;
  private int connectionTimeout;

  public EvmServerInformation(String resource) throws IOException {
    super();

    // Load properties from the resource file and initialize the fields
    Properties properties = loadProperties(resource);
    this.networkURL = properties.getProperty("evm.network.url");
    this.chainId = Long.parseLong(properties.getProperty("evm.chainId"));
    this.gasLimit = Long.parseLong(properties.getProperty("evm.gas.limit"));
    this.gasPrice = Long.parseLong(properties.getProperty("evm.gas.price"));
    this.connectionTimeout = Integer.parseInt(properties.getProperty("evm.connection.timeout"));
  }
}
