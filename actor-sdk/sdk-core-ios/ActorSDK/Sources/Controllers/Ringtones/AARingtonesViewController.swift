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

public class AARingtonesViewController: AATableViewController {
    
    var audioPlayer: AVAudioPlayer!
    var selectedRingtone: String = ""
   
    init() {
        super.init(style: UITableViewStyle.Plain)
        
        self.title = AALocalized("Ringtones")
        
        let cancelButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: Selector("dismiss"))
        let doneButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.Plain, target: self, action: Selector("dismiss"))
        self.navigationItem.setLeftBarButtonItem(cancelButtonItem, animated: false)
        self.navigationItem.setRightBarButtonItem(doneButtonItem, animated: false)
    }
    
    required public init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(true)
        if(audioPlayer != nil && audioPlayer.playing){
        audioPlayer.stop()
        }
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        for directory in rootSoundDirectories {
            directories.append(directory)
            
            let newSoundFile: (directory: String, files: [String]) = (directory, [])
            soundFiles.append(newSoundFile)
        }
        getDirectories()
        loadSoundFiles()
        tableView.rowHeight = 44.0
        tableView.sectionIndexBackgroundColor = UIColor.clearColor()
    }

    let rootSoundDirectories: [String] = ["/Library/Ringtones"/*,"/System/Library/Audio/UISounds"*/]
    
    var directories: [String] = []

    var soundFiles: [(directory: String, files: [String])] = []
    
    override public func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    func getDirectories() {
        let fileManager: NSFileManager = NSFileManager()
        for directory in rootSoundDirectories {
            let directoryURL: NSURL = NSURL(fileURLWithPath: "\(directory)", isDirectory: true)
            
            do {
                if let URLs: [NSURL] = try fileManager.contentsOfDirectoryAtURL(directoryURL, includingPropertiesForKeys: [NSURLIsDirectoryKey], options: NSDirectoryEnumerationOptions()) {
                    var urlIsaDirectory: ObjCBool = ObjCBool(false)
                    for url in URLs {
                        if fileManager.fileExistsAtPath(url.path!, isDirectory: &urlIsaDirectory) {
                            if urlIsaDirectory {
                                let directory: String = "\(url.relativePath!)"
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
    
    func loadSoundFiles() {
    
        for i in 0...directories.count-1 {
            let fileManager: NSFileManager = NSFileManager()
            let directoryURL: NSURL = NSURL(fileURLWithPath: directories[i], isDirectory: true)
            
            do {
                if let URLs: [NSURL] = try fileManager.contentsOfDirectoryAtURL(directoryURL, includingPropertiesForKeys: [NSURLIsDirectoryKey], options: NSDirectoryEnumerationOptions()) {
                    var urlIsaDirectory: ObjCBool = ObjCBool(false)
                    for url in URLs {
                        if fileManager.fileExistsAtPath(url.path!, isDirectory: &urlIsaDirectory) {
                            if !urlIsaDirectory {
                                soundFiles[i].files.append("\(url.lastPathComponent!)")
                            }
                        }
                    }
                }
            } catch {
                debugPrint("\(error)")
            }
        }
    }

    override public func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override public func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return soundFiles[section].files.count
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return "Ringtones"
    }
    
    func tableView(tableView: UITableView, estimatedHeightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 44
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
    override public func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
    
        let fileName: String = soundFiles[indexPath.section].files[indexPath.row]
        let cell: AACommonCell = tableView.dequeueCell(indexPath)
        cell.style = .Normal
        let name = fileName.componentsSeparatedByString(".m4r")
        cell.textLabel?.text = name.first
        return cell
    }
    
    func tableView(tableView: UITableView, didDeselectRowAtIndexPath indexPath: NSIndexPath) {
        if let cell = tableView.cellForRowAtIndexPath(indexPath) as? AACommonCell {
            cell.style = .Normal
        }
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {

        let directory: String = soundFiles[indexPath.section].directory
        let fileName: String = soundFiles[indexPath.section].files[indexPath.row]
        let fileURL: NSURL = NSURL(fileURLWithPath: "\(directory)/\(fileName)")
        do {
            audioPlayer = try AVAudioPlayer(contentsOfURL: fileURL)
            audioPlayer.play()
        } catch {
            debugPrint("\(error)")
        }
        let cell = tableView.cellForRowAtIndexPath(indexPath) as! AACommonCell
        selectedRingtone = soundFiles[indexPath.section].files[indexPath.row]
        cell.style = .Checkmark
    }
    
//    func dismissAndSave() {
//        delegate?.ringtonesController(self, currentRingtone:selectedRingtone)
//        dismiss()
//    }
}
