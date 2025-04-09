package org.omnione.sender.ethereum;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EvmServerInformation {

  private String networkURL;
  private long chainId;
  private long gasLimit;
  private long gasPrice;
  private int connectionTimeout;
}
