//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AATapLabelDelegate: class {
    
    func tapLabel(tapLabel: TapLabel, didSelectLink link: String)
    
}