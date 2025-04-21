// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

import {DocumentLibrary} from "./data/DocumentLibrary.sol";
import {VcMetaLibrary} from "./data/VcMetaLibrary.sol";
import {DocumentStorage} from "./storage/DocumentStorage.sol";
import {VcMetaStorage} from "./storage/VcMetaStorage.sol";
import {ZKPStorage} from "./storage/ZKPStorage.sol";

contract OpenDID is Initializable, UUPSUpgradeable, OwnableUpgradeable {
    // 이벤트 정의
    event Setup();
    event DIDCreated(string did, address controller);
    event DIDUpdated(string did, address controller);
    event DIDDeactivated(string did, address controller);
    event VCIssued(string vcId, address issuer, string did);
    event VCStatus(string vcId, address player, string status);

    using DocumentLibrary for DocumentLibrary.Document;
    using VcMetaLibrary for VcMetaLibrary.VcMeta;

    DocumentStorage private documentStorage;
    VcMetaStorage private vcMetaStorage;
    ZKPStorage private zkpStorage;

    function initialize(
        address _documentStorage,
        address _vcMetaStorage,
        address _zkpStorage
    ) public initializer {
        __Ownable_init(_msgSender());
        __UUPSUpgradeable_init();

        require(
            _documentStorage != address(0),
            "Invalid DocumentStorage address"
        );

        require(_vcMetaStorage != address(0), "Invalid VcMetaStorage address");
        require(_zkpStorage != address(0), "Invalid ZKPStorage address");

        documentStorage = DocumentStorage(_documentStorage);
        vcMetaStorage = VcMetaStorage(_vcMetaStorage);
        zkpStorage = ZKPStorage(_zkpStorage);

        emit Setup();
    }

    function hasInitialized() public view returns (bool) {
        return _getInitializedVersion() > 0;
    }

    function _authorizeUpgrade(
        address newImplement
    ) internal override onlyOwner {}

    function setDocumentStorage(address _documentStorage) public onlyOwner {
        documentStorage = DocumentStorage(_documentStorage);
    }

    function setVcMetaStorage(address _vcMetaStorage) public onlyOwner {
        vcMetaStorage = VcMetaStorage(_vcMetaStorage);
    }

    function setZKPStorage(address _zkpStorage) public onlyOwner {
        zkpStorage = ZKPStorage(_zkpStorage);
    }

    function registDidDoc(
        DocumentLibrary.Document calldata invokedDidDoc
    ) public returns (DocumentLibrary.Document memory) {
        try
        documentStorage.registerDocument(invokedDidDoc, msg.sender)
        returns (bool isSuccess) {
            // Document registration successful
            require(isSuccess, "Document registration failed");
            emit DIDCreated(invokedDidDoc.id, msg.sender);
            return invokedDidDoc;
        } catch Error(string memory reason) {
            // Document registration failed
            revert(reason);
        } catch {
            revert("Unknown error occurred during document registration");
        }
    }

    function getDidDoc(
        string calldata _did
    ) public view returns (DocumentLibrary.Document memory) {
        DocumentLibrary.Document memory document = documentStorage.getDocument(
            _did
        );
        return document;
    }

    function getDidDocStatus(
        string calldata _did
    ) public view returns (DocumentLibrary.DocumentStatus memory) {
        DocumentLibrary.DocumentStatus memory documentStatus = documentStorage
            .getDocumentStatus(_did);
        return documentStatus;
    }

    function updateDidDocStatusInService(
        string calldata _did,
        string calldata _status,
        string calldata _versionId
    ) public {
        // 기존 저장된 문서 조회
        DocumentLibrary.Document memory storedDocument = documentStorage
            .getDocument(_did, _versionId);

        // 문서 상태 업데이트
        DocumentLibrary.setActivated(storedDocument, _status);

        // 문서 상태 저장
        documentStorage.updateDocument(storedDocument, _did, _versionId);
    }

    function updateDidDocStatusRevocation(
        string calldata _did,
        string calldata _status,
        string calldata _terminatedTime
    ) public {
        DocumentLibrary.DocumentStatus memory documentStatus = documentStorage
            .getDocumentStatus(_did);

        DocumentLibrary.updateStatus(documentStatus, _status, _terminatedTime);
        documentStorage.updateDocumentStatus(documentStatus, _did);
    }

    function registVcMetaData(VcMetaLibrary.VcMeta calldata _vcMeta) public {
        vcMetaStorage.registerVcMeta(_vcMeta);
        emit VCIssued(_vcMeta.id, msg.sender, _vcMeta.issuer.did);
    }

    function getVcmetaData(
        string calldata _id
    ) public view returns (VcMetaLibrary.VcMeta memory) {
        VcMetaLibrary.VcMeta memory vcMeta = vcMetaStorage.getVcMeta(_id);
        return vcMeta;
    }

    function updateVcStats(
        string calldata _vcId,
        string calldata _status
    ) public {
        vcMetaStorage.updateVcMetaStatus(_vcId, _status);
        emit VCStatus(_vcId, msg.sender, _status);
    }
}
