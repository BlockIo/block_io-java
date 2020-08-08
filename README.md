# block_io-java
# BlockIo

This Java library is the official reference client for the Block.io payments API and uses sdk 11. To use this, you will need the Dogecoin, Bitcoin, or Litecoin API key(s) from <a href="https://block.io" target="_blank">Block.io</a>. Go ahead, sign up :)

## Installation

1. Clone the repo
2. mvn package

## Usage

It's super easy to get started. In your code, do this:

    BlockIo blockLib = new BlockIo(API_KEY, PIN, VERSION);

    // to pass options:

    BlockIo blockLib = new BlockIo(API_KEY, PIN, VERSION, new Options("API URL", "Bool to allow no pin"))

    // print the account balance request's response
    System.out.println(blockLib.GetBalance(null));

    // print all addresses on this account
    System.out.println(blockLib.GetMyAddresses(null));

    // print the response of a withdrawal request
    // 'SECRET_PIN' is only required if you did not specify it at 
    // class initialization time.
    Map<String, Object> res = blockLib.Withdraw(Map.of(
            "pin", "secret_pin",
            "from_labels", "label1, label2",
            "to_label", "label3",
            "amount", "50.0"
        ));   

##### A note on passing json args to requests:

Args are passed as a Map like this: 

    Map.of("param1", "string", "param2", "intVal", "param3", "this, is, a, list")

## Testing

We use JUnit 5 for unit tests that ensure all internal library functions work correctly.

**DO NOT USE PRODUCTION CREDENTIALS FOR UNIT TESTING!** 

Test syntax:

```bash
mvn test
