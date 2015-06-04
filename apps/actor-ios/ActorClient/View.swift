//
//  View.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 17.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

extension UIView {
    func hideView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 0
        })
    }
    
    func showView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 1
        })
    }
}