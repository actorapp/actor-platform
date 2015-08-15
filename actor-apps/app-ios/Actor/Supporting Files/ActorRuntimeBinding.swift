//
//  ActorRuntimeBinding.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 15.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

private var isInited = false

func initActorRuntime() {
    if !isInited {
        ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorage())
        ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystem())
        ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpProvider())
        // ARCocoaNetworkProxyProvider.setNetworkRuntime(<#networkRuntime: ARNetworkRuntime!#>)
        isInited = true
    }
    
    var async = GCDAsyncSocket()
}