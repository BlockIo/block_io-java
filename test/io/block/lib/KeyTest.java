package io.block.lib;

import org.bitcoinj.core.ECKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyTest {

    String wif = "L1cq4uDmSKMiViT4DuR8jqJv8AiiSZ9VeJr82yau5nfVQYaAgDdr";
    String passphrase = "deadbeef";
    String dataToSign = "e76f0f78b7e7474f04cc14ad1343e4cc28f450399a79457d1240511a054afd63";
    String controlPrivKeyFromPassphrase = "5f78c33274e43fa9de5659265c1d917e25c03722dcb0b8d27db8d5feaa813953";
    String controlPubKeyFromPassphrase = "02953b9dfcec241eec348c12b1db813d3cd5ec9d93923c04d2fa3832208b8c0f84";
    String controlPrivKeyFromWif = "833e2256c42b4a41ee0a6ee284c39cf8e1978bc8e878eb7ae87803e22d48caa9";
    String controlPubKeyFromWif = "024988bae7e0ade83cb1b6eb0fd81e6161f6657ad5dd91d216fbeab22aea3b61a0";
    String controlSignedDataWifKey = "3045022100aec97f7ad7a9831d583ca157284a68706a6ac4e76d6c9ee33adce6227a40e675022008894fb35020792c01443d399d33ffceb72ac1d410b6dcb9e31dcc71e6c49e92";
    String controlSignedDataPassphraseKey = "30450221009a68321e071c94e25484e26435639f00d23ef3fbe9c529c3347dc061f562530c0220134d3159098950b81b678f9e3b15e100f5478bb45345d3243df41ae616e70032";

    ECKey privKeyFromWif = Key.fromWif(wif);
    ECKey privKeyFromPassphrase = Key.extractKeyFromPassphrase(passphrase);

    String pubKeyFromWif = privKeyFromWif.getPublicKeyAsHex();
    String pubKeyFromPassphrase = privKeyFromPassphrase.getPublicKeyAsHex();

    KeyTest() throws Exception {
    }

    @Test
    void fromWif() {
        assertEquals(controlPrivKeyFromWif, privKeyFromWif.getPrivateKeyAsHex());
    }

    @Test
    void extractKeyFromPassphrase() {
        assertEquals(controlPrivKeyFromPassphrase, privKeyFromPassphrase.getPrivateKeyAsHex());
    }
    @Test
    void pubKeyFromWif() {
        assertEquals(controlPubKeyFromWif, pubKeyFromWif);
    }
    @Test
    void pubKeyFromPassphrase() {
        assertEquals(controlPubKeyFromPassphrase, pubKeyFromPassphrase);
    }
    @Test
    void signDataWifKey() {
        assertEquals(Helper.signInputs(privKeyFromWif, dataToSign, pubKeyFromWif), controlSignedDataWifKey);
    }
    @Test
    void signDataPassphraseKey() {
        assertEquals(Helper.signInputs(privKeyFromPassphrase, dataToSign, pubKeyFromPassphrase), controlSignedDataPassphraseKey);
    }
}