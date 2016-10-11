//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAActorRuntime {
    
    fileprivate static var isInited = false
    
    class func configureRuntime() {
        if isInited {
            return
        }
        isInited = true
        
        ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorageRuntime())
        ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpRuntime())
        ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystemRuntime())
        ARCocoaNetworkProxyProvider.setNetworkRuntime(CocoaNetworkRuntime())
        ARCocoaAssetsProxyProvider.setAssetsRuntimeWith(CocoaAssetsRuntime())
        ARCocoaWebRTCProxyProvider.setWebRTCRuntime(CocoaWebRTCRuntime())
        ARCocoaLifecycleProxyProvider.setLifecycleRuntime(CocoaLifecycleRuntime())
        ARCocoaDispatcher.setDispatcherProxy(CocoaDispatcher())
        // ARRuntimeEnvironment.setIsProductionWithBoolean(true)
        ARCocoaCryptoProvider.setProxyProviderWithARCocoaCryptoProxy(CocoaCrypto())
    }
}
