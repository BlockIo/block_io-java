# block_io-java
# BlockIo

This Java library is the official reference client for the Block.io payments API and uses Java 11. To use this, you will need the Dogecoin, Bitcoin, or Litecoin API key(s) from <a href="https://block.io" target="_blank">Block.io</a>. Go ahead, sign up :)

## Installation

### Maven
1. Add the following section to your pom.xml:
   ```
       <repositories>
            <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
            </repository>
       </repositories>
    ```
2. Add the following dependency:

    ```
        <dependency>
            <groupId>com.github.BlockIo</groupId>
            <artifactId>block_io-java</artifactId>
            <version>RELEASE_TAG_OR_COMMIT_HASH</version>
        </dependency>
    ```
### Gradle
1. Add the following in your root build.gradle at the end of repositories:
   ```
       allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
       }
    ```
2. Add the following dependency:

    ```
        dependencies {
		          implementation 'com.github.BlockIo:block_io-java:RELEASE_TAG_OR_COMMIT_HASH'
	       }
    ```

## Usage

It's super easy to get started. In your code, do this:

    import lib.blockIo.BlockIo;

    BlockIo blockLib = new BlockIo(API_KEY, PIN, VERSION);

    // to pass options:

    BlockIo blockLib = new BlockIo(API_KEY, PIN, VERSION, new Options("API URL", "Bool to allow no pin"))

    // print the account balance request's response
    System.out.println(blockLib.GetBalance(null));

    // print all addresses on this account
    System.out.println(blockLib.GetMyAddresses(null));

For more detailed usage examples, take a look at the examples' folder in this repo

### A note on passing json args to requests:

Args are passed as a Map wrapped in a JSONObject like this: 

    new JSONObject(Map.of("param1", "string", "param2", "intVal", "param3", "this, is, a, list"))

## Testing

We use JUnit 5 for unit tests that ensure all internal library functions work correctly.

**DO NOT USE PRODUCTION CREDENTIALS FOR UNIT TESTING!** 

Test syntax:

```bash
mvn test
