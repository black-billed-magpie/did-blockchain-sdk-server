// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library CredentialSchemaLibrary {
    struct CredentialSchema {
        string url;
        string credentialSchemaType;
    }

    function toJson(
        CredentialSchema memory schema
    ) internal pure returns (string memory) {
        return
            string(
                abi.encodePacked(
                    '{"url":"',
                    schema.url,
                    '","credentialSchemaType":"',
                    schema.credentialSchemaType,
                    '"}'
                )
            );
    }

    function equals(
        CredentialSchema memory a,
        CredentialSchema memory b
    ) internal pure returns (bool) {
        return
            keccak256(abi.encodePacked(a.url, a.credentialSchemaType)) ==
            keccak256(abi.encodePacked(b.url, b.credentialSchemaType));
    }
}
