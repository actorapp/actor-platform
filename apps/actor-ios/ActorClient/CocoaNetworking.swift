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
    
    func createConnection(connectionId: jint,
        withMTProtoVersion mtprotoVersion: jint,
        withApiMajorVersion apiMajorVersion: jint,
        withApiMinorVersion apiMinorVersion: jint,
        withEndpoint endpoint: AMConnectionEndpoint!,
        withCallback callback: AMConnectionCallback!,
        withCreateCallback createCallback: AMCreateConnectionCallback!) {
        
        var connection = SwiftCocoaConnection(connectionId: connectionId, withEndpoint: endpoint, withCallback: callback, connectionCreated: { (connection) -> () in
                createCallback.onConnectionCreated(connection)
            }, connectionFailure: { (connection) -> () in
                createCallback.onConnectionCreateError()
            })
        connection.start()
            
        objc_sync_enter(syncObject)
        pendingConnection.append(connection)
        objc_sync_exit(syncObject)
    }
}

class SwiftCocoaConnection: NSObject, AMConnection, GCDAsyncSocketDelegate {

    let TAG_HANDSHAKE = 1
    let TAG_PACKAGE_HEADER = 2
    let TAG_PACKAGE_MT = 3
    let TAG_PACKAGE_SERVICE = 4
    let TAG_PACKAGE_PING = 5
    let TAG_PACKAGE_PONG = 6
    let TAG_PACKAGE_DROP = 7
    let TAG_PACKAGE_REDIRECT = 8
    let TAG_PACKAGE_ACK = 9
    
    let HANDSHAKE_TIMEOUT = 5.0
    let CONNECTION_TIMEOUT = 5.0
    
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
    
