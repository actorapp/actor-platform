# Basic Principles of Federation protocol

Server-Server protocol is push-based protocol. This means that author (server) of a message is responsible to delivering it to receipient.

Currently s2s ptorofol is based on HTTP/1.1. In future releases underlying transport can be replaced according with performance requiremenets of network.

We found that our ideas are often the same as project [matrix.org](http://matrix.org/docs/spec/r0.0.1/server_server.html) have. We believe that we can work together and enrich their's network with Actor. Draft version of our federation API is **NOT** compatable with matrix, but release one we expect to be one.

Protocol is JSON-based, but can be easily transformed to any form of key-value format, for example, protobuf.

## Basic requirements

1) Server have port that is accessible in the internet
2) Server have TLS configured. We are highly recommend to use TLS
3) Owns domain name
4) Owner of a server have email server on this domain name (not explictly checked and can be reduced in future releases)

## Data Types

* **Server Address** - Array of Host Names and ports for connecting to server. There are different ports for different transports.
* **Strong Updates** - updates that affects persistent state of a conversation or entyty. For example: new message, group member invite, user's avatar change, etc...
* **Weak Updates** - ephermal updates that is usually notification about temporary state and doesn't need to be stored in any offline storage. For example: typing notifications, online/offline notifications, etc...
* **Requests** - Abstract request-response from one server to another.

## Layers

As MTProto v2 server-server protocol is divided to layers.

* Transport Level - Low Level transport protocol. Defines how requests made and updates delivered. HTTP/1.1 is used in this spec.
* Protection Level - level that encrypt everything and checks data integrity. Not included in current draft.
* Basic API level - API requests and update support. Defines how it is posslbe to make requests from server to server and how to receive updates even throught 3rd party servers.

## Basic API level

Basic API level have PUSH and RPC support. RPC and PUSH are both request-response entities with minor differences in how they work.

#### Push
As we mentioned before, protocol is push-based and all responsibility of push delivery is on sender's side.

```json
{
  "weak": [
    {
      "update": "typing",
      "user_id": "uid",
      "peer": {
        "type": "group",
        "id": 11223344
      }
    }
  ],
  "strong": [
    {
      "update": "message",
      "peer": {
        "type": "group",
        "id": 11223344
      },
      "content": {
        "type": "text",
        "text": "Hello Wordl!"
      }
    }
  ],
  "seq": 234234
}
```

Response OK
```json
{
  "receive": "ok",
  "seq": 234234
}
```

Response Hole Detected
Response when in seq updates hole is detected. Sender need to resend all **STRONG** updates starting from returned seq.
```json
{
  "receive": "hole_detected",
  "seq": 234234
}
```

Response Wait
Response when request need to be repeated in `wait` seconds
```json
{
  "receive": "wait",
  "wait": 123
}
```

## Transport Level: HTTPS

Each server **MUST** support HTTPS transport. HTTPS transport is not RESTful API and just transport wrapper to our API.

#### Making Requests

To make RPC request, server need to make a POST request to ```/http/v1/rpc``` with RPC json body.

#### Delivering Updates

For delivering Update, server need to make a POST request to ```/http/v1/updates``` with Update JSON body in response server will return approriate JSON-ACK on this Update.
