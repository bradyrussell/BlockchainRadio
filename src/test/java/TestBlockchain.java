import com.bradyrussell.uiscoin.BytesUtil;
import com.bradyrussell.uiscoin.Hash;
import com.bradyrussell.uiscoin.Keys;
import com.bradyrussell.uiscoin.blockchain.Block;
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
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TestBlockchain {
    @Test
    @Disabled
    void TestCreateGenesisBlock() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());

        KeyPair keyPair = Keys.makeKeyPair();

        if (bs.getBlockHeight() >= 0) {
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

        System.out.println("Begin mining at difficulty " + candidate.getBlockHeader().DifficultyTarget);

        long startTime = Instant.now().getEpochSecond();
        long startNonce = candidate.getBlockHeader().Nonce;

        while (!candidate.getBlockHeader().verifyProofOfWork()) {
            candidate.setNonce(candidate.getBlockHeader().Nonce + 1);
            if (candidate.getBlockHeader().Nonce % 1000000 == 0) {
                double megahashes = (candidate.getBlockHeader().Nonce - startNonce) / 1000000.0;
                System.out.println(megahashes / (Instant.now().getEpochSecond() - startTime) + " MH/s");
                int difficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                candidate.calculateTime();
                candidate.calculateDifficultyTarget(bs);
                int newDifficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                if (difficultyTarget != newDifficultyTarget) {
                    System.out.println("Adjusted difficulty to " + newDifficultyTarget);
                }
                candidate.setMinerComment("Mined after " + ((candidate.getBlockHeader().Nonce - startNonce) / 1000000) + " megahashes.");
            }
        }

        if (!candidate.getBlock().verify(bs)) {
            System.out.println("Failed to verify block!");
            System.out.println(candidate.getBlock());
        }

        System.out.println(BytesUtil.base64Encode(candidate.getBlockHeader().getHash()));

        bs.putBlock(candidate.getBlock());

        bs.close();
    }

    @Test
    @Disabled
    void TestBlockchainAdd() throws GeneralSecurityException, IOException {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());

        byte[] bytes = Files.readAllBytes(Path.of("brady.privkey"));
        KeyPair keyPair = Keys.getKeypairFromPrivateKey(bytes);
        //KeyPair keyPair = Keys.makeKeyPair();

        BlockBuilder candidate = new BlockBuilder()
                .setVersion()
                .calculateTime()
                .setNonce(Integer.MIN_VALUE)
                .setDescription("Enjoy Through the Fire and Flames by Dragonforce!")
                .setMinerComment("Brought to you by Brady")
                .calculateMerkleRoot()
                .calculateHashPreviousBlock(bs)
                .calculateBlockHeight(bs)
                .calculateDifficultyTarget(bs)
                .setAudio(Files.readAllBytes(Path.of("C:\\Users\\brady\\Desktop\\ttfaf.ogg")), keyPair);

        System.out.println("Begin mining at difficulty " + candidate.getBlockHeader().DifficultyTarget);

        long startTime = Instant.now().getEpochSecond();
        long startNonce = candidate.getBlockHeader().Nonce;

        if (!candidate.getBlock().verifyCandidate(bs)) {
            System.out.println("Failed to verify candidate!");
            System.out.println(candidate.getBlock());
        }

        while (!candidate.getBlockHeader().verifyProofOfWork()) {
            candidate.setNonce(candidate.getBlockHeader().Nonce + 1);
            if (candidate.getBlockHeader().Nonce % 1000000 == 0) {
                double megahashes = (candidate.getBlockHeader().Nonce - startNonce) / 1000000.0;
                System.out.println(megahashes / (Instant.now().getEpochSecond() - startTime) + " MH/s");
                int difficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                candidate.calculateTime();
                candidate.calculateDifficultyTarget(bs);
                int newDifficultyTarget = candidate.getBlockHeader().DifficultyTarget;
                if (difficultyTarget != newDifficultyTarget) {
                    System.out.println("Adjusted difficulty to " + newDifficultyTarget);
                }
            }
        }

        if (!candidate.getBlock().verify(bs)) {
            System.out.println("Failed to verify block!");
            System.out.println(candidate.getBlock());
        }

        System.out.println(BytesUtil.base64Encode(candidate.getBlockHeader().getHash()));

        bs.putBlock(candidate.getBlock());

        bs.close();
    }

    @Test @Disabled
    void TestMultiThreadedMiner() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());

        byte[] bytes = Files.readAllBytes(Path.of("brady.privkey"));
        KeyPair keyPair = Keys.getKeypairFromPrivateKey(bytes);

        boolean extraEntropyUsingMinerComment = true;

        BlockBuilder candidate = new BlockBuilder()
                .setVersion()
                .calculateTime()
                .setNonce(Integer.MIN_VALUE)
                .setDescription("Enjoy Through the Fire and Flames by Dragonforce!")
                .setMinerComment("Brought to you by Brady")
                .calculateMerkleRoot()
                .calculateHashPreviousBlock(bs)
                .calculateBlockHeight(bs)
                .calculateDifficultyTarget(bs)
                .setAudio(Files.readAllBytes(Path.of("C:\\Users\\brady\\Desktop\\ttfaf.ogg")), keyPair);

        Block candidateBlock = candidate.getBlock();
        if (!candidateBlock.verifyCandidate(bs)) {
            System.out.println("Failed to verify candidate!");
            System.out.println(candidate.getBlock());
        }

        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("Begin mining at difficulty " + candidate.getBlockHeader().DifficultyTarget+" with "+threads+" threads.");

        long startTime = Instant.now().getEpochSecond();
        long difficultyStartTime = Instant.now().getEpochSecond();
        AtomicLong megahashes = new AtomicLong();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CompletionService<Block> completionService = new ExecutorCompletionService<>(executor);

        System.out.println(threads);

        long range = (long)Integer.MAX_VALUE - (long)Integer.MIN_VALUE;
        long chunk = range / threads;
        System.out.println(chunk);
        for (int i = 0; i < threads; i++) {
            int extraEntropy = ThreadLocalRandom.current().nextInt();
            int nonce = Integer.MIN_VALUE + (i * Math.toIntExact(chunk));
            System.out.println(nonce);
            completionService.submit(() -> {
                BlockBuilder threadCandidate = BlockBuilder.fromBlock(candidateBlock);
                threadCandidate.setNonce(nonce);
                if(extraEntropyUsingMinerComment) {
                    threadCandidate.setMinerComment(threadCandidate.getBlockHeader().getMinerComment()+BytesUtil.bytesToHex(BytesUtil.numberToByteArray32(extraEntropy)));
                }

                while (!threadCandidate.getBlockHeader().verifyProofOfWork()) {
                    threadCandidate.getBlockHeader().Nonce++;
                    if (threadCandidate.getBlockHeader().Nonce % 1000000 == 0) {
                        megahashes.addAndGet(1);
                        threadCandidate.calculateTime();
                        threadCandidate.calculateDifficultyTarget(bs);
                    }
                }

                return threadCandidate.getBlock();
            });
        }

        Future<Block> blockFuture = null;
        while(blockFuture == null) {
            blockFuture = completionService.poll();
            Thread.sleep(1000);
            long elapsed = Instant.now().getEpochSecond() - startTime;
            int previousDifficultyTarget = candidate.getBlockHeader().DifficultyTarget;
            candidate.calculateTime();
            candidate.calculateDifficultyTarget(bs);
            int difficultyTarget = candidate.getBlockHeader().DifficultyTarget;
            if(difficultyTarget != previousDifficultyTarget) {
                difficultyStartTime = Instant.now().getEpochSecond();
            }
            long difficultyElapsed = Instant.now().getEpochSecond() - difficultyStartTime;
            long blockElapsed = Instant.now().getEpochSecond() - bs.getBlockByHeight(candidate.getBlockHeader().BlockHeight - 1).getHeader().Time;
            double hashrate = megahashes.get() / (double) elapsed;
            System.out.println(Math.round(hashrate * 100.)/100. +" MH/s \t\t| Difficulty: "+ difficultyTarget +" \t\t| Time Elapsed: "+elapsed+" seconds"+" \t\t| Time At This Difficulty: "+difficultyElapsed+" seconds"+" \t\t| Time Since Last Block: "+blockElapsed+" seconds");
        }

        Block resultBlock = blockFuture.get();

        if (!resultBlock.verify(bs)) {
            System.out.println("Failed to verify block!");
            System.out.println(resultBlock);
        }

        System.out.println(BytesUtil.base64Encode(resultBlock.getHeader().getHash()));

        bs.putBlock(resultBlock);

        bs.close();
    }

    @Test @Disabled
    void TestBlockchainVerify() {
        BlockchainStorage bs = new BlockchainStorageFlatfile("blockchaindb");
        bs.open();
        Assertions.assertTrue(bs.isOperational());
        Assertions.assertTrue(bs.verify());
        bs.close();
    }

    @Test @Disabled
    void TestVanityAddress() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InterruptedException, ExecutionException, IOException {
        String target = "brady";

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<byte[]> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            completionService.submit(() -> {
                try {
                    KeyPair keys;
                    do {
                        keys = Keys.makeKeyPair();
                    } while (!BytesUtil.base64Encode(Hash.getSHA512Bytes(keys.getPublic().getEncoded())).toLowerCase(Locale.ROOT).startsWith(target));
                    return keys.getPrivate().getEncoded();
                } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }

        byte[] resultKey = completionService.take().get();
        System.out.println(BytesUtil.bytesToHex(resultKey));
        Files.write(Path.of(target+".privkey"), resultKey);
    }

    @Test @Disabled
    void TestLoadVanityAddress() throws IOException, GeneralSecurityException {
        byte[] bytes = Files.readAllBytes(Path.of("brady.privkey"));
        KeyPair keys = Keys.getKeypairFromPrivateKey(bytes);

        System.out.println(BytesUtil.base64Encode(Hash.getSHA512Bytes(keys.getPublic().getEncoded())));
    }
}
