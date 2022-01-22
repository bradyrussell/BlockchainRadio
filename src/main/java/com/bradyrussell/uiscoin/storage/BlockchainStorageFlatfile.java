package com.bradyrussell.uiscoin.storage;

import com.bradyrussell.uiscoin.blockchain.Block;
import com.bradyrussell.uiscoin.blockchain.BlockBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlockchainStorageFlatfile extends BlockchainStorageInMemory {
    private final String directory;

    public BlockchainStorageFlatfile(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean open() {
        try {
            if(!Files.isDirectory(Path.of(directory))) {
                Files.createDirectories(Path.of(directory));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.list(Path.of(directory)).forEach((path -> {
                if(!path.getFileName().toString().startsWith("block_")) {
                    return;
                }
                try {
                    byte[] bytes = Files.readAllBytes(path);
                    Block block = new Block();
                    block.deserialize(bytes);
                    putBlock(block);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.open();
    }

    @Override
    public boolean close() {
        for (Block block : getBlockchain()) {
            try {
                Files.write(Path.of(directory, "block_"+block.getHeader().BlockHeight), block.serialize());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.close();
    }

    @Override
    public boolean isOperational() {
        return super.isOperational();
    }
}
