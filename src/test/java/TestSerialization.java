import com.bradyrussell.uiscoin.BytesUtil;
import com.bradyrussell.uiscoin.Constants;
import com.bradyrussell.uiscoin.Hash;
import com.bradyrussell.uiscoin.Keys;
import com.bradyrussell.uiscoin.blockchain.Block;
import com.bradyrussell.uiscoin.blockchain.BlockBuilder;
import com.bradyrussell.uiscoin.blockchain.BlockHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;

public class TestSerialization {
    @Test
    void TestBlockHeaderSerialization() {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.BlockHeight = 1;
        blockHeader.HashPreviousBlock = Hash.getSHA512Bytes("bytes");
        blockHeader.DifficultyTarget = 3;
        blockHeader.HashMerkleRoot = Hash.getSHA512Bytes("bytes2");
        blockHeader.AudioHash = Hash.getSHA512Bytes("bytes3");
        blockHeader.Nonce = 1;
        blockHeader.Time = 1;
        blockHeader.Version = 2;
        blockHeader.setMinerComment("");
        blockHeader.setDescription("cqbfgckipaioiyofmicbdxxkukpxgquwxlrdiopflsyxpmxlzbtocxtzysuyuajnvrjdeseiecaexeqwqnnkjfjwukrevadbyiiriarctzjxfzqubozivrvktjlhrvyv");

        byte[] serialize = blockHeader.serialize();

        BlockHeader blockheader1 = new BlockHeader();
        blockheader1.deserialize(serialize);

        System.out.println(blockHeader);
        System.out.println(blockheader1);

        System.out.println(Arrays.toString(serialize));
        System.out.println(Arrays.toString(blockheader1.serialize()));
        Assertions.assertArrayEquals(blockheader1.serialize(), serialize);

        System.out.println(serialize.length);
    }

    @Test
    void TestBlockSerialization() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\brady\\Desktop\\untitled.ogg"));
        KeyPair keyPair = Keys.makeKeyPair();

        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.BlockHeight = 1;
        blockHeader.HashPreviousBlock = Hash.getSHA512Bytes(bytes);
        blockHeader.DifficultyTarget = 3;
        blockHeader.HashMerkleRoot = Hash.getSHA512Bytes(bytes);
        blockHeader.AudioHash = Hash.getSHA512Bytes(bytes);
        blockHeader.Nonce = 1;
        blockHeader.Time = 1;
        blockHeader.Version = 1;
        blockHeader.setDescription("cqbfgckipaioiyofmicbdxxkukpxgquwxlrdiopflsyxpmxlzbtocxtzysuyuajnvrjdeseiecaexeqwqnnkjfjwukrevadbyiiriarctzjxfzqubozivrvktjlhrvyv");
        blockHeader.setMinerComment("");
        blockHeader.sign(keyPair);
        block.setHeader(blockHeader);

        block.setAudio(bytes);

        byte[] serialize = block.serialize();

        Assertions.assertEquals(Constants.HeaderSizeBytes, block.getHeader().serialize().length);

        Block block1 = new Block();
        block1.deserialize(serialize);

        Assertions.assertArrayEquals(serialize, block1.serialize());

        Assertions.assertTrue(block1.getHeader().verifySignature());
        System.out.println(block.getHeader());
        System.out.println(block1.getHeader());
    }

    @Test
    void TestBlockBuilder() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPair keyPair = Keys.makeKeyPair();

        BlockBuilder candidate = new BlockBuilder()
                .setVersion()
                .setTime(Instant.now().getEpochSecond())
                .setBlockHeight(0)
                .setDifficultyTarget(3)
                .setNonce(Integer.MIN_VALUE)
                .setDescription("BlockBuilder test")
                .setMinerComment()
                .setAudio(Files.readAllBytes(Path.of("C:\\Users\\brady\\Desktop\\untitled.ogg")), keyPair);

        while(Hash.getHashDifficulty(candidate.getBlockHeader().getHash()) < 3) {
            candidate.setNonce(candidate.getBlockHeader().Nonce+1);
        }
        System.out.println(BytesUtil.base64Encode(candidate.getBlockHeader().getHash()));
    }
}
