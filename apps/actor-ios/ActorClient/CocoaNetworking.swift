//
//  CocoaNetworking.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 20.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class SwiftCocoaNetworkProvider : NSObject, AMNetworkProvider {
    // TODO: Check thread-safe correctness
    let syncObject = NSObject()
    var pendingConnection: Array<AnyObject> = []
    
    func createConnection(connectionId: jint, withEndpoint endpoint: AMConnectionEndpoint!, withCallback callback: AMConnectionCallback!, withCreateCallback createCallback: AMCreateConnectionCallback!) {
        
//        var connection = SwiftCocoaConnection(connectionId: connectionId, withEndpoint: endpoint!, withCallback: callback!, connectionCreated: { (connection) -> () in
//            createCallback.onConnectionCreated(connection)
//            objc_sync_enter(self.syncObject)
//            self.pendingConnection.removeAtIndex(find(self.pendingConnection, connection)!)
//            objc_sync_exit(self.syncObject)
//        }) { (connection) -> () in
//            createCallback.onConnectionCreateError()
//            objc_sync_enter(self.syncObject)
//            self.pendingConnection.removeAtIndex(find(self.pendingConnection, connection)!)
//            objc_sync_exit(self.syncObject)
//        }
        
        var connection = CocoaTcpConnection(connectionId: connectionId, connectionEndpoint: endpoint, connectionCallback: callback, createCallback: createCallback)
        
        objc_sync_enter(syncObject)
        pendingConnection.append(connection)
        objc_sync_exit(syncObject)
        
        // connection.start()
    }
}

class SwiftCocoaConnection: NSObject, AMConnection, GCDAsyncSocketDelegate {
    
    let connectionTimeout = 5.0
    
    var isSocketOpen = false
    var isSocketClosed = false
    var gcdSocket:GCDAsyncSocket? = nil;
    var outPackageIndex:UInt32 = 0
    var inPackageIndex:UInt32 = 0
    
    let connectionId:Int;
    let endpoint: AMConnectionEndpoint;
    let connectionCreated: (connection: SwiftCocoaConnection)->()
    let connectionFailure: (connection: SwiftCocoaConnection)->()
    let callback: AMConnectionCallback;
    
    init(connectionId: jint, withEndpoint endpoint: AMConnectionEndpoint, withCallback callback: AMConnectionCallback, connectionCreated: (connection: SwiftCocoaConnection)->(), connectionFailure: (connection: SwiftCocoaConnection)->()) {
        self.connectionId = Int(connectionId)
        self.endpoint = endpoint;
        self.connectionCreated = connectionCreated
        self.connectionFailure = connectionFailure
        self.callback = callback
    }
    
