//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import CommonCrypto

class CocoaCrypto: NSObject, ARCocoaCryptoProxyProvider {
    
    func createSHA256() -> ARDigest! {
        return SHA256Digest()
    }
    
    func createAES128(withKey key: IOSByteArray!) -> ARBlockCipher! {
        return AES128(key: key)
    }
}

class SHA256Digest: NSObject, ARDigest {
    
    let context = UnsafeMutablePointer<CC_SHA256_CTX>.allocate(capacity: 1)
    
    override init() {
        super.init()
        reset()
    }
    
    deinit {
        context.deallocate(capacity: 1)
    }
    
    func reset() {
        CC_SHA256_Init(context)
    }
    
    func update(_ src: IOSByteArray!, withOffset offset: jint, withLength length: jint) {
        
        let pointer = src
            .buffer()
            .advanced(by: Int(offset))
        
        CC_SHA256_Update(context, pointer, CC_LONG(length))
    }
    
    func doFinal(_ dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let pointer = dest.buffer()
            .advanced(by: Int(destOffset))
            .bindMemory(to: UInt8.self, capacity: Int(CC_SHA256_DIGEST_LENGTH))
        
        CC_SHA256_Final(pointer, context)
    }
    
    func getSize() -> jint {
        return jint(CC_SHA256_DIGEST_LENGTH)
    }
}

class AES128: NSObject, ARBlockCipher {
    
    var encryptor = UnsafeMutablePointer<CCCryptorRef?>.allocate(capacity: 1)
    var decryptor = UnsafeMutablePointer<CCCryptorRef?>.allocate(capacity: 1)
    
    init(key: IOSByteArray) {
        CCCryptorCreate(
            CCOperation(kCCEncrypt),
            CCAlgorithm(kCCAlgorithmAES),
            CCOptions(kCCOptionECBMode),
            key.buffer(), 32,
            nil,
            encryptor)
        
        CCCryptorCreate(
            CCOperation(kCCDecrypt),
            CCAlgorithm(kCCAlgorithmAES),
            CCOptions(kCCOptionECBMode),
            key.buffer(), 32,
            nil,
            decryptor)
    }
    
    deinit {
        encryptor.deallocate(capacity: 1)
        decryptor.deallocate(capacity: 1)
    }
    
    func encryptBlock(_ data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let src = data.buffer()
            .advanced(by: Int(offset))
        let dst = dest.buffer()
            .advanced(by: Int(destOffset))
        var bytesOut: Int = 0
        
        CCCryptorUpdate(encryptor.pointee, src, 16, dst, 32, &bytesOut)
    }
    
    func decryptBlock(_ data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let src = data.buffer()
            .advanced(by: Int(offset))
        let dst = dest.buffer()
            .advanced(by: Int(destOffset))
        var bytesOut: Int = 0
        
        CCCryptorUpdate(decryptor.pointee, src, 16, dst, 32, &bytesOut)
    }
    
    func getBlockSize() -> jint {
        return 16
    }
}
