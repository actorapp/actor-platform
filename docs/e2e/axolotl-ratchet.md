# Axolotl Ratchet

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

## Building initial initial_root_key

Alice compute initial_root_key:
```
initial_root_key = SHA256(ECDH(AI', BI) + ECDH(AI', BI))
```
Bob compute initial_root_key:
```
initial_root_key = SHA256(ECDH(A0, BI') + ECDH(AI', BI))
```

## Computing parameters for first message

Alice generates random ephermal key pair (A1, A1') and picks random Bob's ephermal key - B1

```
master_secret = SHA256(initial_root_key + ECDH(A1', B1))
```

