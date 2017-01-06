[![Build Status](https://travis-ci.org/joyent/java-manta.svg?branch=travis)](https://travis-ci.org/joyent/java-manta)

# Java Manta Client SDK

[java-manta](http://joyent.github.com/java-manta) is a community-maintained Java
SDK for interacting with Joyent's Manta system.

## Projects Using the Java Manta Client SDK

* [Apache Commons VFS Manta Provider (Virtual File System)](https://github.com/joyent/commons-vfs-manta)
* [Hadoop Filesystem Driver for Manta](https://github.com/joyent/hadoop-manta)
* [Manta Logback Rollover](https://github.com/dekobon/manta-logback-rollover)
* [COSBench Adaptor for Manta - Object Store Benchmarks](https://github.com/joyent/cosbench-manta)

## Installation

### Requirements
* [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or higher.
* [Maven 3.3.x](https://maven.apache.org/)

### Using Maven
Add the latest java-manta dependency to your Maven `pom.xml`.

```xml
<dependency>
    <groupId>com.joyent.manta</groupId>
    <artifactId>java-manta</artifactId>
    <version>LATEST</version>
</dependency>
```

### From Source
If you prefer to build from source, you'll also need
[Maven](https://maven.apache.org/), and then invoke:

``` bash
# mvn package
```

Which will compile the jar to ./targets/java-manta-version.jar. You can then
add it as a dependency to your Java project.

## Configuration

Configuration parameters take precedence from left to right - values on the
left are overridden by values on the right.

| Default                              | TestNG Param             | System Property                    | Environment Variable        |
|--------------------------------------|--------------------------|------------------------------------|-----------------------------|
| https://us-east.manta.joyent.com:443 | manta.url                | manta.url                          | MANTA_URL                   |
|                                      | manta.user               | manta.user                         | MANTA_USER                  |
|                                      | manta.key_id             | manta.key_id                       | MANTA_KEY_ID                |
| $HOME/.ssh/id_rsa                    | manta.key_path           | manta.key_path                     | MANTA_KEY_PATH              |
|                                      |                          | manta.key_content                  | MANTA_KEY_CONTENT           |
|                                      |                          | manta.password                     | MANTA_PASSWORD              |
| 20000                                | manta.timeout            | manta.timeout                      | MANTA_TIMEOUT               |
| 3 (6 for integration tests)          |                          | manta.retries                      | MANTA_HTTP_RETRIES          |
| 24                                   |                          | manta.max_connections              | MANTA_MAX_CONNS             |
| 8192                                 | manta.http_buffer_size   | manta.http_buffer_size             | MANTA_HTTP_BUFFER_SIZE      |
| TLSv1.2                              |                          | https.protocols                    | MANTA_HTTPS_PROTOCOLS       |
| <value too big - see code>           |                          | https.cipherSuites                 | MANTA_HTTPS_CIPHERS         |
| false                                |                          | manta.no_auth                      | MANTA_NO_AUTH               |
| false                                |                          | manta.disable_native_sigs          | MANTA_NO_NATIVE_SIGS        |
| 10000                                | manta.tcp_socket_timeout | manta.tcp_socket_timeout           | MANTA_TCP_SOCKET_TIMEOUT    |
| true                                 |                          | manta.verify_uploads               | MANTA_VERIFY_UPLOADS        |
| 16384                                |                          | manta.upload_buffer_size           | MANTA_UPLOAD_BUFFER_SIZE    |
| false                                |                          | manta.client_encryption            | MANTA_CLIENT_ENCRYPTION     |
| false                                |                          | manta.permit_unencrypted_downloads | MANTA_UNENCRYPTED_DOWNLOADS |
| Mandatory                            |                          | manta.encryption_auth_mode         | MANTA_ENCRYPTION_AUTH_MODE  |
|                                      |                          | manta.encryption_key_path          | MANTA_ENCRYPTION_KEY_PATH   |
|                                      |                          | manta.encryption_key_bytes         |                             |
|                                      |                          | manta.encryption_key_bytes_base64  | MANTA_ENCRYPTION_KEY_BYTES  |

* `manta.url` ( **MANTA_URL** )
The URL of the manta service endpoint to test against
* `manta.user` ( **MANTA_USER** )
The account name used to access the manta service. If accessing via a [subuser](https://docs.joyent.com/public-cloud/rbac/users),
you will specify the account name as "user/subuser".
* `manta.key_id`: ( **MANTA_KEY_ID**)
The fingerprint for the public key used to access the manta service.
* `manta.key_path` ( **MANTA_KEY_PATH**)
The name of the file that will be loaded for the account used to access the manta service.
* `manta.key_content` ( **MANTA_KEY_CONTENT**)
The content of the private key as a string. This is an alternative to `manta.key_path`. Both
`manta.key_path` and can't be specified at the same time `manta.key_content`.
* `manta.password` ( **MANTA_PASSWORD**)
The password associated with the key specified. This is optional and not normally needed.
* `manta.timeout` ( **MANTA_TIMEOUT**)
The number of milliseconds to wait after a request was made to Manta before failing.
* `manta.retries` ( **MANTA_HTTP_RETRIES**)
The number of times to retry failed HTTP requests.
* `manta.max_connections` ( **MANTA_MAX_CONNS**)
The maximum number of open HTTP connections to the Manta API.
* `manta.http_buffer_size` (**MANTA_HTTP_BUFFER_SIZE**)
The size of the buffer to allocate when processing streaming HTTP data. This sets the value
used by Apache HTTP Client `SessionInputBufferImpl` implementation. Ranges from 1024-16384
are acceptable depending on your average object size and streaming needs.
* `https.protocols` (**MANTA_HTTPS_PROTOCOLS**)
A comma delimited list of TLS protocols.
* `https.cipherSuites` (**MANTA_HTTPS_CIPHERS**)
A comma delimited list of TLS cipher suites.
* `manta.no_auth` (**MANTA_NO_AUTH**)
When set to true, this disables HTTP Signature authentication entirely. This is
only really useful when you are running the library as part of a Manta job.
* `http.signature.native.rsa` (**MANTA_NO_NATIVE_SIGS**)
When set to true, this disables the use of native code libraries for cryptography.
* `manta.tcp_socket_timeout` (**MANTA_TCP_SOCKET_TIMEOUT**)
Time in milliseconds to wait for TCP socket's blocking operations - zero means wait forever.
* `manta.verify_uploads` (**MANTA_VERIFY_UPLOADS**)
When set to true, the client calculates a MD5 checksum of the file being uploaded
to Manta and then checks it against the result returned by Manta.
* `manta.upload_buffer_size` (**MANTA_UPLOAD_BUFFER_SIZE**)
The initial amount of bytes to attempt to load into memory when uploading a stream. If the
entirety of the stream fits within the number of bytes of this value, then the
contents of the buffer are directly uploaded to Manta in a retryable form.

Below is an example of using all of the defaults and only setting the `manta.user` and `manta.key_id`.

```java
ConfigContext defaultConfig = new DefaultsConfigContext();
ConfigContext customConfig = new StandardConfigContext()
    .setMantaKeyId("d4:18:cc:34:43:a8:5a:aa:76:1c:35:36:ba:08:1e:aa")
    .setMantaUser("test-user");
ConfigContext config = new ChainedConfigContext(defaultConfig, customConfig);
```

If you want to skip running of the test suite, use the `-DskipTests` property.

## Accounts, Usernames and Subusers
Joyent's SmartDataCenter account implementation is such that you can have a
subuser as a dependency upon a user. This is part of SmartDataCenter's [RBAC
implementation](https://docs.joyent.com/public-cloud/rbac/users). A subuser
is a user with a unique username that is joined with the account holder's
username. Typically, this is in the format of "user/subuser".

Within the Java Manta library, we refer to the account name as the entire
string used to login - "user/subuser". When we use the term user it is in
reference to the "user" portion of the account name and when we use the term
subuser, it is in reference to the subuser portion of the account name.

The notable exception is that in the configuration passed into the library,
we have continued to use the terminology *Manta user* to refer to the
account name because of historic compatibility concerns.

## Usage

You'll need a manta login, an associated rsa key, and its corresponding key
fingerprint. Note that this api currently only supports rsa ssh keys --
enterprising individuals wishing to use dsa keys can contribute to this repo by
consulting the [node-http-signing
spec](https://github.com/joyent/node-http-signature/blob/master/http_signing.md).

For detailed usage instructions, consult the provided javadoc.

### General Examples
 
* [Get request and client setup](java-manta-examples/src/main/java/SimpleClient.java)
* [Multipart upload](java-manta-examples/src/main/java/Multipart.java)

### Job Examples

Jobs can be created directly with the `MantaClient` class or they can be created
using the `MantaJobBuilder` class. `MantaJobBuilder` provides a fluent interface
that allows for an easier API for job creation and it provides a number of
useful functions for common use cases.

* [Jobs using MantaClient](java-manta-examples/src/main/java/JobsWithMantaClient.java)
* [Jobs using MantaJobBuilder](java-manta-examples/src/main/java/JobsWithMantaJobBuilder.java)

For more examples, check the included integration tests.

### Logging

The SDK utilizes [slf4j](http://www.slf4j.org/), and logging can be configured 
using a SLF4J implementation. Apache HTTP Client is bundled as a shaded artifact 
as well as an Apache Commons Logger adaptor to SLF4J so Apache HTTP Client logs
will also be output via SLF4J.

## Subuser Difficulties

If you are using subusers, be sure to specify the Manta account name as `user/subuser`.
Also, a common problem is that you haven't granted the subuser access to the
path within Manta. Typically this is done via the [Manta CLI Tools](https://apidocs.joyent.com/manta/commands-reference.html)
using the [`mchmod` command](https://github.com/joyent/node-manta/blob/master/docs/man/mchmod.md).
This can also be done by adding roles on the `MantaHttpHeaders` object.

For example:

```bash
mchmod +subusername /user/stor/my_directory
```

## Contributions

Contributions welcome! Please read the [CONTRIBUTING.md](CONTRIBUTING.md) document for details
on getting started.

### Testing

When running the integration tests, you will need an active account on the Joyent public
cloud or a private Manta instance. To test:
```
# Assuming you have already set your environment variables and/or system properties
mvn test
```

By default, the test suite invokes the java manta client against the live manta
service.  In order to run the test suite, you will need to specify environment
variables, system properties or TestNG parameters to tell the library how to
authenticate against Manta.

### Releasing

Please refer to the [release documentation](RELEASING.md).

### Bugs

See <https://github.com/joyent/java-manta/issues>.

## License
Java Manta is licensed under the MPLv2. Please see the [LICENSE.txt](LICENSE.txt) 
file for more details. The license was changed from the MIT license to the MPLv2
license starting at version 2.3.0.

### Credits
We are grateful for the functionality provided by the libraries that this project
depends on. Without them, we would be building everything from scratch. A thank you
goes out to:

* [The Apache Commons Project](https://commons.apache.org/)
* [The Apache HTTP Components Project](http://hc.apache.org/)
* [The FastXML Project](https://github.com/FasterXML)
* [The Legion of the Bouncy Castle Project](https://www.bouncycastle.org/)
* [The SLF4J Project](http://www.slf4j.org/)
* [The JNAGMP Project](https://github.com/square/jna-gmp)
* [The TestNG Project](http://testng.org/doc/index.html)
* [The Mockito Project](http://site.mockito.org/)