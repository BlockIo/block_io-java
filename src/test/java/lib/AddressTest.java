package lib;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libdohj.params.LitecoinTestNet3Params;

import java.util.*;

import static org.bitcoinj.script.ScriptOpCodes.OP_EQUAL;
import static org.bitcoinj.script.ScriptOpCodes.OP_HASH160;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressTest {
    private NetworkParameters networkParams;
    ECKey privKey1;
    ECKey privKey2;

    @BeforeEach
    void setUp() {

        networkParams = LitecoinTestNet3Params.get();
        privKey1 = ECKey.fromPrivate(Hex.decode("ef4fc6cfd682494093bbadf041ba4341afbe22b224432e21a4bc4470c5b939d4"));
        privKey2 = ECKey.fromPrivate(Hex.decode("123f37eb9a7f24a120969a1b2d6ac4859fb8080cfc2e8d703abae0f44305fc12"));
    }

    @Test
    void TestPubKeys() {
        assertEquals(privKey1.getPublicKeyAsHex(), "03820317ad251bca573c8fda2b8f26ffc9aae9d5ecb15b50ee08d8f9e009def38e");
        assertEquals(privKey2.getPublicKeyAsHex(), "0238de8c9eb2842ecaf0cc61ee6ba23fe4e46f1cfd82eac0910e1d8e865bd76df9");
    }

    @Test
    public void TestP2PKHAddress()
    {
        Address p2pkhAddr = Address.fromKey(networkParams, privKey1, Script.ScriptType.P2PKH);

        assertEquals(p2pkhAddr.toString(), "mwop54ocwGjeErSTLCKgKxrdYp1k9o6Cgk");
    }

    @Test
    public void TestP2WPKHAddress()
    {
        Address p2wpkhAddr = Address.fromKey(networkParams, privKey1, Script.ScriptType.P2WPKH);
        assertEquals(p2wpkhAddr.toString(), "tltc1qk2erszs7fp407kh94e6v3yhfq2njczjvg4hnz6");
    }
    @Test
    public void TestWitnessV0Address()
    {
        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
        Script p2wshScript = ScriptBuilder.createP2WSHOutputScript(redeemScript);

        Address p2wshAddr = p2wshScript.getToAddress(networkParams);

        assertEquals(p2wshAddr.toString(), "tltc1q6s4cxsg5q4vm0ksst6rxn68h6ksrwumy9tvzgqa6jxuqllxyzh0qxt7q8g");
    }

    @Test
    public void TestP2SHAddress()
    {
        //failing
        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
        Script p2shMultiSig = ScriptBuilder.createP2SHOutputScript(redeemScript);

        Address p2shAddr = p2shMultiSig.getToAddress(networkParams);

        assertEquals(p2shAddr.toString(), "QPZMy7ivpYdkJRLhtTx7tj5Fa4doQ2auWk");
    }

    @Test
    public void TestP2WPKHOverP2SHAddress()
    {
        //failing
        Address p2wpkhAddr = Address.fromKey(networkParams, privKey1, Script.ScriptType.P2WPKH);
        Script p2shWrappedScript = ScriptBuilder.createP2SHOutputScript(p2wpkhAddr.getHash());
        Address test = p2shWrappedScript.getToAddress(networkParams);

        assertEquals(test.toString(), "Qgn9vENxxnNCPun8CN6KR1PPB7WCo9oxqc");
    }

    @Test
    public void TestP2WSHOverP2SHAddress()
    {
        //failing
        Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(privKey1, privKey2));
        Script p2wshScript = ScriptBuilder.createP2WSHOutputScript(redeemScript);

        byte[] hash = Utils.sha256hash160(p2wshScript.getProgram());
        Script p2shWrapped = ScriptBuilder.createP2SHOutputScript(hash);

        Address addr = p2shWrapped.getToAddress(networkParams);
        assertEquals(addr.toString(), "QeyxkrKbgKvxbBY1HLiBYjMnZx1HDRMYmd");
    }
}
