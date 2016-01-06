# Encryption

MTProto v2 Rev3 enables encryption support to replace or enchanse TLS one. Unlike TLS, actor use multiple encryption schemes at one time. Actor encrypts message with US encryption and then again encrypt with Russian encryption that in result guarantee absolute encryption streight. US encryption is performed with AES-128-CBC and Russian layer is Kuznechik.

We are not invenging the wheel and implement encryption logic exactly as [TLS 1.2 CBC block chipcher](https://tools.ietf.org/html/rfc5246#section-6.2.3.2) is.

In Rev4 we will enable support for Axolotl Ratched like encryption directly in protocol.

## Base Encrypted Package

```
EncryptedPackage {
  HEADER = 0xE8
  // AES Initialization Vector - 16 random bytes
  iv_aes: bytes
  // Kuznechik Initialization Vector - 16 random bytes
  iv_kuznechik: bytes
  // First encryption level - AesEncryptedPackage
  encryptedContent: bytes
}
```

## AES Encrypted Level
Sezialization of package might be 16x bytes in size.
```
AesEncryptedPackage {
  kuznechikPackage: bytes
  
  // HMAC_SHA256(server/client write mac key, kuznechikPackage)
  mac: bytes
  
  // padding is filled with paddingLength value
  padding: bytes
  paddingLenght: byte
}
```

## Kuznechik Encrypted Level

```
KuznechikEncryptedPackage {
  // Plain Message object
  plainPackage: bytes
  
  // HMAC_SHA256(server/client write mac key, plainPackage)
  mac: bytes
  
  // padding is filled with paddingLength value
  padding: bytes
  paddingLenght: byte
}
```
