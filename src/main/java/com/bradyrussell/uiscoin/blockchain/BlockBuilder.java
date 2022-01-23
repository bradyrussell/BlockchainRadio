package com.bradyrussell.uiscoin.blockchain;

import com.bradyrussell.uiscoin.Constants;
import com.bradyrussell.uiscoin.Hash;
import com.bradyrussell.uiscoin.Keys;
import com.bradyrussell.uiscoin.storage.BlockchainStorage;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;

public class BlockBuilder {
    private Block block = null;

    public Block getBlock() {
        if(block == null) {
            block = new Block();
        }
        return block;
    }

    public BlockHeader getBlockHeader() {
        if(getBlock().getHeader() == null) {
            getBlock().setHeader(new BlockHeader());
        }
        return getBlock().getHeader();
    }

    public BlockBuilder setVersion() {
        getBlockHeader().Version = Constants.Version;
        return this;
    }

    public BlockBuilder setVersion(int version) {
        getBlockHeader().Version = version;
        return this;
    }

    public BlockBuilder setTime(long time) {
        getBlockHeader().Time = time;
        return this;
    }

    public BlockBuilder setBlockHeight(int blockHeight) {
        getBlockHeader().BlockHeight = blockHeight;
        return this;
    }

    public BlockBuilder setDifficultyTarget(int difficultyTarget) {
        getBlockHeader().DifficultyTarget = difficultyTarget;
        return this;
    }

    public BlockBuilder setNonce(int nonce) {
        getBlockHeader().Nonce = nonce;
        return this;
    }

    public BlockBuilder setAudio(byte[] audio) {
        getBlock().setAudio(audio);
        getBlockHeader().AudioHash = Hash.getSHA512Bytes(audio);
        return this;
    }

    public BlockBuilder setAudio(byte[] audio, KeyPair keyPair) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        getBlock().setAudio(audio);
        getBlockHeader().AudioHash = Hash.getSHA512Bytes(audio);
        getBlockHeader().sign(keyPair);
        return this;
    }

    public BlockBuilder signAudio(KeyPair keyPair) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        getBlockHeader().AudioHash = Hash.getSHA512Bytes(getBlock().getAudio());
        getBlockHeader().sign(keyPair);
        return this;
    }

    public BlockBuilder setDescription(String description) {
        getBlockHeader().setDescription(description);
        return this;
    }

    public BlockBuilder setHashMerkleRoot(byte[] hashMerkleRoot) {
        getBlockHeader().HashMerkleRoot = hashMerkleRoot;
        return this;
    }

    public BlockBuilder calculateMerkleRoot() {
        return setHashMerkleRoot(new byte[64]);
    }

    public BlockBuilder setHashPreviousBlock(byte[] hashPreviousBlock) {
        getBlockHeader().HashPreviousBlock = hashPreviousBlock;
        return this;
    }

    public BlockBuilder calculateHashPreviousBlock(BlockchainStorage blockchainStorage) {
        return setHashPreviousBlock(blockchainStorage.getHighestBlockHash());
    }

    public BlockBuilder calculateBlockHeight(BlockchainStorage blockchainStorage) {
        return setBlockHeight(blockchainStorage.getBlockHeight() + 1);
    }

    public BlockBuilder calculateDifficultyTarget(BlockchainStorage blockchainStorage) {
        BlockHeader previousBlockHeader = blockchainStorage.getBlockHeader(getBlockHeader().HashPreviousBlock);
        return setDifficultyTarget(BlockHeader.calculateDifficultyTarget(getBlockHeader().Time - previousBlockHeader.Time, previousBlockHeader.DifficultyTarget));
    }

    public BlockBuilder calculateTime() {
        return setTime(Instant.now().getEpochSecond());
    }

    public BlockBuilder setMinerComment(String minerComment) {
        getBlockHeader().setMinerComment(minerComment);
        return this;
    }

    public BlockBuilder setMinerComment() {
        return setMinerComment("");
    }

    public static BlockBuilder fromBlock(Block block) {
        BlockBuilder blockBuilder = new BlockBuilder();
        blockBuilder.setAudio(block.getAudio());
        blockBuilder.setVersion(block.getHeader().Version);
        blockBuilder.setHashPreviousBlock(block.getHeader().HashPreviousBlock);
        blockBuilder.setHashMerkleRoot(block.getHeader().HashMerkleRoot);
        blockBuilder.getBlockHeader().AudioHash = block.getHeader().AudioHash; // already set by setAudio, but we want to allow copying the block exactly
        blockBuilder.getBlockHeader().ContentSignature = block.getHeader().ContentSignature;
        blockBuilder.getBlockHeader().SignaturePublicKey = block.getHeader().SignaturePublicKey;
        blockBuilder.setTime(block.getHeader().Time);
        blockBuilder.setDifficultyTarget(block.getHeader().DifficultyTarget);
        blockBuilder.setNonce(block.getHeader().Nonce);
        blockBuilder.setBlockHeight(block.getHeader().BlockHeight);
        blockBuilder.getBlockHeader().setDescriptionBytes(block.getHeader().getDescriptionBytes());
        blockBuilder.getBlockHeader().setMinerCommentBytes(block.getHeader().getMinerCommentBytes());
        return blockBuilder;
    }
}
