import com.bradyrussell.uiscoin.BytesUtil;
import com.bradyrussell.uiscoin.Hash;
import com.bradyrussell.uiscoin.Keys;
import com.bradyrussell.uiscoin.blockchain.BlockBuilder;
import com.bradyrussell.uiscoin.storage.BlockchainStorage;
import com.bradyrussell.uiscoin.storage.BlockchainStorageFlatfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.time.Instant;

public class TestBlockchain {
    @Test @Disabled
    void TestCreateGenesisBlock() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());

        KeyPair keyPair = Keys.makeKeyPair();

        if(bs.getBlockHeight() >= 0) {
            return;
        }

        BlockBuilder candidate = new BlockBuilder()
                .setVersion()
                .calculateTime()
                .setNonce(Integer.MIN_VALUE)
                .setDescription("BlockBuilder test")
                .setMinerComment()
                .calculateMerkleRoot()
                .setHashPreviousBlock(Hash.getSHA512Bytes("blockchain radio"))
                .setBlockHeight(0)
                .setDifficultyTarget(1)
                .setAudio(Files.readAllBytes(Path.of("whitenoise.ogg")), keyPair);

        System.out.println("Begin mining at difficulty "+candidate.getBlockHeader().DifficultyTarget);

        long startTime = Instant.now().getEpochSecond();
        long startNonce = candidate.getBlockHeader().Nonce;

        while(!candidate.getBlockHeader().verifyProofOfWork()) {
            candidate.setNonce(candidate.getBlockHeader().Nonce+1);
            if(candidate.getBlockHeader().Nonce % 1000000 == 0) {
                double megahashes = (candidate.getBlockHeader().Nonce - startNonce) / 1000000.0;
                System.out.println(megahashes / (Instant.now().getEpochSecond() - startTime) + " MH/s");
                int difficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                candidate.calculateTime();
                candidate.calculateDifficultyTarget(bs);
                int newDifficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                if(difficultyTarget != newDifficultyTarget) {
                    System.out.println("Adjusted difficulty to "+newDifficultyTarget);
                }
                candidate.setMinerComment("Mined after "+((candidate.getBlockHeader().Nonce - startNonce) / 1000000)+" megahashes.");
            }
        }

        if(!candidate.getBlock().verify(bs)) {
            System.out.println("Failed to verify block!");
            System.out.println(candidate.getBlock());
        }

        System.out.println(BytesUtil.base64Encode(candidate.getBlockHeader().getHash()));

        bs.putBlock(candidate.getBlock());

        bs.close();
    }

    @Test @Disabled
    void TestBlockchain() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());

        KeyPair keyPair = Keys.makeKeyPair();

        BlockBuilder candidate = new BlockBuilder()
                .setVersion()
                .calculateTime()
                .setNonce(Integer.MIN_VALUE)
                .setDescription("BlockBuilder test")
                .setMinerComment()
                .calculateMerkleRoot()
                .calculateHashPreviousBlock(bs)
                .calculateBlockHeight(bs)
                .calculateDifficultyTarget(bs)
                .setAudio(Files.readAllBytes(Path.of("whitenoise.ogg")), keyPair);

        System.out.println("Begin mining at difficulty "+candidate.getBlockHeader().DifficultyTarget);

        long startTime = Instant.now().getEpochSecond();
        long startNonce = candidate.getBlockHeader().Nonce;

        if(!candidate.getBlock().verifyCandidate(bs)) {
            System.out.println("Failed to verify candidate!");
            System.out.println(candidate.getBlock());
        }

        while(!candidate.getBlockHeader().verifyProofOfWork()) {
            candidate.setNonce(candidate.getBlockHeader().Nonce+1);
            if(candidate.getBlockHeader().Nonce % 1000000 == 0) {
                double megahashes = (candidate.getBlockHeader().Nonce - startNonce) / 1000000.0;
                System.out.println(megahashes / (Instant.now().getEpochSecond() - startTime) + " MH/s");
                int difficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                candidate.calculateTime();
                candidate.calculateDifficultyTarget(bs);
                int newDifficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                if(difficultyTarget != newDifficultyTarget) {
                    System.out.println("Adjusted difficulty to "+newDifficultyTarget);
                }
            }
        }

        if(!candidate.getBlock().verify(bs)) {
            System.out.println("Failed to verify block!");
            System.out.println(candidate.getBlock());
        }

        System.out.println(BytesUtil.base64Encode(candidate.getBlockHeader().getHash()));

        bs.putBlock(candidate.getBlock());

        bs.close();
    }
}
