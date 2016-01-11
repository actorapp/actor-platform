# Private Messaging

## Keys
Each key group MUST have Curve25519 public key uploaded to server. Each client need to upload ephermal keys to server public in plain-text and private encrypted with symmetric key of a key group. This is needed to share keys across devices.

## Receive message

When client receive private message it CAN upload re-encrypted with symmetric key of every known Key Group to keep messages stored and make them available to download later by other devices.
