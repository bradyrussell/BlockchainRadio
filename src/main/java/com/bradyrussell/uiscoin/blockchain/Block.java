package com.bradyrussell.uiscoin.blockchain;

import com.bradyrussell.uiscoin.*;
import com.bradyrussell.uiscoin.storage.BlockchainStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Block implements BlockchainSerializable, BlockchainVerifiable {
    private BlockHeader header;
    private byte[] audio;

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    public byte[] getAudio() {
        return audio;
    }

    public void setAudio(byte[] audio) {
        this.audio = audio;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeBytes(header.serialize());
        byteArrayOutputStream.writeBytes(BytesUtil.numberToByteArray32(audio.length));
        byteArrayOutputStream.writeBytes(audio);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void deserialize(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

        byte[] headerBytes = new byte[Constants.HeaderSizeBytes];
        byteArrayInputStream.readNBytes(headerBytes, 0, Constants.HeaderSizeBytes);

        byte[] audioLengthBytes = new byte[4];
        byteArrayInputStream.readNBytes(audioLengthBytes, 0, audioLengthBytes.length);
        int audioLength = BytesUtil.byteArrayToNumber32(audioLengthBytes);

        audio = new byte[audioLength];
        byteArrayInputStream.readNBytes(audio, 0, audioLength);

        header = new BlockHeader();
        header.deserialize(headerBytes);
    }

    @Override
    public byte[] getHash() {
        return header.getHash();
    }

    @Override
    public boolean verify(BlockchainStorage blockchain) {
        return verifyCandidate(blockchain) && header.verify(blockchain);
    }

    //verify everything except block hash meets difficulty
    public boolean verifyCandidate(BlockchainStorage blockchain) {
        return header.verifyCandidate(blockchain) && audio.length <= Constants.MaxAudioBytes && Arrays.equals(Hash.getSHA512Bytes(audio), header.AudioHash);
    }
}
