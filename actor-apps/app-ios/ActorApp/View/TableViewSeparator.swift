//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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