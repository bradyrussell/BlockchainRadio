package com.bradyrussell.uiscoin;

public interface BlockchainSerializable {
    byte[] serialize();
    void deserialize(byte[] data);
    byte[] getHash();
}
