//
//  AlertViewDelegate.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 17.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class SwiftAlertDelegate: NSObject, UIAlertViewDelegate {
    
    private var closure: (alertView: UIAlertView, index: Int)->()
    
    init(closure: (alertView: UIAlertView, index: Int)->()) {
        self.closure = closure
    }
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        closure(alertView: alertView, index: buttonIndex)
    }
}