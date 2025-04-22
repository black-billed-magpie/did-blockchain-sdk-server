package org.omnione.sender.ethereum;

import java.math.BigInteger;
import java.util.List;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;

public interface EvmTransaction {
  Object send(Web3j web3j, BigInteger gasPrice, BigInteger gasLimit,
              ContractFunctionName contractFunctionName, List<Type> args
  ) throws Exception;
}
