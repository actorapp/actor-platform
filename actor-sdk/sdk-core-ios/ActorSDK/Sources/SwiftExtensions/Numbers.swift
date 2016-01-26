//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension Int {
    public func format(f: String) -> String {
        return NSString(format: "%\(f)d", self) as String
    }
}

public extension Double {
    public func format(f: String) -> String {
        return NSString(format: "%\(f)f", self) as String
    }
}

public extension NSTimeInterval {
    public var time:String {
        return String(format:"%02d:%02d:%02d.%03d", Int((self/3600.0)%60),Int((self/60.0)%60), Int((self) % 60 ), Int(self*1000 % 1000 ) )
    }
}
