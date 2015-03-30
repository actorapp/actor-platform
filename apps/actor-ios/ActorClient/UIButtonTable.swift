//
//  UIButtonTable.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 22.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class UIButtonTable : UIButton {
    
    override var highlighted: Bool {
        get {
            return super.highlighted
        }
        set (val) {
            if (val) {
                backgroundColor = Resources.SelectorColor
            } else {
                backgroundColor = UIColor.clearColor()
            }
            super.highlighted = val
        }
    }
}