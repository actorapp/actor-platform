//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension Array {
    public func contains<T where T : Equatable>(obj: T) -> Bool {
        return self.filter({$0 as? T == obj}).count > 0
    }
}
