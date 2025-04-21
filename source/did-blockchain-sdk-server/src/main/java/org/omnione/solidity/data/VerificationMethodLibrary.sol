// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library VerificationMethodLibrary {
    enum KeyType {
        RsaVerificationKey2018,
        Secp256k1VerificationKey2018,
        Secp256r1VerificationKey2018
    }

    enum AuthType {
        Free,
        PIN,
        BIO
    }

    struct VerificationMethod {
        string id;
        KeyType keyType;
        string controller;
        string publicKeyMultibase;
        AuthType authType;
    }

    function equals(
        VerificationMethod memory a,
        VerificationMethod memory b
    ) internal pure returns (bool) {
        return
            keccak256(
                abi.encodePacked(
                    a.id,
                    a.keyType,
                    a.controller,
                    a.publicKeyMultibase,
                    a.authType
                )
            ) ==
            keccak256(
                abi.encodePacked(
                    b.id,
                    b.keyType,
                    b.controller,
                    b.publicKeyMultibase,
                    b.authType
                )
            );
    }

    function toJson(
        VerificationMethod memory method
    ) internal pure returns (string memory) {
        return
            string(
                abi.encodePacked(
                    '{"id":"',
                    method.id,
                    '","keyType":"',
                    method.keyType,
                    '","controller":"',
                    method.controller,
                    '","publicKeyMultibase":"',
                    method.publicKeyMultibase,
                    '","authType":"',
                    method.authType,
                    '"}'
                )
            );
    }

    function toHash(
        VerificationMethod memory verificationMethod
    ) internal pure returns (bytes32) {
        return
            sha256(
                abi.encodePacked(
                    verificationMethod.id,
                    verificationMethod.keyType,
                    verificationMethod.controller,
                    verificationMethod.publicKeyMultibase,
                    verificationMethod.authType
                )
            );
    }

    function keyTypeToString(
        KeyType keyType
    ) internal pure returns (string memory) {
        if (keyType == KeyType.RsaVerificationKey2018) {
            return "RsaVerificationKey2018";
        } else if (keyType == KeyType.Secp256k1VerificationKey2018) {
            return "Secp256k1VerificationKey2018";
        } else if (keyType == KeyType.Secp256r1VerificationKey2018) {
            return "Secp256r1VerificationKey2018";
        } else {
            return "";
        }
    }

    function authTypeToInt(AuthType authType) internal pure returns (int) {
        if (authType == AuthType.Free) {
            return 1;
        } else if (authType == AuthType.PIN) {
            return 2;
        } else if (authType == AuthType.BIO) {
            return 4;
        } else {
            return 0;
        }
    }
}
