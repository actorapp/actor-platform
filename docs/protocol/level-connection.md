# Connection Level
Lowest level of MTProto v2

Connection Level describes messages for transmitting of connection packets, checking connection state and initialization of connection. On client applications Connection Level usually implemented separately for each platform.

### TCP Transport

Before working with connection handshake is required to perform. Client can send data after handshake without waiting handshake completion. All data in TCP Transport is separated into frames. Before work client need to perform handshake. After successful handshake connection believed to be established.

#### Warring: Different encoding rules
TCP Transport use **slightly different encoding rules**. Main difference that there are no varint in scheme. bytes are encoded with length as int and raw bytes, byte[32] is a raw 32 bytes. This is done for predictable size of Frame.

```
Frame {
	// Index of package starting from zero. 
  // If packageIndex is broken connection need to be dropped.
	packageIndex: int
  // Type of message
  header: byte
  // Package payload
  body: bytes
  // CRC32 of body
  crc32: int
}
```

Frame can contain different data in body field depends on header value. 
If header is equals to zero then payload is protocol object and **need to be sent to [Transport Level](doc:transport-level)**. Otherwise frame is signaling message. Unknown messages can be silently ignored.

```
// header = 1
// Ping message can be sent from both sides
Ping {
	randomBytes: bytes
}

// header = 2
// Pong message need to be sent immediately after receving Ping message
Pong {
	// Same bytes as in Ping package
	randomBytes: bytes
}

// header = 3
// Notification about connection drop
Drop {
	messageId: long
  errorCode: byte
  errorMessage: string
}

// header = 4
// Sent by server when we need to temporary redirect to another server
Redirect {
	host: string
  port: int
  // Redirection timeout
  timeout: int
}

// header = 6
// Proto package is received by destination peer. Used for determening of connection state
Ack {
	receivedPackageIndex: int
}

// header == 0xFF
Handshake {
  // Current MTProto revision
  // For Rev 2 need to eq 1
	protoRevision: byte
  // API Major and Minor version
  apiMajorVersion: byte
	apiMinorVersion: byte
  // Some Random Bytes (suggested size is 32 bytes)
  randomBytes: bytes
}

// header == 0xFE
HandshakeResponse {
  // return same versions as request, 0 - version is not supported
	protoRevision: byte
  apiMajorVersion: byte
	apiMinorVersion: byte
  // SHA256 of randomBytes from request
  sha1: byte[32]
}
```

### Why we need randomBytes??

You may wonder why we need such strange thing? But very often in some networks raw TCP data is changed in unpredictable way and we need some simple check that we have connection to good sever and not to some proxy that inject HTML to traffic.
Same technic is used in WebSockets.
