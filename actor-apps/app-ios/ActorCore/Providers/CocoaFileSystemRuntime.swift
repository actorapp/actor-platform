//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Public methods for working with files
class CocoaFiles {
    class func pathFromDescriptor(path: String) -> String {
        var manager = NSFileManager.defaultManager();
        var documentsFolders = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true)!;
        if (documentsFolders.count > 0) {
            var appPath = (documentsFolders[0] as! String).stringByDeletingLastPathComponent
            return appPath + path
        } else {
            fatalError("Unable to load Application path")
        }
    }
}

// Implementation of FileSystem storage

@objc class CocoaFileSystemRuntime : NSObject, ARFileSystemRuntime {
    
    var appPath: String = ""
    
    override init() {
        super.init()
        
        var manager = NSFileManager.defaultManager();
        var documentsFolders = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask, true)!;
        if (documentsFolders.count > 0) {
            appPath = (documentsFolders[0] as! String).stringByDeletingLastPathComponent
        } else {
            fatalError("Unable to load Application path")
        }
    }
    
    func createTempFile() -> ARFileSystemReference! {
        var fileName = "/tmp/\(NSUUID().UUIDString)"
        NSFileManager.defaultManager().createFileAtPath(appPath + fileName, contents: NSData(), attributes: nil);
        return CocoaFile(path: fileName);
    }
    
    func commitTempFile(sourceFile: ARFileSystemReference!, withFileId fileId: jlong, withFileName fileName: String!) -> ARFileSystemReference! {
        var manager = NSFileManager.defaultManager();
        
        var baseName = fileName;
        
        var index = 0;
        while(manager.fileExistsAtPath("\(appPath)/Documents/\(index)_\(baseName)")) {
            index = index + 1;
        }
        
        var resultPath = "/Documents/\(index)_\(baseName)";
        
        var error : NSError?;
        manager.moveItemAtPath(appPath + sourceFile.getDescriptor()!, toPath: appPath + resultPath, error: &error)
        
        if (error == nil) {
            return CocoaFile(path: resultPath)
        }
        
        return nil
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
        
        var error:NSError?;
        
        var attrs = NSFileManager().attributesOfItemAtPath(realPath, error: &error);
        
        if (error != nil) {
            return 0;
        }
        
        return jint(NSDictionary.fileSize(attrs!)());
    }
    
    func openWriteWithSize(size: jint) -> AROutputFile! {
        
        var fileHandle = NSFileHandle(forWritingAtPath: realPath);

        if (fileHandle == nil) {
            return nil
        }
        
        fileHandle!.seekToFileOffset(UInt64(size))
        fileHandle!.seekToFileOffset(0)
        
        return CocoaOutputFile(fileHandle: fileHandle!);
    }
    
    func openRead() -> ARInputFile! {
        
        var fileHandle = NSFileHandle(forReadingAtPath: realPath);
        
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
        var toWrite = NSMutableData(length: Int(dataLen))!;
        var srcBuffer = UnsafeMutablePointer<UInt8>(data.buffer());
        var destBuffer = UnsafeMutablePointer<UInt8>(toWrite.bytes);
        for i in 0..<dataLen {
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
            var readed:NSData = self.fileHandle.readDataOfLength(Int(len));
            
            var srcBuffer = UnsafeMutablePointer<UInt8>(readed.bytes);
            var destBuffer = UnsafeMutablePointer<UInt8>(data.buffer());
            var len = min(Int(len), Int(readed.length));
            for i in offset..<offset+len {
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