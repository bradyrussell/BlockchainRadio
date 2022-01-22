package com.bradyrussell.uiscoin.blockchain;

import com.bradyrussell.uiscoin.*;
import com.bradyrussell.uiscoin.storage.BlockchainStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class BlockHeader implements BlockchainSerializable, BlockchainVerifiable {
    public int Version; // 4
    public byte[] HashPreviousBlock; // 64
    public byte[] HashMerkleRoot;  // 64
    public byte[] AudioHash;  // 64
    public byte[] ContentSignature;  // 73 [byte length] [signature] [byte 0]... to pad out to 73
    public byte[] SignaturePublicKey; // 88
    public long Time; // 8
    public int DifficultyTarget;  // 4 //https://en.bitcoinwiki.org/wiki/Difficulty_in_Mining#:~:text=Difficulty%20is%20a%20value%20used,a%20lower%20limit%20for%20shares.
    public int Nonce; // 4
    public int BlockHeight; // 4
    private String description; // Constants.MaxDescriptionBytes (128)
    private String minerComment; // Constants.MaxDescriptionBytes (128)

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getDescriptionBytes() {
        if(description == null) {
            return new byte[Constants.MaxDescriptionBytes];
        }
        return description.getBytes(StandardCharsets.UTF_8);
    }

    public void setDescriptionBytes(byte[] description) {
        this.description = new String(description, StandardCharsets.UTF_8);
    }

    public String getMinerComment() {
        return minerComment;
    }

    public void setMinerComment(String minerComment) {
        this.minerComment = minerComment;
    }

    public byte[] getMinerCommentBytes() {
        if(minerComment == null) {
            return new byte[Constants.MaxDescriptionBytes];
        }
        return minerComment.getBytes(StandardCharsets.UTF_8);
    }

    public void setMinerCommentBytes(byte[] minerCommentBytes) {
        this.minerComment = new String(minerCommentBytes, StandardCharsets.UTF_8);
    }

    public void setPaddedContentSignature(byte[] audioHashSignature) {
        byte[] lengthAndSignature = BytesUtil.concatArray(new byte[]{(byte) audioHashSignature.length}, audioHashSignature);
        int paddingLength = 73 - lengthAndSignature.length;
        byte[] paddingBytes = new byte[paddingLength];
        ContentSignature = BytesUtil.concatArray(lengthAndSignature, paddingBytes);
    }

    public byte[] getUnpaddedContentSignature() {
        int signatureLength = Byte.toUnsignedInt(ContentSignature[0]);
        byte[] signature = new byte[signatureLength];
        System.arraycopy(ContentSignature, 1, signature, 0, signatureLength);
        return signature;
    }

    public void sign(KeyPair keyPair) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if(AudioHash == null) {
            throw new NullPointerException("AudioHash is null!");
        }
        if(description == null) {
            throw new NullPointerException("Description is null!");
        }
        Keys.SignedData signedData = Keys.signData(keyPair, BytesUtil.concatArray(AudioHash, getDescriptionHash()));
        setPaddedContentSignature(signedData.signature);
        SignaturePublicKey = signedData.publicKey;
    }

    public boolean verifySignature() throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        if(AudioHash == null) {
            throw new NullPointerException("AudioHash is null!");
        }
        if(description == null) {
            throw new NullPointerException("Description is null!");
        }
        return Keys.verifySignedData(new Keys.SignedData(SignaturePublicKey, getUnpaddedContentSignature(), BytesUtil.concatArray(AudioHash, getDescriptionHash())));
    }

    public byte[] getDescriptionHash() {
        byte[] descriptionBytes = new byte[Constants.MaxDescriptionBytes];
        byte[] originalBytes = getDescriptionBytes();
        System.arraycopy(originalBytes, 0, descriptionBytes, 0, Math.min(originalBytes.length, Constants.MaxDescriptionBytes));
        return Hash.getSHA512Bytes(descriptionBytes);
    }

    public byte[] getPublicKeyHash() {
        return Hash.getSHA512Bytes(SignaturePublicKey);
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray32(Version));

        if(HashPreviousBlock == null) {
            HashPreviousBlock = new byte[64];
        }
        byteArrayOutputStream.writeBytes(HashPreviousBlock);

        if(HashMerkleRoot == null) {
            HashMerkleRoot = new byte[64];
        }
        byteArrayOutputStream.writeBytes(HashMerkleRoot);

        if(AudioHash == null) {
            AudioHash = new byte[64];
        }
        byteArrayOutputStream.writeBytes(AudioHash);

        if(ContentSignature == null) {
            ContentSignature = new byte[73];
        }
        byteArrayOutputStream.writeBytes(ContentSignature);

        if(SignaturePublicKey == null) {
            SignaturePublicKey = new byte[88];
        }
        byteArrayOutputStream.writeBytes(SignaturePublicKey);
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray64(Time));
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray32(DifficultyTarget));
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray32(Nonce));
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray32(BlockHeight));

        byte[] descriptionBytes = new byte[Constants.MaxDescriptionBytes];
        byte[] originalBytes = getDescriptionBytes();

        System.arraycopy(originalBytes, 0, descriptionBytes, 0, Math.min(originalBytes.length, Constants.MaxDescriptionBytes));

        byteArrayOutputStream.writeBytes(descriptionBytes);

        byte[] minerCommentBytes = new byte[Constants.MaxDescriptionBytes];
        byte[] originalMinerCommentBytes = getMinerCommentBytes();

        System.arraycopy(originalMinerCommentBytes, 0, minerCommentBytes, 0, Math.min(originalMinerCommentBytes.length, Constants.MaxDescriptionBytes));

        byteArrayOutputStream.writeBytes(minerCommentBytes);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void deserialize(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        byte[] versionBytes = new byte[4];
        byteArrayInputStream.readNBytes(versionBytes, 0, versionBytes.length);
        Version = BytesUtil.byteArrayToNumber32(versionBytes);
        HashPreviousBlock = new byte[64];
        byteArrayInputStream.readNBytes(HashPreviousBlock, 0, HashPreviousBlock.length);
        HashMerkleRoot = new byte[64];
        byteArrayInputStream.readNBytes(HashMerkleRoot, 0, HashMerkleRoot.length);
        AudioHash = new byte[64];
        byteArrayInputStream.readNBytes(AudioHash, 0, AudioHash.length);
        ContentSignature = new byte[73];
        byteArrayInputStream.readNBytes(ContentSignature, 0, ContentSignature.length);
        SignaturePublicKey = new byte[88];
        byteArrayInputStream.readNBytes(SignaturePublicKey, 0, SignaturePublicKey.length);
        byte[] timeBytes = new byte[8];
        byteArrayInputStream.readNBytes(timeBytes, 0, timeBytes.length);
        Time = BytesUtil.byteArrayToNumber64(timeBytes);
        byte[] difficultyBytes = new byte[4];
        byteArrayInputStream.readNBytes(difficultyBytes, 0, difficultyBytes.length);
        DifficultyTarget = BytesUtil.byteArrayToNumber32(difficultyBytes);
        byte[] nonceBytes = new byte[4];
        byteArrayInputStream.readNBytes(nonceBytes, 0, nonceBytes.length);
        Nonce = BytesUtil.byteArrayToNumber32(nonceBytes);
        byte[] blockHeightBytes = new byte[4];
        byteArrayInputStream.readNBytes(blockHeightBytes, 0, blockHeightBytes.length);
        BlockHeight = BytesUtil.byteArrayToNumber32(blockHeightBytes);
        byte[] descriptionBytes = new byte[Constants.MaxDescriptionBytes];
        byteArrayInputStream.readNBytes(descriptionBytes, 0, descriptionBytes.length);

        int descriptionLength = -1;
        for (int i = 0; i < descriptionBytes.length; i++) {
            if(descriptionBytes[i] == (byte)0) {
                descriptionLength = i;
                break;
            }
        }

        if(descriptionLength > 0) {
            byte[] trimmedDescription = new byte[descriptionLength];
            System.arraycopy(descriptionBytes, 0, trimmedDescription, 0, trimmedDescription.length);
            descriptionBytes = trimmedDescription;
        }

        setDescriptionBytes(descriptionBytes);

        byte[] minerCommentBytes = new byte[Constants.MaxDescriptionBytes];
        byteArrayInputStream.readNBytes(minerCommentBytes, 0, minerCommentBytes.length);

        int minerCommentLength = -1;
        for (int i = 0; i < minerCommentBytes.length; i++) {
            if(minerCommentBytes[i] == (byte)0) {
                minerCommentLength = i;
                break;
            }
        }

        if(minerCommentLength > 0) {
            byte[] trimmedMinerComment = new byte[minerCommentLength];
            System.arraycopy(minerCommentBytes, 0, trimmedMinerComment, 0, trimmedMinerComment.length);
            minerCommentBytes = trimmedMinerComment;
        }

        setMinerCommentBytes(minerCommentBytes);
    }

    @Override
    public boolean verify(BlockchainStorage blockchain) {
        return verifyCandidate(blockchain) && verifyProofOfWork();
    }

    public boolean verifyCandidate(BlockchainStorage blockchain) {
        if(BlockHeight > 0) {
            BlockHeader previousBlockHeader;
            try {
                previousBlockHeader = blockchain.getBlockHeader(HashPreviousBlock);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                return false;
            }

            if (BlockHeight != previousBlockHeader.BlockHeight + 1) {
                System.out.println("Invalid blockheight!");
                return false;
            }

            if (DifficultyTarget < calculateDifficultyTarget(Time - previousBlockHeader.Time, previousBlockHeader.DifficultyTarget)) {
                System.out.println("Difficulty does not meet target!");
                return false;
            }
        }

        try {
            return getDescriptionBytes().length <= Constants.MaxDescriptionBytes &&
                    HashPreviousBlock.length == 64 &&
                    HashMerkleRoot.length == 64 &&
                    AudioHash.length == 64 &&
                    ContentSignature.length == 73 &&
                    SignaturePublicKey.length == 88 &&
                    verifySignature();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyProofOfWork() {
        return Hash.validateHash(getHash(), DifficultyTarget);
    }

    @Override
    public byte[] getHash() {
        return Hash.getSHA512Bytes(serialize());
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "Version=" + Version +
                ", HashPreviousBlock=" + Arrays.toString(HashPreviousBlock) +
                ", HashMerkleRoot=" + Arrays.toString(HashMerkleRoot) +
                ", AudioHash=" + Arrays.toString(AudioHash) +
                ", ContentSignature=" + Arrays.toString(ContentSignature) +
                ", SignaturePublicKey=" + Arrays.toString(SignaturePublicKey) +
                ", Time=" + Time +
                ", DifficultyTarget=" + DifficultyTarget +
                ", Nonce=" + Nonce +
                ", BlockHeight=" + BlockHeight +
                ", description='" + description + '\'' +
                '}';
    }

    public static int calculateDifficultyTarget(long timeSinceLastBlock, int lastBlockDifficulty) {
        if(timeSinceLastBlock < Constants.TargetSecondsPerBlock) return Math.min(64, lastBlockDifficulty + 1);
        if(timeSinceLastBlock > Constants.TargetSecondsPerBlock) return Math.max(3, lastBlockDifficulty - 1);
        return lastBlockDifficulty;
    }
}
