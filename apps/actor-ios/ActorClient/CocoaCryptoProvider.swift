//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaCryptoProvider : BCBouncyCastleProvider {
    override init() {
        super.init(BCRandomProvider: CocoaRandomProvider())
    }
}

class CocoaRandomProvider : NSObject, BCRandomProvider {

    func randomBytesWithInt(length: jint) -> IOSByteArray! {
        var res = NSMutableData(capacity: Int(length))!
        for i in 0...length {
            res.appendByte(UInt8(arc4random_uniform(255)))
        }
        return res.toJavaBytes()
    }
    
    func randomIntWithInt(maxValue: jint) -> jint {
        return jint(arc4random_uniform(UInt32(maxValue)))
    }
    
    func nextBytesWithByteArray(data: IOSByteArray!) {
        var buffer = UnsafeMutablePointer<UInt8>(data.buffer())
        for i in 0...data.length() {
            buffer.memory = UInt8(arc4random_uniform(255))
            buffer++
        }
    }
    
    // Obsolete
    
    func generateBigIntegerWithInt(numBits: jint) -> JavaMathBigInteger! {
        return nil
    }
    
    func generateBigIntegerWithInt(numBits: jint, withInt certanity: jint) -> JavaMathBigInteger! {
        return nil
    }
    
}