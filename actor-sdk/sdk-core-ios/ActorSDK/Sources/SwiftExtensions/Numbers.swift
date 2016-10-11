//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension Int {
    public func format(_ f: String) -> String {
        return NSString(format: "%\(f)d" as NSString, self) as String
    }
}

public extension Double {
    public func format(_ f: String) -> String {
        return NSString(format: "%\(f)f" as NSString, self) as String
    }
}

public extension TimeInterval {
    public var time:String {
        return String(format:"%02d:%02d:%02d.%03d", Int((self/3600.0).truncatingRemainder(dividingBy: 60)),Int((self/60.0).truncatingRemainder(dividingBy: 60)), Int((self).truncatingRemainder(dividingBy: 60) ), Int((self*1000).truncatingRemainder(dividingBy: 1000) ) )
    }
}
