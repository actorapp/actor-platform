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
        return AES128(key: key)
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

class AES128: NSObject, ARBlockCipher {
    
    var encryptor = UnsafeMutablePointer<CCCryptorRef>.alloc(1)
    var decryptor = UnsafeMutablePointer<CCCryptorRef>.alloc(1)
    
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
        encryptor.dealloc(1)
        decryptor.dealloc(1)
    }
    
    func encryptBlock(data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let src = data.buffer()
            .advancedBy(Int(offset))
        let dst = dest.buffer()
            .advancedBy(Int(destOffset))
        var bytesOut: Int = 0
        
        CCCryptorUpdate(encryptor.memory, src, 16, dst, 32, &bytesOut)
    }
    
    func decryptBlock(data: IOSByteArray!, withOffset offset: jint, toDest dest: IOSByteArray!, withOffset destOffset: jint) {
        
        let src = data.buffer()
            .advancedBy(Int(offset))
        let dst = dest.buffer()
            .advancedBy(Int(destOffset))
        var bytesOut: Int = 0
        
        CCCryptorUpdate(decryptor.memory, src, 16, dst, 32, &bytesOut)
    }
    
    func getBlockSize() -> jint {
        return 16
    }
}