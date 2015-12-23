MTProto V2 is binary protocol that is optimized for mobile networks. MTProto V2 doesn't contain any kind of encryption and securing data is implemented by encapsulating of MTProto V2 in TLS.

Main goal of MTProto V2 is providing fast and reliable communication with server. MTProto V2 is not compatable with protobuf and doesn't contain any API definitions. For learning more about API read another page.

MTProto V2 now have two revisions: rev 1 and rev 2. They are incompatible and rev 1 currently not in production. This document is about rev 2. 

MTProto V2 consist of some layers:
  * [Encoding](encoding.md)
  * [Connection Level](level-connection.md)
  * [Transport Level](level-transport.md)
  * [Basic Sync Level](level-basic-sync.md)
