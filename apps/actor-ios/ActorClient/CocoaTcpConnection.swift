//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaTcpConnectionFactory: NSObject, AMAsyncConnectionFactory {
    func createConnectionWithInt(connectionId: jint, withAMConnectionEndpoint endpoint: AMConnectionEndpoint!, withAMAsyncConnectionInterface connectionInterface: AMAsyncConnectionInterface!) -> AMAsyncConnection! {
        return CocoaTcpConnection(connectionId: Int(connectionId), AMConnectionEndpoint: endpoint, withAMAsyncConnectionInterface: connectionInterface)
    }
}

class CocoaTcpConnection: AMAsyncConnection, GCDAsyncSocketDelegate {

    let READ_HEADER = 1
    let READ_BODY = 2
    
    var TAG: String!
    var gcdSocket:GCDAsyncSocket? = nil
    
    var header: NSData?
    
    init(connectionId: Int, AMConnectionEndpoint endpoint: AMConnectionEndpoint!, withAMAsyncConnectionInterface connection: AMAsyncConnectionInterface!) {
        super.init(AMConnectionEndpoint: endpoint, withAMAsyncConnectionInterface: connection)
        TAG = "🎍ConnectionTcp#\(connectionId)"
    }
    
    override func doConnect() {
//        NSLog("\(TAG) connecting...")
        gcdSocket = GCDAsyncSocket(delegate: self, delegateQueue: dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0))
        var endpoint = getEndpoint()
        gcdSocket!.connectToHost(endpoint.getHost()!, onPort: UInt16(endpoint.getPort()), withTimeout: Double(AMManagedConnection_CONNECTION_TIMEOUT) / 1000.0, error: nil)
    }
    
    // Affer successful connection
    func socket(sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        if (UInt(self.getEndpoint().getType().ordinal()) == AMConnectionEndpoint_Type.TCP_TLS.rawValue) {
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
            var size = data.readUInt32(5)
            gcdSocket?.readDataToLength(UInt(size + 4), withTimeout: -1, tag: READ_BODY)
        } else if (tag == READ_BODY) {
//            NSLog("\(TAG) Body received")
            var package = NSMutableData()
            package.appendData(self.header!)
            package.appendData(data)
            self.header = nil
            gcdSocket?.readDataToLength(UInt(9), withTimeout: -1, tag: READ_HEADER)
            
            onReceivedWithByteArray(package.toJavaBytes())
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
    
    override func doSendWithByteArray(data: IOSByteArray!) {
//        NSLog("\(TAG) Sending package...")
        gcdSocket?.writeData(data.toNSData(), withTimeout: -1, tag: 0)
    }
}