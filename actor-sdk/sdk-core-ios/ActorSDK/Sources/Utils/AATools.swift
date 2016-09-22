//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import zipzap

class AATools {
    
    class func copyFileCommand(_ from: String, to: String) -> ACCommand {
        return CopyCommand(from: from, to: to)
    }
    
    class func zipDirectoryCommand(_ from: String, to: String) -> ACCommand {
        return ZipCommand(dir: from, to: to)
    }
    
    class func isValidEmail(_ testStr:String) -> Bool {
        
        let emailRegEx = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}"
        
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: testStr)
    }
}

private class ZipCommand: BackgroundCommand {
    
    fileprivate let dir: String
    fileprivate let to: String
    
    init(dir: String, to: String) {
        self.dir = dir
        self.to = to
    }
    
    fileprivate override func backgroundTask() throws {
        let rootPath = URL(fileURLWithPath: dir).lastPathComponent
        
        let zip = try ZZArchive(url: URL(fileURLWithPath: to), options: [ZZOpenOptionsCreateIfMissingKey: true])
        
        let subs = try FileManager.default.subpathsOfDirectory(atPath: dir)
        var entries = [ZZArchiveEntry]()
        for p in subs {
            
            // Full path of object
            let fullPath = "\(dir)/\(p)"
            let destPath = "\(rootPath)/\(p)"
            
            // Check path type: directory or file?
            var isDir : ObjCBool = false
            if FileManager.default.fileExists(atPath: fullPath, isDirectory: &isDir) {
                
                if !isDir.boolValue {
                    
                    // If file write file
                    entries.append(ZZArchiveEntry(fileName: destPath, compress: false, dataBlock: { (error) -> Data! in
                        
                        // TODO: Error handling?
                        return (try! Data(contentsOf: URL(fileURLWithPath: fullPath)))
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
    
    fileprivate override func backgroundTask() throws {
        try FileManager.default.copyItem(atPath: from, toPath: to)
    }
}

class BackgroundCommand: NSObject, ACCommand {
    
    func start(with callback: ACCommandCallback!) {
        DispatchQueue.global(priority: DispatchQueue.GlobalQueuePriority.default).async { () -> Void in
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
