//
//  CocoaCryptoProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 31.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaCryptoProvider : BCBouncyCastleProvider {
    override func generateRSA1024KeyPair() -> AMCryptoKeyPair {
        var data = NSData()
        return AMCryptoKeyPair(byteArray: data.toJavaBytes(), withByteArray: data.toJavaBytes())
    }
}