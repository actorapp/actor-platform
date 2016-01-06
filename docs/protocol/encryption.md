# Encryption

Rev3 of protocol enables encryption support to replace TLS one. We are not invenging the wheel and implement encryption exactly as [TLS 1.2 AEAD block chipcher](https://tools.ietf.org/html/rfc5246#section-6.2.3.2) is. With AES-128-GCM and SHA512.
