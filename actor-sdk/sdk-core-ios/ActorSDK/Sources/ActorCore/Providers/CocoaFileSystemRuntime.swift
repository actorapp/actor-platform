//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private let documentsFolder = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.userDomainMask, true)[0].asNS.deletingLastPathComponent

// Public methods for working with files
open class CocoaFiles {
    open class func pathFromDescriptor(_ path: String) -> String {
        return documentsFolder + path
    }
}

// Implementation of FileSystem storage

@objc class CocoaFileSystemRuntime : NSObject, ARFileSystemRuntime {
    
    let manager = FileManager.default
    
    override init() {
        super.init()
    }
    
    func createTempFile() -> ARFileSystemReference! {
        let fileName = "/tmp/\(UUID().uuidString)"
        FileManager.default.createFile(atPath: documentsFolder + fileName, contents: nil, attributes: nil)
        return CocoaFile(path: fileName)
    }
    
    func commitTempFile(_ sourceFile: ARFileSystemReference!, withFileId fileId: jlong, withFileName fileName: String!) -> ARFileSystemReference! {

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
        
        let srcUrl = URL(fileURLWithPath: documentsFolder + sourceFile.getDescriptor()!)
        let destUrl = URL(fileURLWithPath: documentsFolder + descriptor)
        
        // manager.replaceItemAtURL(srcUrl, withItemAtURL: destUrl, backupItemName: nil, options: 0, resultingItemURL: nil)
        
        // Moving file to new place
        
        do {
            try manager.replaceItem(at: destUrl, withItemAt: srcUrl, backupItemName: nil, options: FileManager.ItemReplacementOptions(rawValue: 0), resultingItemURL: nil)
            
            // try manager.moveItemAtPath(documentsFolder + sourceFile.getDescriptor()!, toPath: path)
            return CocoaFile(path: descriptor)
        } catch _ {
            return nil
        }
    }
    
    func file(fromDescriptor descriptor: String!) -> ARFileSystemReference! {
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
        return FileManager().fileExists(atPath: realPath);
    }
    
    func isInAppMemory() -> jboolean {
        return false
    }
    
    func isInTempDirectory() -> jboolean {
        return false
    }
    
    func getSize() -> jint {
        do {
            let attrs = try FileManager().attributesOfItem(atPath: realPath)
            return jint((attrs[FileAttributeKey.size] as! NSNumber).int32Value)
        } catch _ {
            return 0
        }
    }
    
    
    func openRead() -> ARPromise! {
        let fileHandle = FileHandle(forReadingAtPath: realPath)
        
        if (fileHandle == nil) {
            return ARPromise.failure(JavaLangRuntimeException(nsString: "Unable to open file"))
        }
        
        return ARPromise.success(CocoaInputFile(fileHandle: fileHandle!))
    }
    
    func openWrite(withSize size: jint) -> ARPromise! {
        let fileHandle = FileHandle(forWritingAtPath: realPath)
        
        if (fileHandle == nil) {
            return ARPromise.failure(JavaLangRuntimeException(nsString: "Unable to open file"))
        }
        
        fileHandle!.seek(toFileOffset: UInt64(size))
        fileHandle!.seek(toFileOffset: 0)
        
        return ARPromise.success(CocoaOutputFile(fileHandle: fileHandle!))
    }
}

class CocoaOutputFile : NSObject, AROutputFile {
    
    let fileHandle: FileHandle
    
    init(fileHandle:FileHandle) {
        self.fileHandle = fileHandle
    }
    
    func write(withOffset fileOffset: jint, withData data: IOSByteArray!, withDataOffset dataOffset: jint, withLength dataLen: jint) -> Bool {
        
        let pointer = data.buffer().advanced(by: Int(dataOffset))
        let srcData = Data(bytesNoCopy: UnsafeMutableRawPointer(pointer), count: Int(dataLen), deallocator: .none)
        
        fileHandle.seek(toFileOffset: UInt64(fileOffset))
        fileHandle.write(srcData)

        return true;
    }
    
    func close() -> Bool {
        self.fileHandle.synchronizeFile()
        self.fileHandle.closeFile()
        return true;
    }
}

class CocoaInputFile :NSObject, ARInputFile {
    
    let fileHandle:FileHandle
    
    init(fileHandle:FileHandle) {
        self.fileHandle = fileHandle
    }
    
    func read(withOffset fileOffset: jint, withLength len: jint) -> ARPromise! {
        
        return ARPromise { (resolver) in
            dispatchBackground {
                self.fileHandle.seek(toFileOffset: UInt64(fileOffset))
                
                let readed = self.fileHandle.readData(ofLength: Int(len))
                let data = IOSByteArray(length: UInt(len))
                let destBuffer = UnsafeMutableRawPointer(data!.buffer())!
                let destBindedBuffer = destBuffer.bindMemory(to: UInt8.self, capacity: readed.count)
                readed.copyBytes(to: destBindedBuffer, count: readed.count)
                
                resolver.result(ARFilePart(offset: fileOffset, withLength: len, withContents: data!))
            }
        }
    }
    
    func close() -> ARPromise! {
        self.fileHandle.closeFile()
        return ARPromise.success(nil)
    }
}
