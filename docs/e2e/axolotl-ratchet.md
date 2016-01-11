# Axolotl Ratchet

There are various sources for describing Axolotl Ratchet and there are different ways to implement this:
* https://whispersystems.org/blog/advanced-ratcheting/
* https://github.com/trevp/axolotl/wiki
* https://github.com/WhisperSystems/Signal-Android/wiki/ProtocolV2

We are trying to combine everything one one actual documentation and implement it in Actor.

# Public Keys

Each Key Group have Curve25519 public keys:
* **Identity Key** - main static public key that is used for identification
* **Ephermal Keys** - a lot of temporary public keys that is used to start conversaion
* **Last Chance Key** - semi-static key that is used when there are no ephermal keys are available

All this keys are uploaded to Actor Server befor starting communcation.

# Description

Axolotl Ratchet is a session protocol. When session is established you there are no need to abort it. Axolotl is a stateful protocol that means that you need to keep state for each session. Sessions are established between each device.

When session is simulatenously created from both sides, then application need to use any of them, but keep state for each one. We recommend to pick something with lower id of each session.

We start with case when Alice wants to send message to Bob for the first time.

Alice keys: (AI,AI') - identity key paris, (A1,A1')..(AN,AN') - ephermal key pairs

Bob keys: (BI,BI') - identity key paris, (B1,AB')..(BN,BN') - ephermal key pairs

## Computing parameters for first message

Alice generates random ephermal key pair (A0, A0') and picks random Bob's ephermal key - B0. Then generates master_secret, then stretch key to 64 bytes and get RootKey and ChainKey:
```
master_secret = SHA256(ECDH(AI', B0)  + ECDH(A0', BI) + ECDH(A0', B0))
master_secret_extendend = HKDF(master_secret, 64)
root_key = master_secret_extendend[0..31]
chain_key = master_secret_extendend[32..63]
```
