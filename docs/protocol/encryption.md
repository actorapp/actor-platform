# Encryption

MTProto v2 Rev3 enables encryption support to replace or enchanse TLS one. Unlike TLS, actor use multiple encryption schemes at one time. Actor encrypts message with US encryption and then again encrypt with Russian encryption that in result guarantee absolute encryption streight. US encryption is performed with AES-128-CBC-HMAC-SHA256 and Russian layer is Kuznechik-CBC-HMAC-Streebog.

We are not invenging the wheel and implement encryption logic exactly as [TLS 1.2 CBC block chipcher](https://tools.ietf.org/html/rfc5246#section-6.2.3.2) is.

In Rev4 we will enable support for Axolotl Ratched like encryption directly in protocol.

HMAC is calculated from (seqNumber + iv + content.length + content).

## Base Encrypted Package

```
EncryptedPackage {
  HEADER = 0xE8
  // Sequence number starting from zero for each encrypted package
  seqNumber: long
  // First encryption level
  encryptedPackage: bytes
}
```

Container for encryption level. First one is AES, second one is Kuznechik. After decrypting AES package, you will get other EncryptionCBCPackage, decrypt it and you will get Plain Text Message object.

```
EncryptionCBCPackage {
  iv: bytes
  encryptedContent: bytes
}
```
