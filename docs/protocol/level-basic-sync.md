# Basic Sync Level
Basic syncronization support

Raw protocol Rpc and Push provide only basic support and work only with untyped byte arrays, but for real application we need to implement something more detailed. Basic Sync Level is created for this.

### RPC

Basic Sync Level contains various wrappers for Rpc Requests and Response. This objects are encoded as body in ProtoRpcRequest/ProtoRpcResponse.

```
RpcRequest {
    HEADER = 0x01
    // ID of API Method Request
    methodId: int
    // Encoded Request
    body: bytes
}

// Successful RPC
RpcOk {
    HEADER = 0x01
    // ID of API Method Response
    methodResponseId: int
    // Encoded response
    body: bytes
}
 
// RPC Error
RpcError {
    HEADER = 0x02
    // Error Code like HTTP Error code
    errorCode: int
    // Error Tag like "ACCESS_DENIED"
    errorTag: string
    // User visible error
    userMessage: string
    // Can user try again
    canTryAgain: bool
    // Some additional data of error
    errorData: bytes
}
 
// RPC Flood Control. 
// Client need to repeat request after delay
RpcFloodWait {
    HEADER = 0x03
    // Repeat delay on seconds
    delay: int
}
 
// Internal Server Error
// Client may try to resend message
RpcInternalError {
    HEADER = 0x04
    canTryAgain: bool
    tryAgainDelay: int
}
```

## Push

Basic Sync Level contains wrapper for Push. This objects are encoded as body in ProtoPush.

```
Push {
	// Push Entity Id
	updateId: int
  // Encoded Push body
	body: bytes
}
```
