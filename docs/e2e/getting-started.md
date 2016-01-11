# End-To-End encryption

Next version of Actor will enable end-to-end encryption by default for everyone. Actor uses Axolotl Ratchet with Curve25519 for key exchange and mix of AES and Kuznechik encryption as block cipher.

## Definitions

* *Key* - Encryption key
* *Key Pair* - Pair of Public and Private encryption keys for asymmetric encryption
* *Symmetric Key* - Secret Key for symmetric encrytpion
* *Key Group* - Group of Key Pairs and Symmetric Keys. Key Group can have optional name.
* *Public Key Group* - Public Keys of specific Key Group
* *Device* - End-user device with Actor installed
* *Account* - User's Actor account

## Device, Accounts, Keys and Key Groups

Each *Account* can have unlimited amount of devices.
Each *Device* by default create new Key Group and uploads Public Key Group to server.
*Devices* can exchange key groups between each other. Eventually there must be only one shared *Key Group* for all *Devices*.
*Devices* can delete key groups that are no longer in use.

## Sending a private message from one account to another

For sending message to account, you need to encrypt message with every Key Group with public or symmetric keys (if available) and with every Key Group of recepient.

## Group Messages

Groups in Actor have one symmetric key for encryption on every members change encrytpion key of a group and client apps need to explictly change and broadcast new key to every member of a group.
