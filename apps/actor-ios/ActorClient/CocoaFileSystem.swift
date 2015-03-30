//
//  CocoaFileSystem.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 13.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

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

@objc class CocoaFileSystem : NSObject, AMFileSystemProvider {
    
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
    
    func createTempFile() -> AMFileSystemReference! {
        var fileName = "/tmp/\(NSUUID().UUIDString)"
        NSLog("CreatingTempFile: \(appPath + fileName)" )
        NSFileManager.defaultManager().createFileAtPath(appPath + fileName, contents: NSData(), attributes: nil);
        return CocoaFile(path: fileName);
    }
    
    func commitTempFile(sourceFile: AMFileSystemReference!, withReference fileReference: AMFileReference!) -> AMFileSystemReference! {
        var manager = NSFileManager.defaultManager();
        
        
        var baseName = fileReference.getFileName();
        
        var index = 0;
        while(manager.fileExistsAtPath("\(appPath)/Documents/\(index)_\(baseName)")) {
            index = index + 1;
        }
        
        var resultPath = "/Documents/\(index)_\(baseName)";
        
        NSLog("CommitingFile: \(appPath + resultPath)" )
        
        var error : NSError?;
        manager.moveItemAtPath(appPath + sourceFile.getDescriptor()!, toPath: appPath + resultPath, error: &error)
        
        if (error == nil) {
            return CocoaFile(path: resultPath)
        }
    
        return nil
    }
    
    func fileFromDescriptor(descriptor: String!) -> AMFileSystemReference! {
        return CocoaFile(path: descriptor);
    }
    
    func isFsPersistent() -> Bool {
        return true;
    }
}

class CocoaFile : NSObject, AMFileSystemReference {
    
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
    
    func openWriteWithSize(size: jint) -> AMOutputFile! {
        
        var fileHandle = NSFileHandle(forWritingAtPath: realPath);

        if (fileHandle == nil) {
            return nil
        }
        
        fileHandle!.seekToFileOffset(UInt64(size))
        fileHandle!.seekToFileOffset(0)
        
        return CocoaOutputFile(fileHandle: fileHandle!);
    }
    
    func openRead() -> AMInputFile! {
        
        var fileHandle = NSFileHandle(forReadingAtPath: realPath);
        
        if (fileHandle == nil) {
            return nil
        }
        
        return CocoaInputFile(fileHandle: fileHandle!);
    }
}

class CocoaOutputFile : NSObject, AMOutputFile {
    
    let fileHandle: NSFileHandle;
    
    init(fileHandle:NSFileHandle){
        self.fileHandle = fileHandle;
    }

    func writeWithOffset(fileOffset: jint, withData data: IOSByteArray!, withDataOffset dataOffset: jint, withDataLen dataLen: jint) -> Bool {
        
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

class CocoaInputFile :NSObject, AMInputFile {
    
    let fileHandle:NSFileHandle;
    
    init(fileHandle:NSFileHandle){
        self.fileHandle = fileHandle;
    }
    
    func readAtOffset(fileOffset: jint, toArray data: IOSByteArray!, withArrayOffset offset: jint, withArrayLen len: jint) -> Bool {
        
        fileHandle.seekToFileOffset(UInt64(fileOffset));
        var readed:NSData = fileHandle.readDataOfLength(Int(len));
        
        var srcBuffer = UnsafeMutablePointer<UInt8>(readed.bytes);
        var destBuffer = UnsafeMutablePointer<UInt8>(data.buffer());
        var len = min(Int(len), Int(readed.length));
        for i in offset..<offset+len {
            destBuffer.memory = srcBuffer.memory;
            destBuffer++;
            srcBuffer++;
        }
        
        return true;
    }
    
    func close() -> Bool {
        self.fileHandle.closeFile()
        return true;

    }
}