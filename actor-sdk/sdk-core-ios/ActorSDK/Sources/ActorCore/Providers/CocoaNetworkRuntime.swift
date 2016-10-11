//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaNetworkRuntime : ARManagedNetworkProvider {
    
    override init() {
        super.init(factory: CocoaTcpConnectionFactory())
    }
}

class CocoaTcpConnectionFactory: NSObject, ARAsyncConnectionFactory {
    
    func createConnection(withConnectionId connectionId: jint,
        with endpoint: ARConnectionEndpoint!,
        with connectionInterface: ARAsyncConnectionInterface!) -> ARAsyncConnection! {
            
            return CocoaTcpConnection(connectionId: Int(connectionId), endpoint: endpoint, connection: connectionInterface)
    }
}

class CocoaTcpConnection: ARAsyncConnection, GCDAsyncSocketDelegate {
    
    static let queue = DispatchQueue(label: "im.actor.network", attributes: [])
    
    let READ_HEADER = 1
    let READ_BODY = 2
    
    var TAG: String!
    var gcdSocket:GCDAsyncSocket? = nil
    
    var header: Data?
    
    init(connectionId: Int, endpoint: ARConnectionEndpoint!, connection: ARAsyncConnectionInterface!) {
        super.init(endpoint: endpoint, with: connection)
        TAG = "🎍ConnectionTcp#\(connectionId)"
    }
    
    override func doConnect() {
        let endpoint = getEndpoint()
        
        gcdSocket = GCDAsyncSocket(delegate: self, delegateQueue: CocoaTcpConnection.queue)
        gcdSocket?.isIPv4PreferredOverIPv6 = false
        do {
            try self.gcdSocket!.connect(toHost: (endpoint?.host!)!, onPort: UInt16((endpoint?.port)!), withTimeout: Double(ARManagedConnection_CONNECTION_TIMEOUT) / 1000.0)
        } catch _ {
            
        }
    }
    
    // Affer successful connection
    func socket(_ sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        if (self.getEndpoint().type == ARConnectionEndpoint.type_TCP_TLS()) {
            //            NSLog("\(TAG) Starring TLS Session...")
            sock.startTLS(nil)
        } else {
            startConnection()
        }
    }
    
    // After TLS successful
    func socketDidSecure(_ sock: GCDAsyncSocket!) {
        //        NSLog("\(TAG) TLS Session started...")
        startConnection()
    }
    
    func startConnection() {
        gcdSocket?.readData(toLength: UInt(9), withTimeout: -1, tag: READ_HEADER)
        onConnected()
    }
    
    // On connection closed
    func socketDidDisconnect(_ sock: GCDAsyncSocket!, withError err: Error?) {
        //        NSLog("\(TAG) Connection closed...")
        onClosed()
    }
    
    func socket(_ sock: GCDAsyncSocket, didRead data: Data, withTag tag: Int) {
        if (tag == READ_HEADER) {
            //            NSLog("\(TAG) Header received")
            self.header = data
            data.readUInt32(0) // IGNORE: package id
            let size = data.readUInt32(5)
            gcdSocket?.readData(toLength: UInt(size + 4), withTimeout: -1, tag: READ_BODY)
        } else if (tag == READ_BODY) {
            //            NSLog("\(TAG) Body received")
            var package = Data()
            package.append(self.header!)
            package.append(data)
            package.readUInt32(0) // IGNORE: package id
            self.header = nil
            onReceived(package.toJavaBytes())
            
            gcdSocket?.readData(toLength: UInt(9), withTimeout: -1, tag: READ_HEADER)
        } else {
            fatalError("Unknown tag in read data")
        }
    }
    
    override func doClose() {
        if (gcdSocket != nil) {
            //            NSLog("\(TAG) Closing...")
            gcdSocket?.disconnect()
            gcdSocket = nil
        }
    }
    
    override func doSend(_ data: IOSByteArray!) {
        gcdSocket?.write(data.toNSData(), withTimeout: -1, tag: 0)
    }
}

private extension Data {
    
    func readUInt32() -> UInt32 {
        var raw: UInt32 = 0;
        (self as NSData).getBytes(&raw, length: 4)
        return raw.bigEndian
    }
    
    func readUInt32(_ offset: Int) -> UInt32 {
        var raw: UInt32 = 0;
        (self as NSData).getBytes(&raw, range: NSMakeRange(offset, 4))
        return raw.bigEndian
    }
}
