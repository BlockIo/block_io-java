package lib;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libdohj.params.LitecoinTestNet3Params;
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

        TransactionSignature sig1TransactionSignature = new TransactionSignature(sig1, Transaction.SigHash.ALL, false);
        TransactionSignature sig2TransactionSignature = new TransactionSignature(sig2, Transaction.SigHash.ALL, false);

        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(ImmutableList.of(sig1TransactionSignature, sig2TransactionSignature), redeemScript);

        newTx.getInput(0).setScriptSig(inputScript);

        assertEquals(Helper.txToHexString(newTx), "010000000162be60c1cdbd6b44167d8460756f04feb65ba7bd969ef825a174f576970bd84a01000000da00473044022009143b07279ef6d5317865672e9fc28ada31314abf242ae786917b92cf027ac002207544d055f2b8bb249dc0294d565c6d538f4e04f9b142331fa103d82e0498a181014830450221009ce297b1eba341be03e0ae656ac0233464c8249d36f3659676b01c45c74808680220252d0c54d56d78b4193cc1c63b7b8dc2a6b9889bed5dc3555571c8aaa1a710e70147522103820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e210238de8c9eb2842ecaf0cc61ee6ba23fe4e46f1cfd82eac0910e1d8e865bd76df952aeffffffff01f0a29a3b0000000017a914c99a494597ade09b5194f9ec8e02d96607ae64798700000000");
        assertEquals(newTx.getTxId().toString(), "754162225c3f2b8ce476d1df7a0b35f04ba6fe24fd6c0fd89e31c5e54d4eaec1");
    }
}
