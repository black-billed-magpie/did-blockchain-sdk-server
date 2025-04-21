// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library ServiceLibrary {
    enum DID_SERVICE_TYPE {
        LINKED_DOMAINS,
        CREDENTIAL_REGISTRY
    }

    struct Service {
        string id;
        string serviceEndpoint;
        string[] serviceType;
    }

    function equals(
        Service memory a,
        Service memory b
    ) internal pure returns (bool) {
        if (
            keccak256(abi.encodePacked(a.id)) !=
            keccak256(abi.encodePacked(b.id)) ||
            keccak256(abi.encodePacked(a.serviceEndpoint)) !=
            keccak256(abi.encodePacked(b.serviceEndpoint)) ||
            a.serviceType.length != b.serviceType.length
        ) {
            return false;
        }

        for (uint256 i = 0; i < a.serviceType.length; i++) {
            if (
                keccak256(abi.encodePacked(a.serviceType[i])) !=
                keccak256(abi.encodePacked(b.serviceType[i]))
            ) {
                return false;
            }
        }

        return true;
    }
}
