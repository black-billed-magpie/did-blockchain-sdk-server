// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library ZKPLibrary {
    struct Schema {
        string id;
        string desc;
        string value;
    }

    function createSchema(
        string memory schemaId,
        string memory schemaDesc,
        string memory schemaValue
    ) internal pure returns (Schema memory) {
        return Schema(schemaId, schemaDesc, schemaValue);
    }

    struct CredentialDefinition {
        string id;
        string desc;
        string value;
    }

    function createCredentialDefinition(
        string memory creddefId,
        string memory creddefDesc,
        string memory creddefValue
    ) internal pure returns (CredentialDefinition memory) {
        return CredentialDefinition(creddefId, creddefDesc, creddefValue);
    }
}
