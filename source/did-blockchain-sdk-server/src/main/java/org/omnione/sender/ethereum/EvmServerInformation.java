package org.omnione.sender.ethereum;

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
}
