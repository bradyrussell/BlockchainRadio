/* (C) Brady Russell 2021 */
package com.bradyrussell.uiscoin.storage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.bradyrussell.uiscoin.BytesUtil;
import com.bradyrussell.uiscoin.blockchain.Block;
import com.bradyrussell.uiscoin.blockchain.BlockHeader;

public class BlockchainStorageInMemory implements BlockchainStorage {
    protected final ArrayList<Block> blocksByHeight = new ArrayList<>(); // this should be all that needs to be stored, everything else can be reconstructed
    private final HashMap<String, Block> blocks = new HashMap<>();
    private final AtomicInteger blockheight = new AtomicInteger(-1);
    private final HashMap<String, Block> candidateBlocks = new HashMap<>();

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public boolean isOperational() {
        return true;
    }

    @Override
    public boolean verify() {
        for (Block block : getBlockchain()) {
            if(!block.verify(this)) return false;
        }
        return true;
    }

    @Override
    public boolean verifyRange(int beginHeight, int endHeight) /**/ {
        for (Block block : getBlockchainRange(beginHeight, endHeight)) {
            if(!block.verify(this)) return false;
        }
        return true;
    }

    @Override
    public int getBlockHeight() {
        return blockheight.get();
    }

    @Override
    public List<Block> getBlockchain() {
        return blocksByHeight;
    }

    @Override
    public List<Block> getBlockchainRange(int beginHeight, int endHeight)/* */ {
        if(beginHeight < 0 || beginHeight > blockheight.get() || endHeight < 0 || endHeight > blockheight.get() || beginHeight > endHeight) throw new UnsupportedOperationException("Cannot find block");
        return blocksByHeight.subList(beginHeight, endHeight+1);
    }

    @Override
    public byte[] getHighestBlockHash() /**/ {
        return getBlockHeaderByHeight(blockheight.get()).getHash();
    }

    @Override
    public Block getHighestBlock() /**/ {
        return getBlockByHeight(blockheight.get());
    }

    @Override
    public boolean hasBlockHeader(byte[] blockHash) {
        return hasBlock(blockHash);
    }

    @Override
    public BlockHeader getBlockHeader(byte[] blockHash)/* */ {
        if(!blocks.containsKey(BytesUtil.base64Encode(blockHash))) throw new UnsupportedOperationException("Cannot find block header for block "+BytesUtil.base64Encode(blockHash));
        return blocks.get(BytesUtil.base64Encode(blockHash)).getHeader();
    }

    @Override
    public BlockHeader getBlockHeaderByHeight(int height) /**/ {
        return getBlockByHeight(height).getHeader();
    }

    @Override
    public boolean hasBlock(byte[] blockHash) {
        return blocks.containsKey(BytesUtil.base64Encode(blockHash));
    }

    @Override
    public Block getBlock(byte[] blockHash)  {
        if(!blocks.containsKey(BytesUtil.base64Encode(blockHash))) throw new UnsupportedOperationException("Cannot find block "+BytesUtil.base64Encode(blockHash));
        return blocks.get(BytesUtil.base64Encode(blockHash));
    }

    @Override
    public Block getBlockByHeight(int height)  {
        if(height < 0 || height > blockheight.get()) throw new UnsupportedOperationException("Cannot find block "+height);
        return blocksByHeight.get(height);
    }

    @Override
    public boolean putBlock(Block block) {
        if(blockheight.get() < block.getHeader().BlockHeight) {
            blockheight.set(block.getHeader().BlockHeight);
            System.out.println("Block Height is now "+blockheight.get());
        }
        blocks.put(BytesUtil.base64Encode(block.getHeader().getHash()), block);
        while(blocksByHeight.size() < block.getHeader().BlockHeight + 1) blocksByHeight.add(null);
        blocksByHeight.set(block.getHeader().BlockHeight, block);
        return true;
    }

    @Override
    public boolean putBlockHeader(BlockHeader blockHeader) {
        return true;
    }

    @Override
    public boolean putCandidateBlock(Block candidateBlock) {
        candidateBlocks.put(BytesUtil.base64Encode(candidateBlock.getHeader().ContentSignature), candidateBlock);
        return true;
    }

    @Override
    public List<Block> getCandidateBlocks() {
        return new ArrayList<>(candidateBlocks.values());
    }
}