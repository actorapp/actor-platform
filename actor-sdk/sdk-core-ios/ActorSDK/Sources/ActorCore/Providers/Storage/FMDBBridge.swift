//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

extension ARListEngineRecord {
    public func dbQuery() -> AnyObject {
        if (self.getQuery() == nil) {
            return NSNull()
        } else {
            return self.getQuery()!.lowercaseString
        }
    }
}

extension jlong {
    func toNSNumber() -> NSNumber {
        return NSNumber(longLong: self)
    }
}

extension jint {
    func toNSNumber() -> NSNumber {
        return NSNumber(int: self)
    }
}

extension JavaLangLong {
    func toNSNumber() -> NSNumber {
        return NSNumber(longLong: self.longLongValue())
    }
}

extension NSData {
    
    func readNSData(offset: Int, len: Int) -> NSData {
        return self.subdataWithRange(NSMakeRange(Int(offset), Int(len)))
    }    
}