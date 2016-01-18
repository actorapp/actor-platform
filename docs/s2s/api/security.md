# Security in s2s protocol

Keeping everything secure is very important for any modern federated messaging. This page describes security part of server-server.

# Access Hashes

Managing what other side can access is very hard. You need to keep permissions table, always keep it at sync. Telegram's team ended up with a simple solution: access hashes.

Access Hashes is server-side generated HMAC for each secured object for each peer. For interacting with secured object, peer need to get Access Hash first, for example, via update or as response from search method.

Peers are responsible to keep Access hash values forewer and use them as key to interact with objects. For example, sending a message also requires approriate access hash.

Access Hashes are not secret and they are valid only for one peer and invalid for other, so there are no sence in stealing them.

In s2s protocol access hashes are represented by strings.
