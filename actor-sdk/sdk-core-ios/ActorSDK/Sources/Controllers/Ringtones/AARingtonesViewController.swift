//
//  AARingtonesViewController.swift
//  ActorSDK
//
//  Created by Alexey Galaev on 5/27/16.
//  Copyright © 2016 Steve Kite. All rights reserved.
//

import Foundation
import UIKit
import AVFoundation

open class AARingtonesViewController: AATableViewController {
    
    var audioPlayer: AVAudioPlayer!
    var selectedRingtone: String = ""
    var completion: ((String) -> ())!
    
    let rootSoundDirectories: [String] = ["/Library/Ringtones"/*,"/System/Library/Audio/UISounds"*/]
    var directories: [String] = []
    var soundFiles: [(directory: String, files: [String])] = []
   
    init() {
        super.init(style: UITableViewStyle.plain)
        
        self.title = AALocalized("Ringtones")
        
        let cancelButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.plain, target: self, action: Selector("dismiss"))
        let doneButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.plain, target: self, action: Selector("dismiss"))
        self.navigationItem.setLeftBarButton(cancelButtonItem, animated: false)
        self.navigationItem.setRightBarButton(doneButtonItem, animated: false)
    }
    
    required public init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        for directory in rootSoundDirectories {
            directories.append(directory)
            
            let newSoundFile: (directory: String, files: [String]) = (directory, [])
            soundFiles.append(newSoundFile)
        }
        
        getDirectories()
        // loadSoundFiles()
        tableView.rowHeight = 44.0
        tableView.sectionIndexBackgroundColor = UIColor.clear
    }
    
    override open func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(true)
        if(audioPlayer != nil && audioPlayer.isPlaying){
            audioPlayer.stop()
        }
    }
    
    open override func viewDidDisappear(_ animated: Bool) {
        completion(selectedRingtone)
    }

    override open func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    func getDirectories() {
        let fileManager: FileManager = FileManager()
        for directory in rootSoundDirectories {
            let directoryURL: URL = URL(fileURLWithPath: "\(directory)", isDirectory: true)
            
            do {
                if let URLs: [URL] = try fileManager.contentsOfDirectory(at: directoryURL, includingPropertiesForKeys: [URLResourceKey.isDirectoryKey], options: FileManager.DirectoryEnumerationOptions()) {
                    var urlIsaDirectory: ObjCBool = ObjCBool(false)
                    for url in URLs {
                        if fileManager.fileExists(atPath: url.path, isDirectory: &urlIsaDirectory) {
                            if urlIsaDirectory.boolValue {
                                let directory: String = "\(url.relativePath)"
                                let files: [String] = []
                                let newSoundFile: (directory: String, files: [String]) = (directory, files)
                                directories.append("\(directory)")
                                soundFiles.append(newSoundFile)
                            }
                        }
                    }
                }
            } catch {
                debugPrint("\(error)")
            }
        }
    }
    
//    func loadSoundFiles() {
    
//        for i in 0...directories.count-1 {
//            let fileManager: FileManager = FileManager()
//            let directoryURL: URL = URL(fileURLWithPath: directories[i], isDirectory: true)
//            
//            do {
//                if let URLs: [URL] = try fileManager.contentsOfDirectory(at: directoryURL, includingPropertiesForKeys: [URLResourceKey.isDirectoryKey], options: FileManager.DirectoryEnumerationOptions()) {
//                    var urlIsaDirectory: ObjCBool = ObjCBool(false)
//                    for url in URLs {
//                        if fileManager.fileExists(atPath: url.path, isDirectory: &urlIsaDirectory) {
//                            if !urlIsaDirectory {
//                                soundFiles[i].files.append("\(url.lastPathComponent)")
//                            }
//                        }
//                    }
//                }
//            } catch {
//                debugPrint("\(error)")
//            }
//        }
//    }

//    override open func numberOfSections(in tableView: UITableView) -> Int {
//        return 1
//    }
//    
//    override open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
//        return soundFiles[section].files.count
//    }
    
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return "Ringtones"
    }
    
    func tableView(_ tableView: UITableView, estimatedHeightForRowAtIndexPath indexPath: IndexPath) -> CGFloat {
        return 44
    }
    
    func tableView(_ tableView: UITableView, heightForRowAtIndexPath indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
//    override open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//    
//        let fileName: String = soundFiles[(indexPath as NSIndexPath).section].files[(indexPath as NSIndexPath).row]
//        let cell: AACommonCell = tableView.dequeueCell(indexPath)
//        cell.style = .normal
//        let name = fileName.components(separatedBy: ".m4r")
//        cell.textLabel?.text = name.first
//        return cell
//    }
    
    func tableView(_ tableView: UITableView, didDeselectRowAtIndexPath indexPath: IndexPath) {
        if let cell = tableView.cellForRow(at: indexPath) as? AACommonCell {
            cell.style = .normal
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAtIndexPath indexPath: IndexPath) {

        let directory: String = soundFiles[(indexPath as NSIndexPath).section].directory
        let fileName: String = soundFiles[(indexPath as NSIndexPath).section].files[(indexPath as NSIndexPath).row]
        let fileURL: URL = URL(fileURLWithPath: "\(directory)/\(fileName)")
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: fileURL)
            audioPlayer.play()
        } catch {
            debugPrint("\(error)")
            selectedRingtone = ""
        }
        let cell = tableView.cellForRow(at: indexPath) as! AACommonCell
        selectedRingtone = soundFiles[(indexPath as NSIndexPath).section].files[(indexPath as NSIndexPath).row]
        cell.style = .checkmark
    }
}
