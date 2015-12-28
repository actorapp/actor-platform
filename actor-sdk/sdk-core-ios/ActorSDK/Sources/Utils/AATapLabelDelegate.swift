//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol AATapLabelDelegate: class {
    
    func tapLabel(tapLabel: TapLabel, didSelectLink link: String)
    
}