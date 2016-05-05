//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private let documentsFolder = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true)[0].asNS.stringByDeletingLastPathComponent

// Public methods for working with files
class CocoaFiles {
    class func pathFromDescriptor(path: String) -> String {
        return documentsFolder + path
    }
}

// Implementation of FileSystem storage

@objc class CocoaFileSystemRuntime : NSObject, ARFileSystemRuntime {
    
    let manager = NSFileManager.defaultManager()
    
    override init() {
        super.init()
    }
    
    func createTempFile() -> ARFileSystemReference! {
        let fileName = "/tmp/\(NSUUID().UUIDString)"
        NSFileManager.defaultManager().createFileAtPath(documentsFolder + fileName, contents: nil, attributes: nil)
        return CocoaFile(path: fileName)
    }
    
    func commitTempFile(sourceFile: ARFileSystemReference!, withFileId fileId: jlong, withFileName fileName: String!) -> ARFileSystemReference! {

        // Finding file available name
        
        // let path = "\(documentsFolder)/Documents/\(fileId)_\(fileName)"
        let descriptor = "/Documents/\(fileId)_\(fileName)"
//        
//        if manager.fileExistsAtPath("\(documentsFolder)/Documents/\(fileId)_\(fileName)") {
//            do {
//                try manager.removeItemAtPath(path)
//            } catch _ {
//                return nil
//            }
//        }
        
        let srcUrl = NSURL(fileURLWithPath: documentsFolder + sourceFile.getDescriptor()!)
        let destUrl = NSURL(fileURLWithPath: documentsFolder + descriptor)
        
        // manager.replaceItemAtURL(srcUrl, withItemAtURL: destUrl, backupItemName: nil, options: 0, resultingItemURL: nil)
        
        // Moving file to new place
        
        do {
            try manager.replaceItemAtURL(destUrl, withItemAtURL: srcUrl, backupItemName: nil, options: NSFileManagerItemReplacementOptions(rawValue: 0), resultingItemURL: nil)
            
            // try manager.moveItemAtPath(documentsFolder + sourceFile.getDescriptor()!, toPath: path)
            return CocoaFile(path: descriptor)
        } catch _ {
            return nil
        }
    }
    
    func fileFromDescriptor(descriptor: String!) -> ARFileSystemReference! {
        return CocoaFile(path: descriptor)
    }
    
    func isFsPersistent() -> Bool {
        return true
    }
}

class CocoaFile : NSObject, ARFileSystemReference {
    
    let path: String;
    let realPath: String;
    
    init(path:String) {
        self.path = path
        self.realPath = CocoaFiles.pathFromDescriptor(path)
    }
    
    func getDescriptor() -> String! {
        return path;
    }
    
    func isExist() -> Bool {
        return NSFileManager().fileExistsAtPath(realPath);
    }
    
    func isInAppMemory() -> jboolean {
        return false
    }
    
    func isInTempDirectory() -> jboolean {
        return false
    }
    
    func getSize() -> jint {
        do {
            let attrs = try NSFileManager().attributesOfItemAtPath(realPath)
            return jint(NSDictionary.fileSize(attrs)())
        } catch _ {
            return 0
        }
    }
    
    
    func openRead() -> ARPromise! {
        let fileHandle = NSFileHandle(forReadingAtPath: realPath)
        
        if (fileHandle == nil) {
            return ARPromise.failure(JavaLangRuntimeException(NSString: "Unable to open file"))
        }
        
        return ARPromise.success(CocoaInputFile(fileHandle: fileHandle!))
    }
    
    func openWriteWithSize(size: jint) -> ARPromise! {
        let fileHandle = NSFileHandle(forWritingAtPath: realPath)
        
        if (fileHandle == nil) {
            return ARPromise.failure(JavaLangRuntimeException(NSString: "Unable to open file"))
        }
        
        fileHandle!.seekToFileOffset(UInt64(size))
        fileHandle!.seekToFileOffset(0)
        
        return ARPromise.success(CocoaOutputFile(fileHandle: fileHandle!))
    }
}

class CocoaOutputFile : NSObject, AROutputFile {
    
    let fileHandle: NSFileHandle
    
    init(fileHandle:NSFileHandle) {
        self.fileHandle = fileHandle
    }
    
    func writeWithOffset(fileOffset: jint, withData data: IOSByteArray!, withDataOffset dataOffset: jint, withLength dataLen: jint) -> Bool {
        
        let pointer = data.buffer().advancedBy(Int(dataOffset))
        let srcData = NSData(bytesNoCopy: pointer, length: Int(dataLen), freeWhenDone: false)
        
        fileHandle.seekToFileOffset(UInt64(fileOffset))
        fileHandle.writeData(srcData)

        return true;
    }
    
    func close() -> Bool {
        self.fileHandle.synchronizeFile()
        self.fileHandle.closeFile()
        return true;
    }
}

class CocoaInputFile :NSObject, ARInputFile {
    
    let fileHandle:NSFileHandle
    
    init(fileHandle:NSFileHandle) {
        self.fileHandle = fileHandle
    }
    
    func readWithOffset(fileOffset: jint, withLength len: jint) -> ARPromise! {
        
        return ARPromise { (resolver) in
            dispatchBackground {
                self.fileHandle.seekToFileOffset(UInt64(fileOffset))
                
                let readed: NSData = self.fileHandle.readDataOfLength(Int(len))
                let data = IOSByteArray(length: UInt(len))
                var srcBuffer = UnsafeMutablePointer<UInt8>(readed.bytes)
                var destBuffer = UnsafeMutablePointer<UInt8>(data.buffer())
                let readCount = min(Int(len), Int(readed.length))
                for _ in 0..<readCount {
                    destBuffer.memory = srcBuffer.memory
                    destBuffer = destBuffer.successor()
                    srcBuffer = srcBuffer.successor()
                }
                
                resolver.result(ARFilePart(offset: fileOffset, withLength: len, withContents: data))
            }
        }
    }
    
    func close() -> ARPromise! {
        self.fileHandle.closeFile()
        return ARPromise.success(nil)
    }
    
//    func readWithOffset(fileOffset: jint, withData data: IOSByteArray!, withDataOffset offset: jint, withLength len: jint, withCallback callback: ARFileReadCallback!) {
//        
//        dispatchBackground {
//            
//            self.fileHandle.seekToFileOffset(UInt64(fileOffset))
//            
//            let readed: NSData = self.fileHandle.readDataOfLength(Int(len))
//            
//            var srcBuffer = UnsafeMutablePointer<UInt8>(readed.bytes)
//            var destBuffer = UnsafeMutablePointer<UInt8>(data.buffer())
//            let len = min(Int(len), Int(readed.length))
//            for _ in offset..<offset+len {
//                destBuffer.memory = srcBuffer.memory
//                destBuffer = destBuffer.successor()
//                srcBuffer = srcBuffer.successor()
//            }
//            
//            callback.onFileReadWithOffset(fileOffset, withData: data, withDataOffset: offset, withLength: jint(len))
//        }
//    }
    
//    func close() -> Bool {
//        self.fileHandle.closeFile()
//        return true
//    }
}