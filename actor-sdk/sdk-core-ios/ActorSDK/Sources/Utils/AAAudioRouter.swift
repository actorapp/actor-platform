//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

public enum Route {
    case speaker
    case receiver
}

open class AAAudioRouter {
    
    fileprivate var isBatchedUpdate = false
    fileprivate var isInvalidated = false
    
    open var isEnabled = false {
        willSet(v) {
            if isEnabled != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    open var isRTCEnabled = false {
        willSet(v) {
            if isRTCEnabled != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    open var currentRoute = Route.receiver {
        willSet(v) {
            if currentRoute != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    open var mode = AVAudioSessionModeDefault {
        willSet(v) {
            if mode != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    open var category = AVAudioSessionCategorySoloAmbient {
        willSet(v) {
            if category != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    public init() {
        fixSession()
        NotificationCenter.default.addObserver(forName: NSNotification.Name.AVAudioSessionRouteChange,
            object: nil, queue: OperationQueue.main) { (note) -> Void in
            let notification: Notification = note as Notification
            if let info = (notification as NSNotification).userInfo {
                let numberReason: NSNumber = info[AVAudioSessionRouteChangeReasonKey] as! NSNumber
                if let reason = AVAudioSessionRouteChangeReason(rawValue: UInt(numberReason.intValue)) {
                    self.routeChanged(reason)
                }
            }
        }
    }
    
    func batchedUpdate(_ closure: ()->()) {
        isInvalidated = false
        isBatchedUpdate = true
        closure()
        isBatchedUpdate = false
        if isInvalidated {
            isInvalidated = false
            fixSession()
        }
    }
    
    fileprivate func onChanged() {
        if !isBatchedUpdate && isInvalidated {
            isInvalidated = false
            fixSession()
        }
    }
    
    fileprivate func fixSession() {
        
        let session = AVAudioSession.sharedInstance()
        
        if isRTCEnabled {
            do {
                if session.category != AVAudioSessionCategoryPlayAndRecord {
                    try session.setCategory(category)
                }
                
                if session.mode != AVAudioSessionModeVoiceChat {
                    try AVAudioSession.sharedInstance().setMode(mode)
                }
            } catch let error as NSError {
                print("📡 Audio Session: \(error.description)")
            }
            
            do {
                if let route: AVAudioSessionRouteDescription = session.currentRoute {
                    for port in route.outputs {
                        let portDescription: AVAudioSessionPortDescription = port as AVAudioSessionPortDescription
                        if (self.currentRoute == .receiver && portDescription.portType != AVAudioSessionPortBuiltInReceiver) {
                            try session.overrideOutputAudioPort(.none)
                        } else if (self.currentRoute == .speaker && portDescription.portType != AVAudioSessionPortBuiltInSpeaker) {
                            try session.overrideOutputAudioPort(AVAudioSessionPortOverride.speaker)
                        }
                    }
                }
            } catch let error as NSError {
                print("📡 Audio Session: \(error.description)")
            }
        } else {
            do {
                if session.category != category {
                    try session.setCategory(category)
                }
                
                if session.mode != mode {
                    try AVAudioSession.sharedInstance().setMode(mode)
                }
                
                if let route: AVAudioSessionRouteDescription = session.currentRoute {
                    for port in route.outputs {
                        let portDescription: AVAudioSessionPortDescription = port as AVAudioSessionPortDescription
                        if (self.currentRoute == .receiver && portDescription.portType != AVAudioSessionPortBuiltInReceiver) {
                            try session.overrideOutputAudioPort(.none)
                        } else if (self.currentRoute == .speaker && portDescription.portType != AVAudioSessionPortBuiltInSpeaker) {
                            try session.overrideOutputAudioPort(AVAudioSessionPortOverride.speaker)
                        }
                    }
                }
            } catch let error as NSError {
                print("📡 Audio Session: \(error.description)")
            }
            
            do {
                try session.setActive(isEnabled)
            } catch let error as NSError {
                print("📡 Audio Session: \(error.description)")
            }
        }
    }
    
    fileprivate func isHeadsetPluggedIn() -> Bool {
        let route: AVAudioSessionRouteDescription = AVAudioSession.sharedInstance().currentRoute
        for port in route.outputs {
            let portDescription: AVAudioSessionPortDescription = port as AVAudioSessionPortDescription
            if portDescription.portType == AVAudioSessionPortHeadphones || portDescription.portType == AVAudioSessionPortHeadsetMic {
                return true
            }
        }
        return false
    }
    
    fileprivate func routeChanged(_ reason: AVAudioSessionRouteChangeReason) {
        if reason == .newDeviceAvailable {
            if isHeadsetPluggedIn() {
                self.currentRoute = .receiver
                return
            }
        } else if reason == .oldDeviceUnavailable {
            if !isHeadsetPluggedIn() {
                self.currentRoute = .receiver
                return
            }
        }
        
        if reason == .override || reason == .routeConfigurationChange {
            fixSession()
        }
    }
}
