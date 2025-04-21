// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

import "./ProviderLibrary.sol";
import "./CredentialSchemaLibrary.sol";

library VcMetaLibrary {
    using ProviderLibrary for ProviderLibrary.Provider;
    using CredentialSchemaLibrary for CredentialSchemaLibrary.CredentialSchema;

    struct VcMeta {
        string id;
        ProviderLibrary.Provider issuer;
        string subject;
        CredentialSchemaLibrary.CredentialSchema credentialSchema;
        string status;
        string issuanceDate;
        string validFrom;
        string validUntil;
        string formatVersion;
        string language;
    }

    function toJson(VcMeta memory meta) internal pure returns (string memory) {
        return
            string(
                abi.encodePacked(
                    '{"id":"',
                    meta.id,
                    '","issuer":',
                    meta.issuer.toJson(),
                    ',"subject":"',
                    meta.subject,
                    '","credentialSchema":',
                    meta.credentialSchema.toJson(),
                    ',"status":"',
                    meta.status,
                    '","issuanceDate":"',
                    meta.issuanceDate,
                    '","validFrom":"',
                    meta.validFrom,
                    '","validUntil":"',
                    meta.validUntil,
                    '","formatVersion":"',
                    meta.formatVersion,
                    '","language":"',
                    meta.language,
                    '"}'
                )
            );
    }

    function equals(
        VcMeta memory a,
        VcMeta memory b
    ) internal pure returns (bool) {
        return
            keccak256(abi.encodePacked(a.id)) ==
            keccak256(abi.encodePacked(b.id)) &&
            a.issuer.equals(b.issuer) &&
            keccak256(abi.encodePacked(a.subject)) ==
            keccak256(abi.encodePacked(b.subject)) &&
            a.credentialSchema.equals(b.credentialSchema) &&
            keccak256(abi.encodePacked(a.status)) ==
            keccak256(abi.encodePacked(b.status)) &&
            keccak256(abi.encodePacked(a.issuanceDate)) ==
            keccak256(abi.encodePacked(b.issuanceDate)) &&
            keccak256(abi.encodePacked(a.validFrom)) ==
            keccak256(abi.encodePacked(b.validFrom)) &&
            keccak256(abi.encodePacked(a.validUntil)) ==
            keccak256(abi.encodePacked(b.validUntil)) &&
            keccak256(abi.encodePacked(a.formatVersion)) ==
            keccak256(abi.encodePacked(b.formatVersion)) &&
            keccak256(abi.encodePacked(a.language)) ==
            keccak256(abi.encodePacked(b.language));
    }

    function updateVcStatus(
        VcMeta storage meta,
        string memory _status
    ) internal {
        meta.status = _status; // Directly assign the string value
    }
}
