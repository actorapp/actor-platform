# Actor Ratchet (based on Axolotl Ratchet)

Actor Ratchet is based on ideas of Axolotl Ratchet, but imrove it to make it self-healing proto if something goes wrong in implementation or device environment.

Axolotl also requires to keep RootKey and complex state about encryption. We are trying to avoid this and make proto to be able to automatically heal itself.

Instead of Axolotl, Actor Ratchet support multi-device key syncing for better performance and less offline key verification iterations.

Actor Ratchet and Axolotl Ratchet are both session protocols, but unlike Axolotl - Actor's sessions are short lived. Actor's sessions are almost same as Axolotl, but they are supposed to live much shorter and can be easily discharged after 24 hours.

There are various sources for describing Axolotl Ratchet and there are different ways to implement this, our specs are based on:
* https://whispersystems.org/blog/advanced-ratcheting/
* https://github.com/trevp/axolotl/wiki
* https://github.com/WhisperSystems/Signal-Android/wiki/ProtocolV2

# Public Keys

Each Key Group have Curve25519 public keys:
* **Identity Key** - main static public key that is used for identification
* **Ephermal Keys** - a lot of temporary public keys that is used to start conversaion
* **Last Chance Key** - semi-static key that is used when there are no ephermal keys are available

All this keys are uploaded to Actor Server befor starting communcation.

# Description

We start with case when Alice wants to send message to Bob for the first time.

Alice keys: (AI,AI') - identity key paris, (A1,A1')..(AN,AN') - ephermal key pairs

Bob keys: (BI,BI') - identity key paris, (B1,AB')..(BN,BN') - ephermal key pairs

## Sessions

Each session is identified by *Identity Keys* and *Initial Ephermal Keys*.

Each **Session** state contains:
* Identity Keys - static for a lifetime
* Initial Ephermal Keys - static for a lifetime
* Last 10 received ephermal keys - can be safely discharged in an 24 hours
* Last 10 sent ephermal keys  - can be safely discharged in an 24 hours

Even if **Session** is lost it could be restored without ephermal keys by just download new identity keys from server. New ephermal keys are healed right after first message exchange. Untill first message sent it is unable to receive any messages from this session.

Each *Chain* state contains:
* Reference to related **Session**
* Current outgoing ephermal key

## Starting a new Session

Alice pick own ephermal key pair (A0, A0') and bob's ephermal public key (B0) - both of them are stored at server and can be easily downloaded by both parties.

For deterministic key creation you need to assume that AI < BI. If it is not, swap A* and B* in calculation.

Based on ephermal keys both parties can compute master_secret:
```
master_secret = SHA256(ECDH(AI', B0)  + ECDH(A0', BI) + ECDH(A0', B0))
```

There can be an issue that both sides creates new session with different ephermal keys. If app receives message from new session if should switch to a new one and discard old one (**TO BE DISCUSSED**). Alternative is providing more control from server-side about negotiation of a session.

## Encryption Chain

After establishing master_secret client need to prepare encryption chain. For building a new chain client need to have one additional ephermal key from both sides. It can be used one of the public ephermal keys from server key directory or from previously received message. **This keys are not required to be directly uploaded to key directory.**

Client's *MUST* create new ecnryption chains when they receive new ephermal key from other side or every 24 hours.

Chain is *one-side* entity, that mean that only sender use it **only** for encryption and receiver use it **only** for decryption.

Let's say we pick A1 and B1 keys to our new Chain then we will calculate *root_chain_key*:
```
root_chain_key = HKDF(key = ECDH(A1', B1), salt = master_secret, info = "ActorRatchet".getBytes())
```

## Encryption key for a message

Each message have message_index in current encryption chain starting from zero.
For each message client need to generate message_key based on root_chain_key, then exend it to 64 bytes with HKDF and split it to cipher_key and mac_key:
```
message_key = HMAC_SHA256(root_chain_key, message_index[0..4])
message_key_extendend = HKDF(message_key, 64)
cipher_key = message_key_extendend[0..31]
mac_key = message_key_extendend[32..63]
```

## Encryption

For each message random iv (16 bytes) is generated and then message encrypted and MACed:
```
iv = random[0..16]
cipher_text = AES-128-CBC(iv, cipher_key, plain_text)
cipher_mac = HMAC_SHA256(mac_key, cipher_text)
```

## Outgoing Package

Encrypted Result is serialized to:

```
PreMessage {
  sender_ephermal_id: int64 = A0.id
  receiver_ephermal_id: int64 = A0.id
  sender_ephermal_key: bytes = A1
  receiver_ephermal_key: bytes = B1
  iv: bytes
  cipher_text: bytes
  cipher_mac: bytes
}
```
