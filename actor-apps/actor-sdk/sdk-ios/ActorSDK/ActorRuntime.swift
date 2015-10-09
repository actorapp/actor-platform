//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AAActorRuntime {
    
    private static var isInited = false
    
    class func configureRuntime() {
        if isInited {
            return
        }
        isInited = true
        
        ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorageRuntime())
        ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpRuntime())
        ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystemRuntime())
        ARCocoaNetworkProxyProvider.setNetworkRuntime(CocoaNetworkRuntime())
        ARCocoaAssetsProxyProvider.setAssetsRuntimeWithARAssetsRuntime(CocoaAssetsRuntime())
    }
}