//
//  CocoaExecution.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 22.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

extension UIViewController {
    
    func execute(command: AMCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    func execute(command: AMCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        MBProgressHUD.showHUDAddedTo(UIApplication.sharedApplication().keyWindow, animated: true)
        command.startWithAMCommandCallback(CocoaCallback(result: { (val:Any?) -> () in
            dispatch_async(dispatch_get_main_queue(), {
                MBProgressHUD.hideAllHUDsForView(UIApplication.sharedApplication().keyWindow, animated: true)
                successBlock?(val: val)
            })
            }, error: { (val) -> () in
                dispatch_async(dispatch_get_main_queue(), {
                    MBProgressHUD.hideAllHUDsForView(UIApplication.sharedApplication().keyWindow, animated: true)
                    failureBlock?(val: val)
                })
        }))
    }
    
}