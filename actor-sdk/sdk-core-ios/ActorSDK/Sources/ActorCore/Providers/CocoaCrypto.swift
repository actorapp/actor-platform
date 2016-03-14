//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import IDZSwiftCommonCrypto

class CocoaCrypto: NSObject, ARCocoaCryptoProxyProvider {
    
    func createSHA256() -> ARDigest! {
        return SHA256Digest()
    }
}

class SHA256Digest: NSObject, ARDigest {
    
    var digest = Digest(algorithm: .SHA256)
    
    func reset() {
        digest = Digest(algorithm: .SHA256)
    }
    
    func update(src: IOSByteArray!, withOffset offset: jint, withLength length: jint) {
        digest.update(src.toNSData().subdataWithRange(NSRange(location: Int(offset), length: Int(length))))
    }
    
    func doFinal(dest: IOSByteArray!, withOffset destOffset: jint) {
        let res = digest.final()
        for i: UInt in 0..<32 {
            dest.replaceByteAtIndex(UInt(destOffset) + i, withByte: jbyte(bitPattern: res[Int(i)]))
        }
    }
    
    func getDigestSize() -> jint {
        return 32
    }
}