    // Initialize connection
    func start() {
        NSLog("🎍#\(connectionId) Connecting to \(endpoint.getHost()):\(endpoint.getPort())...")
        gcdSocket = GCDAsyncSocket(delegate: self, delegateQueue: dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0))
        gcdSocket!.connectToHost(endpoint.getHost()!, onPort: UInt16(endpoint.getPort()), withTimeout: CONNECTION_TIMEOUT, error: nil)
    }
    
    // After connection successful
    func socket(sock: GCDAsyncSocket!, didConnectToHost host: String!, port: UInt16) {
        NSLog("🎍#\(connectionId) Connected...")
        
        if (UInt(self.endpoint.getType().ordinal()) == AMConnectionEndpoint_Type.TCP_TLS.rawValue) {
            NSLog("🎍#\(self.connectionId) Starring TLS Session...")
            sock.startTLS(nil)
        } else {
            performHandshake()
        }
    }
    // After TLS successful
    func socketDidSecure(sock: GCDAsyncSocket!) {
        performHandshake()
    }
    
    // Performing handshake
    func performHandshake() {
        if (isSocketOpen) {
            return
        }
        isSocketOpen = true
        
        NSLog("🎍#\(connectionId) Sending Handshake request...")
        var dataToWrite = NSMutableData(capacity: Int(32 + 4 + 3))!
        dataToWrite.appendByte(1)
        dataToWrite.appendByte(1)
        dataToWrite.appendByte(0)
        dataToWrite.appendUInt32(32)
        for i in 1...32 {
            dataToWrite.appendByte(UInt8(arc4random_uniform(255)))
        }
        gcdSocket?.writeData(dataToWrite, withTimeout: -1, tag: 0)

        NSLog("🎍#\(connectionId) Request Handshake response...")
        gcdSocket?.readDataToLength(35, withTimeout: HANDSHAKE_TIMEOUT, tag: TAG_HANDSHAKE)
    }
    
    // Handshake response
    func handshakeReponse(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        var mtprotoVersion = data.readUInt8()
        var apiMajorVersion = data.readUInt8(1)
        var apiMinorVersion = data.readUInt8(2)
        var sha256 = data.readNSData(3, len: 32)
        // TODO: Check sha256
        
        NSLog("🎍#\(connectionId) Handshake response received \(mtprotoVersion),\(apiMajorVersion),\(apiMinorVersion)")
        
        connectionCreated(connection: self)
        
        requestReadHeader()
    }
    
    // Reading package
    func requestReadHeader() {
        NSLog("🎍#\(connectionId) Request reading header...")
        gcdSocket?.readDataToLength(9, withTimeout: -1, tag: TAG_PACKAGE_HEADER)
    }
    
    func headerRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        var packageIndex = data.readUInt32(0)
        var header = data.readUInt8(4)
        var size = data.readUInt32(5)
        var readTag = TAG_PACKAGE_SERVICE;
        
        NSLog("🎍#\(connectionId) Request reading package #\(header)...")
        if (header == 0) {
            readTag = TAG_PACKAGE_MT
        } else if (header == 1) {
            readTag = TAG_PACKAGE_PING
        } else if (header == 2) {
            readTag = TAG_PACKAGE_PONG
        } else if (header == 6) {
            readTag = TAG_PACKAGE_ACK
        } else if (header == 4) {
            readTag = TAG_PACKAGE_REDIRECT
        } else if (header == 3) {
            readTag = TAG_PACKAGE_DROP
        }
        gcdSocket?.readDataToLength(UInt(size + 4), withTimeout: -1, tag: readTag)
    }
    
    // Packages received
    // MTProto package
    func mtRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        var realData = data.readNSData(0, len: data.length - 4)
        var crc32 = data.readNSData(realData.length, len: 4)
        
        NSLog("🎍#\(connectionId) Body received \(realData.length - 4)")
        
        callback.onMessage(realData.toJavaBytes(), withOffset: jint(0), withLen: jint(realData.length))
    }
    
    // Ping package
    func pingRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        
    }
    
    // Pong package
    func pongRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        
    }
    
    // Drop package
    func dropRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        
        crashConnection()
    }
    
    // Redirect package
    func redirectRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {

    }
    
    // Ack package
    func ackRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        
    }

    // Unknown Service package
    func serviceRead(sock: GCDAsyncSocket!, didReadData data: NSData!) {
        // Just ignore this
    }
    
    // Socket data receive
    func socket(sock: GCDAsyncSocket!, didReadData data: NSData!, withTag tag: Int) {
        if (tag == TAG_HANDSHAKE) {
            handshakeReponse(sock, didReadData: data)
        } else if (tag == TAG_PACKAGE_HEADER) {
            headerRead(sock, didReadData: data)
        } else if (tag == TAG_PACKAGE_MT) {
            mtRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_PING) {
            pingRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_PONG) {
            pongRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_DROP) {
            dropRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_REDIRECT) {
            redirectRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_ACK) {
            ackRead(sock, didReadData: data)
            requestReadHeader()
        } else if (tag == TAG_PACKAGE_SERVICE) {
            serviceRead(sock, didReadData: data)
            requestReadHeader()
        } else {
            fatalError("🎍#\(connectionId) Unknown tag #\(tag)")
        }
    }
    
    // Socket data send
    func post(data: IOSByteArray!, withOffset offset: jint, withLen len: jint) {
        if (isSocketClosed) {
            NSLog("🎍#\(connectionId) isSocketClosed...")
            return
        }
        
        // Prepare Transport package
        var realData = data.toNSData().subdataWithRange(NSMakeRange(Int(offset), Int(len)))
        var dataToWrite = NSMutableData(capacity: Int(data.length() + 13))!
        dataToWrite.appendUInt32(UInt32(self.outPackageIndex++))
        dataToWrite.appendByte(0)
        dataToWrite.appendUInt32(UInt32(len))
        dataToWrite.appendData(realData)
        dataToWrite.appendData(CRC32.crc32(realData))
        
        // TODO: Propper timeout??
        NSLog("🎍#\(self.connectionId) Data posted to socket...")
        self.gcdSocket?.writeData(dataToWrite, withTimeout: -1, tag: 0)
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