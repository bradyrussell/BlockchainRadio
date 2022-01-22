package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.blockchain.Block;
import com.bradyrussell.uiscoin.blockchain.BlockHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\brady\\Desktop\\untitled.ogg"));
        System.out.println(bytes.length);
        System.out.println(Arrays.toString(Hash.getSHA512Bytes(bytes)));

        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.BlockHeight = 1;
        blockHeader.HashPreviousBlock = Hash.getSHA512Bytes(bytes);
        blockHeader.DifficultyTarget = 3;
        blockHeader.HashMerkleRoot = Hash.getSHA512Bytes(bytes);
        blockHeader.Nonce = 1;
        blockHeader.Time = 1;
        blockHeader.Version = 1;
        blockHeader.setDescription("yo");
        block.setHeader(blockHeader);

        block.setAudio(bytes);

        byte[] serialize = block.serialize();
        System.out.println(BytesUtil.base64Encode(serialize));

        Block block1 = new Block();
        block1.deserialize(serialize);

        System.out.println(Arrays.equals(block1.serialize(), serialize));
    }
}
