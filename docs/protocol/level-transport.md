# Transport Level
Level for organization reliable package transmition

Transport Level is main and most complex part of protocol.

Main goals of Transport Level
  * Fast connection recreation
  * Minimize traffic amount
  * Connection-agnostic package transmition
  * Recovering after connection die
  * Recovering after server die
  * Work in networks that work on buggy hardware that breaks connection in random situations

In reference protocol implementation only single active connection is used. This assumption makes implementation much more simpler and avoid some weird behaviour when new connection die and old one is still working.

## Packages

Each transport level package is a structure

```javascript
// Message Container
Message {
    // message identifier
    messageId: long
    // message body
    message: byte
}

Package {
    // unique identifier that is constant thru all application lifetime
    authId: long
    // random identifier of current session
    sessionId: long
    // message
    message: Message
}
```

## Requesting Auth Id

Before sending and receiving packages it is required to perform ```authId``` calculation. In current revision ```authId``` is requested by simple special API request. In future version ```authId``` will be calculated by performing Diffie-Hellman key exchange. We assume that getting ```authId``` is implemented separately and is not part of main MTProto implementation.

During getting ```authId``` authId/sessionId is required to be zero for all other operations authId and sessionId can't be zero.

```javascript
RequestAuthId {
    HEADER = 0xF0
}

ResponseAuthId {
    HEADER = 0xF1
    authId: long
}
```

## RPC and Push Packages

Clients can perform various RPC Requests and get RPC Responses. Server can send Push packages for notification client about updates.

### Hint: Messages Priority
When implementing protocol it is useful to have ability to mark some RPC Requests as high priority and send them always as separate packages and before any other packages. This technic can reduce message send latency.

```javascript
ProtoRpcRequest {
    HEADER = 0x03
    // Request body
    payload: bytes
}

ProtoRpcResponse {
    HEADER = 0x04
    // messageId from Message that contains ProtoRpcRequest
    messageId: long
    // Response body
    payload: bytes
}

ProtoPush {
    HEADER = 0x05
    // Push body
    payload: bytes
}
```

## Service Packages

Service packages are packages that used for keeping everything in sync.

### Acknowledge

Receiving of most messages need to be confirmed by receiver side. ProtoRpcResponse is automatically marking ProtoRpcRequest as confirmed.
For explicitly marking package as received used MessageAck (that doesn't need to be acknowledged).
Behaviour of server side MessageAck and cleint side MessageAck is a bit different. Server try to send Acks as fast as it can and try to group all acks.
Client send confirm:
  * When new connection is created: first message is always MessageAck if available.
  * When unconfirmed buffer contains more than 10 messages
  * When client need to send some normal priority packages: client group it with MessageAck
  * When uncorfirmed buffer is older than 1 minute

```javascript
MessageAck {
    HEADER = 0x06
    // Message Identificators for confirmation
    messageIds: longs
}
```

### Resend

When sent message is not confirmed then other side can try to resend message or notify about other side about sent message. Usually this is performed when new connection is created or confirmation doesn't arrived in 1 minute.

If package is greater than 1kb peer can send notification about sent package for avoiding double-sending. When other peer gets notification about sent message it need to response with MessageAck or RequestResend depends on if peer received this message.

```javascript
// Notification about unsent message (usually ProtoRpcRequest or ProtoPush)
UnsentMessage {
    HEADER = 0x07
    // Sent Message Id
    messageId: long
    // Size of message in bytes
    len: int
}

// Notification about unsent ProtoRpcResponse
UnsentResponse {
    HEADER = 0x08
    // Sent Message Id
    messageId: long
    // Request Message Id
    requestMessageId: long
    // Size of message in bytes
    len: int
}

// Requesting resending of message
RequestResend {
    HEADER = 0x09
    // Message Id for resend
    messageId: long
}
```

### NewSessionCreated
**Most important part of protocol.** NewSessionCreated is client notification about session recreation because of performing Session Garbage Collection or because of Server Failure.
When client receive NewSessionCreated it need to resend all ProtoRpcRequests, resubscribe to all events. All Session state is dropped when NewSessionCreated arrive. When client generate new session id then server send NewSessionCreated because there are never was a session with this id. NewSessionCreated are expected to appear randomly in any situation and client side code need to correctly implement it's support.

```javascript
NewSession {
    HEADER = 0x0C
    // Created Session Id
    sessionId: long
    // Message Id of Message that created session
    messageId: long
}
```

### SessionHello
Notification about session and auth id in session. Useful when connection is recreated, but there are no suitable data to send.

```javascript
SessionHello {
    HEADER = 0x0F
}
```

### SessionLost
Notification about session of connection is lost. Client need to perform any request or send SessionHello package

```javascript
SessionLost {
    HEADER = 0x10
}
```

### AuthIdInvalid

Client's AuthId dies and connection is closed after sending this message

```javascript
AuthIdInvalid {
    HEADER = 0x11
}
```

### Drop
Drop is a package for notification about some problem with processing package. After sending Drop server close connection.
```javascript
Drop {
    HEADER = 0x0D
    // Message Id of message that causes Drop. May be zero if not available
    messageId: long
    // Error Message
    errorMessage: String
}
```

### Container

For grouping messages into containers protocol uses Container package

```javascript
Container {
    HEADER = 0x0A
    // Messages count
    count: varint
    // Messages in container
    data: Message[]
}
```
