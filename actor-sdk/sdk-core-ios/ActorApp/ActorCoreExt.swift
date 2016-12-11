//
//  ActorCoreExt.swift
//  ActorApp
//
//  Created by Diego Ferreira da Silva on 08/12/16.
//  Copyright © 2016 Actor LLC. All rights reserved.
//

import Foundation
import ActorSDK

open class AAAPromiseFunc: NSObject, ARPromiseFunc {
    
    let closure: (_ resolver: ARPromiseResolver) -> ()
    init(closure: @escaping (_ resolver: ARPromiseResolver) -> ()){
        self.closure = closure
    }
    
    open func exec(_ resolver: ARPromiseResolver) {
        closure(resolver)
    }
}

extension ARPromise {
    convenience init(closure: @escaping (_ resolver: ARPromiseResolver) -> ()) {
        self.init(executor: AAAPromiseFunc(closure: closure))
    }
}
