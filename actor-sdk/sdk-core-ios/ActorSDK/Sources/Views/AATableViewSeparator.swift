//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

 class AATableViewSeparator : UIView {

    init(color: UIColor) {
        super.init(frame: CGRect.zero)
        
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
