package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.storage.BlockchainStorage;

public interface BlockchainVerifiable {
    boolean verify(BlockchainStorage blockchain);
}
