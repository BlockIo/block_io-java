package lib;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libdohj.params.LitecoinTestNet3Params;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest {
    private long fee;
    private long preOutputValue;
    private NetworkParameters networkParams;
    private ECKey privKey1;
    private ECKey privKey2;

    @BeforeEach
    void setUp() {
        networkParams = LitecoinTestNet3Params.get();
        fee = 10000;
        preOutputValue = 1000000000;
        privKey1 = ECKey.fromPrivate(Hex.decode("ef4fc6cfd682494093bbadf041ba4341afbe22b224432e21a4bc4470c5b939d4"));
        privKey2 = ECKey.fromPrivate(Hex.decode("123f37eb9a7f24a120969a1b2d6ac4859fb8080cfc2e8d703abae0f44305fc12"));
    }

    @Test
    public void TestTransactionP2SHToP2WSHOverP2SH() {
        Address toAddr = Address.fromString(networkParams, "2NBdCdqTMfDssSzmxfEgAU2vMRZZTNFPqUV");

        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
        Transaction newTx = new Transaction(networkParams);

        Script p2shScript = ScriptBuilder.createP2SHOutputScript(redeemScript);

        newTx.addInput(Sha256Hash.wrap("4ad80b9776f574a125f89e96bda75bb6fe046f7560847d16446bbdcdc160be62"), 1, p2shScript).clearScriptBytes();
        newTx.addOutput(Coin.valueOf(preOutputValue - fee), toAddr);

        assertEquals(Helper.txToHexString(newTx), "010000000162be60c1cdbd6b44167d8460756f04feb65ba7bd969ef825a174f576970bd84a0100000000ffffffff01f0a29a3b0000000017a914c99a494597ade09b5194f9ec8e02d96607ae64798700000000");

        Sha256Hash sigHash = newTx.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false);
        assertEquals(sigHash.toString(), "93a075651d1b6b79cd9bf128bf5e15001fe65865defea6cedab0a1da438f565e");

        ECKey.ECDSASignature sig1 = privKey1.sign(sigHash);
        ECKey.ECDSASignature sig2 = privKey2.sign(sigHash);

        TransactionSignature txSig1 = new TransactionSignature(sig1, Transaction.SigHash.ALL, false);
        TransactionSignature txSig2 = new TransactionSignature(sig2, Transaction.SigHash.ALL, false);

        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(ImmutableList.of(txSig1, txSig2), redeemScript);

        newTx.getInput(0).setScriptSig(inputScript);

        assertEquals(Helper.txToHexString(newTx), "010000000162be60c1cdbd6b44167d8460756f04feb65ba7bd969ef825a174f576970bd84a01000000da00473044022009143b07279ef6d5317865672e9fc28ada31314abf242ae786917b92cf027ac002207544d055f2b8bb249dc0294d565c6d538f4e04f9b142331fa103d82e0498a181014830450221009ce297b1eba341be03e0ae656ac0233464c8249d36f3659676b01c45c74808680220252d0c54d56d78b4193cc1c63b7b8dc2a6b9889bed5dc3555571c8aaa1a710e70147522103820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e210238de8c9eb2842ecaf0cc61ee6ba23fe4e46f1cfd82eac0910e1d8e865bd76df952aeffffffff01f0a29a3b0000000017a914c99a494597ade09b5194f9ec8e02d96607ae64798700000000");
        assertEquals(newTx.getTxId().toString(), "754162225c3f2b8ce476d1df7a0b35f04ba6fe24fd6c0fd89e31c5e54d4eaec1");
    }

    @Test
    public void TestTransactionP2WPKHToWitnessV0(){
        String prevTxId = "66a78d3cda988e4c90611b192ae5bd02e0fa70c08c3219110c02594802a42c01";
        long inputValue = preOutputValue - fee - fee;
        long outputValue = inputValue - fee;
        Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(privKey1);
        Script witnessScript = ScriptBuilder.createP2PKHOutputScript(privKey1);

        Address toAddr = Address.fromString(networkParams, "tltc1q6s4cxsg5q4vm0ksst6rxn68h6ksrwumy9tvzgqa6jxuqllxyzh0qxt7q8g");

        Transaction newTx = new Transaction(networkParams);
        newTx.addInput(Sha256Hash.wrap(prevTxId), 0, redeemScript).clearScriptBytes();
        newTx.addOutput(Coin.valueOf(outputValue), toAddr);

        assertEquals(Helper.txToHexString(newTx), "0100000001012ca4024859020c1119328cc070fae002bde52a191b61904c8e98da3c8da7660000000000ffffffff01d0549a3b00000000220020d42b8341140559b7da105e8669e8f7d5a03773642ad82403ba91b80ffcc415de00000000");

        Sha256Hash sigHash = newTx.hashForWitnessSignature(0, witnessScript, Coin.valueOf(inputValue), Transaction.SigHash.ALL, false);
        assertEquals(sigHash.toString(), "ff94560e1ca289de4d661695029f495dde37b16bddd6645fb65c8f61decec22c");

        ECKey.ECDSASignature sig1 = privKey1.sign(sigHash);
        TransactionSignature txSig = new TransactionSignature(sig1, Transaction.SigHash.ALL, false);

        newTx.getInput(0).setScriptSig(ScriptBuilder.createEmpty());
        newTx.getInput(0).setWitness(TransactionWitness.redeemP2WPKH(txSig, privKey1));

        assertEquals(Helper.txToHexString(newTx), "01000000000101012ca4024859020c1119328cc070fae002bde52a191b61904c8e98da3c8da7660000000000ffffffff01d0549a3b00000000220020d42b8341140559b7da105e8669e8f7d5a03773642ad82403ba91b80ffcc415de02483045022100c5db5e86122fd9609dda1f17a6dd3527074ef9b301fd23273b3940bfd8225e4e0220089b268c45437f5ac692be0d546971e84e7dc59c6ce309f5a10dd7cdcc4bb683012103820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e00000000");
        assertEquals(newTx.getTxId().toString(), "d14891128bc4c72dfa45269f302edf690289214874c5ee40b118c1d5465319e6");
    }

    @Test
    public void TestTransactionP2WSHOverP2SHToP2WPKH() throws IOException {
        Address toAddr = Address.fromString(networkParams, "tltc1qk2erszs7fp407kh94e6v3yhfq2njczjvg4hnz6");

        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
//        Script p2wshScript = ScriptBuilder.createP2WSHOutputScript(redeemScript);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write((byte)0);
        output.write((byte)32);
        output.write(Sha256Hash.hash(redeemScript.getProgram()));
        byte[] out = output.toByteArray();
        Script p2wshScript = new ScriptBuilder().data(out).build();

        long prevOutputValue = 1000000000 - fee;
        long outputValue = prevOutputValue - fee;

        Transaction newTx = new Transaction(networkParams);
        newTx.addInput(Sha256Hash.wrap("2464c6122378ee5ed9a42d5192e15713b107924d05d15b58254eb7b2030118c7"), 0, p2wshScript).clearScriptBytes();
        newTx.addOutput(Coin.valueOf(outputValue), toAddr);

        assertEquals(Helper.txToHexString(newTx), "0100000001c7180103b2b74e25585bd1054d9207b11357e192512da4d95eee782312c664240000000000ffffffff01e07b9a3b00000000160014b2b2380a1e486aff5ae5ae74c892e902a72c0a4c00000000");

        Sha256Hash sigHash = newTx.hashForWitnessSignature(0, redeemScript,  Coin.valueOf(prevOutputValue), Transaction.SigHash.ALL, false);
        assertEquals(sigHash.toString(), "e1c684f769c0e186be215ece3b7c1f3f23985ecbafafe0c8d43936fcd79eafdc");

        ECKey.ECDSASignature sig1 = privKey1.sign(sigHash);
        ECKey.ECDSASignature sig2 = privKey2.sign(sigHash);

        TransactionSignature txSig1 = new TransactionSignature(sig1, Transaction.SigHash.ALL, false);
        TransactionSignature txSig2 = new TransactionSignature(sig2, Transaction.SigHash.ALL, false);

        TransactionWitness wit = new TransactionWitness(4);
        wit.setPush(0, new byte[0]);
        wit.setPush(1, txSig1.encodeToBitcoin());
        wit.setPush(2, txSig2.encodeToBitcoin());
        wit.setPush(3, redeemScript.getProgram());

        newTx.getInput(0).setScriptSig(p2wshScript);
        newTx.getInput(0).setWitness(wit);

        assertEquals(Helper.txToHexString(newTx), "01000000000101c7180103b2b74e25585bd1054d9207b11357e192512da4d95eee782312c664240000000023220020d42b8341140559b7da105e8669e8f7d5a03773642ad82403ba91b80ffcc415deffffffff01e07b9a3b00000000160014b2b2380a1e486aff5ae5ae74c892e902a72c0a4c0400473044022067c9f8ed5c8f0770be1b7d44ade72c4d976a2b0e6c4df39ea70923daff26ea5e02205894350de5304d446343fbf95245cd656876a11c94025554bf878b3ecf90db720147304402204ee76a1814b3eb289e492409bd29ebb77088c9c20645c8a63c75bfe44eac41f70220232bcd35a0cc78e88dfa59dc15331023c3d3bb3a8b63e6b753c8ab4599b7bd290147522103820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e210238de8c9eb2842ecaf0cc61ee6ba23fe4e46f1cfd82eac0910e1d8e865bd76df952ae00000000");
        assertEquals(newTx.getTxId().toString(), "66a78d3cda988e4c90611b192ae5bd02e0fa70c08c3219110c02594802a42c01");
    }
}
