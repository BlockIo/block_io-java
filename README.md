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
            <version>2.0.1-beta</version>
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
		    implementation 'com.github.BlockIo:block_io-java:2.0.1-beta'
	  }
   ```
### Android
After adding the gradle dependency, you need to:
1. Add this in your AndroidManifest:
   
   `<uses-permission android:name="android.permission.INTERNET"/>`
   

2. Add this in your build.gradle:

   ```
   android{
      defaultConfig {
         ...
         multiDexEnabled true
      }
   
      packagingOptions {
        exclude 'lib/x86_64/darwin/libscrypt.dylib'
        exclude 'lib/x86_64/freebsd/libscrypt.so'
        exclude 'lib/x86_64/linux/libscrypt.so'
      }
   
      dependencies {
        implementation 'com.github.BlockIo:block_io-java:2.0.1-beta'
        implementation 'com.android.support:multidex:1.0.3'
        implementation 'com.squareup.okhttp3:okhttp:4.1.0'
	  }
   
   } 
   ```
   
See the sample Activity in examples/Android

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
