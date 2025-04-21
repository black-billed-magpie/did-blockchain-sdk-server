// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

library StringUtils {
    function toString(uint256 _value) internal pure returns (string memory) {
        if (_value == 0) {
            return "0";
        }
        uint256 temp = _value;
        uint256 digits;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }
        bytes memory buffer = new bytes(digits);
        while (_value != 0) {
            digits -= 1;
            buffer[digits] = bytes1(uint8(48 + uint256(_value % 10)));
            _value /= 10;
        }
        return string(buffer);
    }

    function stringToUint(string memory _s) internal pure returns (uint256) {
        bytes memory b = bytes(_s);
        uint256 result = 0;
        for (uint256 i = 0; i < b.length; i++) {
            require(
                b[i] >= 0x30 && b[i] <= 0x39,
                "Invalid character in string"
            ); // Ensure it's a digit
            result = result * 10 + (uint256(uint8(b[i])) - 48);
        }
        return result;
    }

    function isEqual(
        string calldata _a,
        string calldata _b
    ) internal pure returns (bool) {
        return
            keccak256(abi.encodePacked(_a)) == keccak256(abi.encodePacked(_b));
    }
}
