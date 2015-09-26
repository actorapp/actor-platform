//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


class Tools {
    class func copyFileCommand(from: String, to: String) -> ACCommand {
        return CopyCommand(from: from, to: to)
    }
    
    class func zipDirectoryCommand(from: String, to: String) -> ACCommand {
        return ZipCommand(dir: from, to: to)
    }
}

private class ZipCommand: BackgroundCommand {
    
    private let dir: String
    private let to: String
    
    init(dir: String, to: String) {
        self.dir = dir
        self.to = to
    }
    
    private override func backgroundTask() throws {
        let rootPath = NSURL(fileURLWithPath: dir).lastPathComponent!
        
        let zip = try ZZArchive(URL: NSURL(fileURLWithPath: to), options: [ZZOpenOptionsCreateIfMissingKey: true])
        
        let subs = try NSFileManager.defaultManager().subpathsOfDirectoryAtPath(dir)
        var entries = [ZZArchiveEntry]()
        for p in subs {
            
            // Full path of object
            let fullPath = "\(dir)/\(p)"
            let destPath = "\(rootPath)/\(p)"
            
            // Check path type: directory or file?
            var isDir : ObjCBool = false
            if NSFileManager.defaultManager().fileExistsAtPath(fullPath, isDirectory: &isDir) {
                
                if !isDir {
                    
                    // If file write file
                    entries.append(ZZArchiveEntry(fileName: destPath, compress: false, dataBlock: { (error) -> NSData! in
                        
                        // TODO: Error handling?
                        return NSData(contentsOfFile: fullPath)!
                    }))
                } else {
                    
                    // Create directory
                    
                    // Uncommenting this line causes unpack errors
                    // Also zip format doesn't have file paths
                    
                    // entries.append(ZZArchiveEntry(directoryName: destPath))
                }
            }
        }
        
        // Write entries
        try zip.updateEntries(entries)
    }
}

private class CopyCommand: BackgroundCommand {

    let from: String
    let to: String
    
    init(from: String, to: String) {
        self.from = from
        self.to = to
    }
    
    private override func backgroundTask() throws {
        try NSFileManager.defaultManager().copyItemAtPath(from, toPath: to)
    }
}

class BackgroundCommand: NSObject, ACCommand {
    
    func startWithCallback(callback: ACCommandCallback!) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) { () -> Void in
            do {
                try self.backgroundTask()
                callback.onResult(nil)
            } catch {
                callback.onError(nil)
            }
        }
    }
    
    func backgroundTask() throws {
        
    }
}
