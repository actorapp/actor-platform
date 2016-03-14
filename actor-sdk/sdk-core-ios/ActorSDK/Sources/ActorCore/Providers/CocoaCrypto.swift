//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import CommonCrypto

class CocoaCrypto: NSObject, ARCocoaCryptoProxyProvider {
    
    func createSHA256() -> ARDigest! {
        return SHA256Digest()
    }
    
    func createAES128WithKey(key: IOSByteArray!) -> ARBlockCipher! {
        //return AES128(key: key)
        return nil
    }
}

class SHA256Digest: NSObject, ARDigest {
    
    let context = UnsafeMutablePointer<CC_SHA256_CTX>.alloc(1)
    
    override init() {
        super.init()
        reset()
    }
    
    deinit {
        context.dealloc(1)
    }
    
    func reset() {
        CC_SHA256_Init(context)
    }
    
    func update(src: IOSByteArray!, withOffset offset: jint, withLength length: jint) {
        
        let pointer = src
            .buffer()
            .advancedBy(Int(offset))
        
        CC_SHA256_Update(context, pointer, CC_LONG(length))
    }
    
    func doFinal(dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let pointer = UnsafeMutablePointer<UInt8>(dest.buffer())
            .advancedBy(Int(destOffset))
        
        CC_SHA256_Final(pointer, context)
    }
    
    func getDigestSize() -> jint {
        return jint(CC_SHA256_DIGEST_LENGTH)
    }
}

//class AES128: NSObject, ARBlockCipher {
//    
//    var key = [UInt8]()
//    let encryptor: Cryptor
//    let decryptor: Cryptor
//    
//    init(key: IOSByteArray) {
//        for i in 0..<16 {
//            let bt = key.byteAtIndex(UInt(i))
//            self.key.append(UInt8(bitPattern: Int8(bt)))
//        }
//        encryptor = Cryptor(operation: .Encrypt , algorithm: .AES, options: .ECBMode, key: self.key, iv: [])
//        decryptor = Cryptor(operation: .Decrypt , algorithm: .AES, options: .ECBMode, key: self.key, iv: [])
//    }
//    
//    func encryptBlock(data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
//        let src = data.toNSData().subdataWithRange(NSRange(location: Int(offset), length: Int(16)))
//        var res: [UInt8] = [UInt8](count: 16, repeatedValue: 0)
//        encryptor.update(src, byteArrayOut: &res)
//        for i: UInt in 0..<16 {
//            dest.replaceByteAtIndex(UInt(destOffset) + i, withByte: jbyte(bitPattern: res[Int(i)]))
//        }
//    }
//    
//    func decryptBlock(data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
//        let src = data.toNSData().subdataWithRange(NSRange(location: Int(offset), length: Int(16)))
//        decryptor.update(src)
//        let res = decryptor.final()!
//        for i: UInt in 0..<16 {
//            dest.replaceByteAtIndex(UInt(destOffset) + i, withByte: jbyte(bitPattern: res[Int(i)]))
//        }
//    }
//    
//    func getBlockSize() -> jint {
//        return 16
//    }
//}