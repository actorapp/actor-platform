//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import CocoaAsyncSocket

class CocoaNetworkRuntime : ARManagedNetworkProvider {
    
    override init() {
        super.init(factory: CocoaTcpConnectionFactory())
    }
}

class CocoaTcpConnectionFactory: NSObject, ARAsyncConnectionFactory {
    
    func createConnectionWithConnectionId(connectionId: jint,
        withEndpoint endpoint: ARConnectionEndpoint!,
        withInterface connectionInterface: ARAsyncConnectionInterface!) -> ARAsyncConnection! {
            
            return CocoaTcpConnection(connectionId: Int(connectionId), endpoint: endpoint, connection: connectionInterface)
    }
}

class CocoaTcpConnection: ARAsyncConnection, GCDAsyncSocketDelegate {
    
    static let queue = dispatch_queue_create("im.actor.network", DISPATCH_QUEUE_SERIAL)
    
    let READ_HEADER = 1
    let READ_BODY = 2
    
    var TAG: String!
    var gcdSocket:GCDAsyncSocket? = nil
    
    var header: NSData?
    
    init(connectionId: Int, endpoint: ARConnectionEndpoint!, connection: ARAsyncConnectionInterface!) {
        super.init(endpoint: endpoint, withInterface: connection)
        TAG = "🎍ConnectionTcp#\(connectionId)"
    }
    
    override func doConnect() {
        let endpoint = getEndpoint()
        
        gcdSocket = GCDAsyncSocket(delegate: self, delegateQueue: CocoaTcpConnection.queue)
        do {
            try self.gcdSocket!.connectToHost(endpoint.getHost()!, onPort: UInt16(endpoint.getPort()), withTimeout: Double(ARManagedConnection_CONNECTION_TIMEOUT) / 1000.0)
        } catch _ {
            
        }
    }
    
    // Affer successful connection
    func socket(sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        if (self.getEndpoint().getType().ordinal() == ARConnectionEndpoint_Type.TCP_TLS().ordinal()) {
            //            NSLog("\(TAG) Starring TLS Session...")
            sock.startTLS(nil)
        } else {
            startConnection()
        }
    }
    
    // After TLS successful
    func socketDidSecure(sock: GCDAsyncSocket!) {
        //        NSLog("\(TAG) TLS Session started...")
        startConnection()
    }
    
    func startConnection() {
        gcdSocket?.readDataToLength(UInt(9), withTimeout: -1, tag: READ_HEADER)
        onConnected()
    }
    
    // On connection closed
    func socketDidDisconnect(sock: GCDAsyncSocket!, withError err: NSError!) {
        //        NSLog("\(TAG) Connection closed...")
        onClosed()
    }
    
    func socket(sock: GCDAsyncSocket!, didReadData data: NSData!, withTag tag: Int) {
        if (tag == READ_HEADER) {
            //            NSLog("\(TAG) Header received")
            self.header = data
            data.readUInt32(0) // IGNORE: package id
            let size = data.readUInt32(5)
            gcdSocket?.readDataToLength(UInt(size + 4), withTimeout: -1, tag: READ_BODY)
        } else if (tag == READ_BODY) {
            //            NSLog("\(TAG) Body received")
            let package = NSMutableData()
            package.appendData(self.header!)
            package.appendData(data)
            package.readUInt32(0) // IGNORE: package id
            self.header = nil
            onReceived(package.toJavaBytes())
            
            gcdSocket?.readDataToLength(UInt(9), withTimeout: -1, tag: READ_HEADER)
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
    
    override func doSend(data: IOSByteArray!) {
        gcdSocket?.writeData(data.toNSData(), withTimeout: -1, tag: 0)
    }
}

private extension NSData {
    
    func readUInt32() -> UInt32 {
        var raw: UInt32 = 0;
        self.getBytes(&raw, length: 4)
        return raw.bigEndian
    }
    
    func readUInt32(offset: Int) -> UInt32 {
        var raw: UInt32 = 0;
        self.getBytes(&raw, range: NSMakeRange(offset, 4))
        return raw.bigEndian
    }
}
