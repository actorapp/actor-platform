//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

public class TableViewSeparator : UIView {

    public init(color: UIColor) {
        super.init(frame: CGRectZero)
        
        super.backgroundColor = color
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override var backgroundColor: UIColor? {
        get {
            return super.backgroundColor
        }
        set {
            
        }
    }
}