    func start() {
        NSLog("🎍#\(connectionId) Connecting...")
        gcdSocket = GCDAsyncSocket(delegate: self, delegateQueue: dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0))
        gcdSocket!.connectToHost(endpoint.getHost()!, onPort: UInt16(endpoint.getPort()), withTimeout: connectionTimeout, error: nil)
    }
    
    func socket(sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        
        NSLog("🎍#\(connectionId) Connected...")
        
        if (UInt(self.endpoint.getType().ordinal()) == AMConnectionEndpoint_Type.TCP_TLS.rawValue) {
            NSLog("🎍#\(self.connectionId) Starring TLS Session...")
                
                // TODO: Check TLS
            sock.startTLS([//(id)kCFStreamSSLAllowsExpiredCertificates:@NO,
                //(id)kCFStreamSSLAllowsExpiredRoots:@NO,
                //(id)kCFStreamSSLAllowsAnyRoot:@YES,
                //(id)kCFStreamSSLValidatesCertificateChain:@YES,
                kCFStreamSSLPeerName:"actor.im"
                //(id)kCFStreamSSLLevel:(id)kCFStreamSocketSecurityLevelNegotiatedSSL,
                ])
        } else {
            if (self.isSocketOpen) {
                return
            }
            self.isSocketOpen = true
            
            self.requestReadHeader()
            self.connectionCreated(connection: self)
        }
    }
    
    func socketDidSecure(sock: GCDAsyncSocket!) {
        NSLog("🎍#\(connectionId) TLS connection established...")
        if (isSocketOpen) {
            return
        }
        isSocketOpen = true
        
        self.requestReadHeader()
        self.connectionCreated(connection: self)
    }

    func socketDidDisconnect(sock: GCDAsyncSocket!, withError err: NSError!) {
        if (isSocketOpen) {
            isSocketClosed = true
            
            NSLog("🎍#\(connectionId) Connection die")
            callback.onConnectionDie()
        } else {
            if (isSocketClosed) {
                return
            }
            isSocketClosed = true
            
            NSLog("🎍#\(connectionId) Connection failured")
            connectionFailure(connection: self)
        }
    }
    
    func requestReadHeader() {
        NSLog("🎍#\(connectionId) Request reading header...")
        gcdSocket?.readDataToLength(4, withTimeout: -1, tag: 0)
        // gcdSocket?.readDataWithTimeout(-1, tag: 0)
    }
    
    func requestReadBody(bodySize: UInt) {
        NSLog("🎍#\(connectionId) Request reading body \(bodySize)...")
        gcdSocket?.readDataToLength(bodySize, withTimeout: -1, tag: 1)
    }
    
    func socket(sock: GCDAsyncSocket!, didReadPartialDataOfLength partialLength: UInt, tag: Int) {
        NSLog("🎍#\(connectionId) didReadPartialDataOfLength \(partialLength)...")
    }
    
    func socket(sock: GCDAsyncSocket!, didReadData data: NSData!, withTag tag: Int) {
        if (tag == 0) {
            // Header
            if (data.length != 4) {
                fatalError("🎍#\(connectionId) Unknown header size");
            }
            var len = data.readUInt32();
            
            if (len == 0) {
                crashConnection()
                return
            } else if (len > 1024 * 1024 * 1024) {
                crashConnection()
                return
            }
            
            NSLog("🎍#\(connectionId) Received header \(len)...")
            requestReadBody(UInt(len));
        } else if (tag == 1) {
            // Body
            NSLog("🎍#\(connectionId) Received body \(data.length)...")
            
            var packageIndex = data.readUInt32();
            var package = data.subdataWithRange(NSMakeRange(4, Int(data.length - 8)))
            var crc32 = data.readUInt32(data.length - 4)
            
            // TODO: Add packageIndex and crc32 checks
            
            NSLog("🎍#\(connectionId) Loaded body #\(packageIndex)...")
            
            callback.onMessage(package.toJavaBytes(), withOffset: jint(0), withLen: jint(package.length))
            
            requestReadHeader()
        } else {
            fatalError("🎍#\(connectionId) Unknown tag");
        }
    }
    
    func socket(sock: GCDAsyncSocket!, didWriteDataWithTag tag: Int) {
        NSLog("🎍#\(connectionId) didWriteDataWithTag...")
    }
    
    func socket(sock: GCDAsyncSocket!, didWritePartialDataOfLength partialLength: UInt, tag: Int) {
        NSLog("🎍#\(connectionId) didWritePartialDataOfLength \(partialLength)...")
    }
    
    func post(data: IOSByteArray!, withOffset offset: jint, withLen len: jint) {
        if (isSocketClosed) {
            NSLog("🎍#\(connectionId) isSocketClosed...")
            return
        }
        
        // Prepare Transport package
        var dataToWrite = NSMutableData(capacity: Int(data.length() + 12))!
        dataToWrite.appendUInt32(UInt32(8 + data.length()))
        dataToWrite.appendUInt32(UInt32(self.outPackageIndex++))
        dataToWrite.appendData(data.toNSData().subdataWithRange(NSMakeRange(Int(offset), Int(len))))
        dataToWrite.appendData(CRC32.crc32(dataToWrite as NSData))
        
        // TODO: Propper timeout??
        NSLog("🎍#\(self.connectionId) Data posted to socket...")
        self.gcdSocket?.writeData(dataToWrite, withTimeout: -1, tag: 0)
    }
    
    func crashConnection() {
        if (isSocketClosed) {
            return
        }
        isSocketClosed = true
        gcdSocket?.disconnect()
    }
        
    func isClosed() -> Bool {
        return isSocketClosed
    }
    
    func close() {
        if (isSocketClosed) {
            return
        }
        isSocketClosed = true
        crashConnection()
    }
}