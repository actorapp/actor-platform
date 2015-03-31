//
//  DispatcherProvider.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 31.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class DispatcherProvider: NSObject, AMDispatcherProvider {
    func dispatchWithJavaLangRunnable(runnable: JavaLangRunnable!) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), {
            runnable.run()
        });
    }
}