//
//  TableViewSelector.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 19.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

class TableViewSeparator : UIView {

    init(color: UIColor) {
        super.init(frame: CGRectZero)
        
        super.backgroundColor = color
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override var backgroundColor: UIColor? {
        get {
            return super.backgroundColor
        }
        set {
            
        }
    }
}