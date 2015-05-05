//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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