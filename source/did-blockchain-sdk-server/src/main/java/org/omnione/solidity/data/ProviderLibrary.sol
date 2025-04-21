// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library ProviderLibrary {
    struct Provider {
        string did;
        string certVcReference;
    }

    function equals(
        Provider memory a,
        Provider memory b
    ) internal pure returns (bool) {
        if (
            keccak256(abi.encodePacked(a.did)) !=
            keccak256(abi.encodePacked(b.did)) ||
            keccak256(abi.encodePacked(a.certVcReference)) !=
            keccak256(abi.encodePacked(b.certVcReference))
        ) {
            return false;
        }

        return true;
    }

    function toJson(
        Provider memory provider
    ) internal pure returns (string memory) {
        return
            string(
                abi.encodePacked(
                    '{"did":"',
                    provider.did,
                    '","certVcReference":"',
                    provider.certVcReference,
                    '"}'
                )
            );
    }
}
