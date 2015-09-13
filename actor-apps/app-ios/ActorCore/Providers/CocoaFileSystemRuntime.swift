//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Public methods for working with files
class CocoaFiles {
    class func pathFromDescriptor(path: String) -> String {
        var documentsFolders = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true)
        if (documentsFolders.count > 0) {
            let appPath = documentsFolders[0].asNS.stringByDeletingLastPathComponent
            return appPath + path
        } else {
            fatalError("Unable to load Application path")
        }
    }
}

// Implementation of FileSystem storage

@objc class CocoaFileSystemRuntime : NSObject, ARFileSystemRuntime {
    
    var appPath: String = ""
    
    let manager = NSFileManager.defaultManager()
    
    override init() {
        super.init()
        
        var documentsFolders = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true)
        if (documentsFolders.count > 0) {
            appPath = documentsFolders[0].asNS.stringByDeletingLastPathComponent
        } else {
            fatalError("Unable to load Application path")
        }
    }
    
    func createTempFile() -> ARFileSystemReference! {
        let fileName = "/tmp/\(NSUUID().UUIDString)"
        NSFileManager.defaultManager().createFileAtPath(appPath + fileName, contents: NSData(), attributes: nil)
        return CocoaFile(path: fileName)
    }
    
    func commitTempFile(sourceFile: ARFileSystemReference!, withFileId fileId: jlong, withFileName fileName: String!) -> ARFileSystemReference! {

        // Finding file available name
        var index = 0;
        while(manager.fileExistsAtPath("\(appPath)/Documents/\(index)_\(fileName)")) {
            index = index + 1;
        }
        let resultPath = "/Documents/\(index)_\(fileName)";
        
        // Moving file to new place
        do {
            try manager.moveItemAtPath(appPath + sourceFile.getDescriptor()!, toPath: appPath + resultPath)
            return CocoaFile(path: resultPath)
        } catch _ {
            return nil
        }
    }
    
    func fileFromDescriptor(descriptor: String!) -> ARFileSystemReference! {
        return CocoaFile(path: descriptor);
    }
    
    func isFsPersistent() -> Bool {
        return true;
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
    
    func getSize() -> jint {
        do {
            let attrs = try NSFileManager().attributesOfItemAtPath(realPath)
            return jint(NSDictionary.fileSize(attrs)())
        } catch _ {
            return 0
        }
    }
    
    func openWriteWithSize(size: jint) -> AROutputFile! {
        
        let fileHandle = NSFileHandle(forWritingAtPath: realPath);

        if (fileHandle == nil) {
            return nil
        }
        
        fileHandle!.seekToFileOffset(UInt64(size))
        fileHandle!.seekToFileOffset(0)
        
        return CocoaOutputFile(fileHandle: fileHandle!);
    }
    
    func openRead() -> ARInputFile! {
        
        let fileHandle = NSFileHandle(forReadingAtPath: realPath);
        
        if (fileHandle == nil) {
            return nil
        }
        
        return CocoaInputFile(fileHandle: fileHandle!);
    }
}

class CocoaOutputFile : NSObject, AROutputFile {
    
    let fileHandle: NSFileHandle;
    
    init(fileHandle:NSFileHandle){
        self.fileHandle = fileHandle;
    }
    
    func writeWithOffset(fileOffset: jint, withData data: IOSByteArray!, withDataOffset dataOffset: jint, withLength dataLen: jint) -> Bool {
        let toWrite = NSMutableData(length: Int(dataLen))!;
        var srcBuffer = UnsafeMutablePointer<UInt8>(data.buffer());
        var destBuffer = UnsafeMutablePointer<UInt8>(toWrite.bytes);
        for _ in 0..<dataLen {
            destBuffer.memory = srcBuffer.memory;
            destBuffer++;
            srcBuffer++;
        }
        
        fileHandle.seekToFileOffset(UInt64(fileOffset));
        fileHandle.writeData(toWrite)
        
        return true;
    }
    
    func close() -> Bool {
        self.fileHandle.synchronizeFile()
        self.fileHandle.closeFile()
        return true;
    }
}

class CocoaInputFile :NSObject, ARInputFile {
    
    let fileHandle:NSFileHandle;
    
    init(fileHandle:NSFileHandle){
        self.fileHandle = fileHandle;
    }
    
    func readWithOffset(fileOffset: jint, withData data: IOSByteArray!, withDataOffset offset: jint, withLength len: jint, withCallback callback: ARFileReadCallback!) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_LOW, 0)) {
            self.fileHandle.seekToFileOffset(UInt64(fileOffset));
            let readed:NSData = self.fileHandle.readDataOfLength(Int(len));
            
            var srcBuffer = UnsafeMutablePointer<UInt8>(readed.bytes);
            var destBuffer = UnsafeMutablePointer<UInt8>(data.buffer());
            let len = min(Int(len), Int(readed.length));
            for _ in offset..<offset+len {
                destBuffer.memory = srcBuffer.memory;
                destBuffer++;
                srcBuffer++;
            }
            
            callback.onFileReadWithOffset(fileOffset, withData: data, withDataOffset: offset, withLength: jint(len))
        }
    }
    
    func close() -> Bool {
        self.fileHandle.closeFile()
        return true;

    }
}