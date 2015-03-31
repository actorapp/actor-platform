//
//  CocoaCryptoProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 31.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaCryptoProvider : BCBouncyCastleProvider {
    override init() {
        super.init(BCRandomProvider: AMJavaRandomProvider())
    }
}