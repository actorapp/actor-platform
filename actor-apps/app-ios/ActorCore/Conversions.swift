//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension NSData {
    func toJavaBytes() -> IOSByteArray {
        return IOSByteArray(bytes: UnsafePointer<jbyte>(self.bytes), count: UInt(self.length))
    }
    
    func readUInt8() -> UInt8 {
        var raw: UInt8 = 0;
        self.getBytes(&raw, length: 1)
        return raw
    }
    
    func readUInt8(offset: Int) -> UInt8 {
        var raw: UInt8 = 0;
        self.getBytes(&raw, range: NSMakeRange(offset, 1))
        return raw
    }

    func readUInt32() -> UInt32 {
        var raw: UInt32 = 0;
        self.getBytes(&raw, length: 4)
        return raw.bigEndian
    }
    
    func readUInt32(offset: Int) -> UInt32 {
        var raw: UInt32 = 0;
        self.getBytes(&raw, range: NSMakeRange(offset, 4))
        return raw.bigEndian
    }
    
    func readNSData(offset: Int, len: Int) -> NSData {
        return self.subdataWithRange(NSMakeRange(Int(offset), Int(len)))
    }
}

extension ACMessage {
    var isOut: Bool {
        get {
            return Actor.myUid() == self.senderId
        }
    }
}

//extension ACAuthStateEnum {
//    
//}
//
////public func ==(lhs: ACAuthStateEnum, rhs: ACAuthStateEnum) -> Bool {
////    return lhs.ordinal() == rhs.ordinal()
////}

extension JavaUtilAbstractCollection : SequenceType {
    
    public func generate() -> NSFastGenerator {
        return NSFastGenerator(self)
    }    
}

extension ACPeer {
    var isGroup: Bool {
        get {
            return UInt(self.peerType.ordinal()) == ACPeerType.GROUP.rawValue
        }
    }
}

extension NSMutableData {
    func appendUInt32(value: UInt32) {
      var raw = value.bigEndian
        self.appendBytes(&raw, length: 4)
    }
    func appendByte(value: UInt8) {
        var raw = value
        self.appendBytes(&raw, length: 1)
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

extension ARListEngineRecord {
    func dbQuery() -> AnyObject {
        if (self.getQuery() == nil) {
            return NSNull()
        } else {
            return self.getQuery().lowercaseString
        }
    }
}

extension NSMutableData {
    
    /** Convenient way to append bytes */
    internal func appendBytes(arrayOfBytes: [UInt8]) {
        self.appendBytes(arrayOfBytes, length: arrayOfBytes.count)
    }
    
}

extension NSData {
    
    public func checksum() -> UInt16 {
        var s:UInt32 = 0;
        
        var bytesArray = self.bytes();
        
        for (var i = 0; i < bytesArray.count; i++) {
            s = s + UInt32(bytesArray[i])
        }
        s = s % 65536;
        return UInt16(s);
    }
}

extension JavaUtilArrayList {
    
    func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.size() {
            res.append(self.getWithInt(i) as! T)
        }
        return res
    }
    
    func toSwiftArray() -> [AnyObject] {
        var res = [AnyObject]()
        for i in 0..<self.size() {
            res.append(self.getWithInt(i))
        }
        return res
    }
}

extension IOSObjectArray {
    
    func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.length() {
            res.append(self.objectAtIndex(UInt(i)) as! T)
        }
        return res
    }
    
    func toSwiftArray() -> [AnyObject] {
        var res = [AnyObject]()
        for i in 0..<self.length() {
            res.append(self.objectAtIndex(UInt(i)))
        }
        return res
    }
}

extension NSData {
    
    public var hexString: String {
        return self.toHexString()
    }
    
    func toHexString() -> String {
        let count = self.length / sizeof(UInt8)
        var bytesArray = [UInt8](count: count, repeatedValue: 0)
        self.getBytes(&bytesArray, length:count * sizeof(UInt8))
        
        var s:String = "";
        for byte in bytesArray {
            s = s + (NSString(format:"%02X", byte) as String)
        }
        return s;
    }
    
    func bytes() -> [UInt8] {
        let count = self.length / sizeof(UInt8)
        var bytesArray = [UInt8](count: count, repeatedValue: 0)
        self.getBytes(&bytesArray, length:count * sizeof(UInt8))
        return bytesArray
    }
    
    class public func withBytes(bytes: [UInt8]) -> NSData {
        return NSData(bytes: bytes, length: bytes.count)
    }
}