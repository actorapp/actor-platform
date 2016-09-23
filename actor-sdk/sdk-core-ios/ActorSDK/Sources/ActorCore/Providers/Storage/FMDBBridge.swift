//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

extension ARListEngineRecord {
    public func dbQuery() -> AnyObject {
        if (self.getQuery() == nil) {
            return NSNull()
        } else {
            return self.getQuery()!.lowercased() as AnyObject
        }
    }
}

extension jlong {
    func toNSNumber() -> NSNumber {
        return NSNumber(value: self as Int64)
    }
}

extension jint {
    func toNSNumber() -> NSNumber {
        return NSNumber(value: self as Int32)
    }
}

extension JavaLangLong {
    func toNSNumber() -> NSNumber {
        return NSNumber(value: self.longLongValue() as Int64)
    }
}

extension Data {
    func readNSData(_ offset: Int, len: Int) -> Data {
        return self.subdata(in: self.startIndex.advanced(by: Int(offset))..<self.startIndex.advanced(by: Int(offset + len)))
    }
}
