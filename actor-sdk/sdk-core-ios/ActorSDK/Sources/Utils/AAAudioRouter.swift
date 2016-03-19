//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

public enum Route {
    case Speaker
    case Receiver
}

public class AAAudioRouter {
    
    private var isBatchedUpdate = false
    private var isInvalidated = false
    
    public var isEnabled = false {
        willSet(v) {
            if isEnabled != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    public var isRTCEnabled = false {
        willSet(v) {
            if isRTCEnabled != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    public var currentRoute = Route.Receiver {
        willSet(v) {
            if currentRoute != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    public var mode = AVAudioSessionModeDefault {
        willSet(v) {
            if mode != v {
                isInvalidated = true
            }
        }
        didSet(v) {
            onChanged()
        }
    }
    
    public var category = AVAudioSessionCategorySoloAmbient {
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
        NSNotificationCenter.defaultCenter().addObserverForName(AVAudioSessionRouteChangeNotification,
            object: nil, queue: NSOperationQueue.mainQueue()) { (note) -> Void in
            let notification: NSNotification = note as NSNotification
            if let info = notification.userInfo {
                let numberReason: NSNumber = info[AVAudioSessionRouteChangeReasonKey] as! NSNumber
                if let reason = AVAudioSessionRouteChangeReason(rawValue: UInt(numberReason.integerValue)) {
                    self.routeChanged(reason)
                }
            }
        }
    }
    
    func batchedUpdate(@noescape closure: ()->()) {
        isInvalidated = false
        isBatchedUpdate = true
        closure()
        isBatchedUpdate = false
        if isInvalidated {
            isInvalidated = false
            fixSession()
        }
    }
    
    private func onChanged() {
        if !isBatchedUpdate && isInvalidated {
            isInvalidated = false
            fixSession()
        }
    }
    
    private func fixSession() {
        
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
                        if (self.currentRoute == .Receiver && portDescription.portType != AVAudioSessionPortBuiltInReceiver) {
                            try session.overrideOutputAudioPort(.None)
                        } else if (self.currentRoute == .Speaker && portDescription.portType != AVAudioSessionPortBuiltInSpeaker) {
                            try session.overrideOutputAudioPort(AVAudioSessionPortOverride.Speaker)
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
                        if (self.currentRoute == .Receiver && portDescription.portType != AVAudioSessionPortBuiltInReceiver) {
                            try session.overrideOutputAudioPort(.None)
                        } else if (self.currentRoute == .Speaker && portDescription.portType != AVAudioSessionPortBuiltInSpeaker) {
                            try session.overrideOutputAudioPort(AVAudioSessionPortOverride.Speaker)
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
    
    private func isHeadsetPluggedIn() -> Bool {
        let route: AVAudioSessionRouteDescription = AVAudioSession.sharedInstance().currentRoute
        for port in route.outputs {
            let portDescription: AVAudioSessionPortDescription = port as AVAudioSessionPortDescription
            if portDescription.portType == AVAudioSessionPortHeadphones || portDescription.portType == AVAudioSessionPortHeadsetMic {
                return true
            }
        }
        return false
    }
    
    private func routeChanged(reason: AVAudioSessionRouteChangeReason) {
        if reason == .NewDeviceAvailable {
            if isHeadsetPluggedIn() {
                self.currentRoute = .Receiver
                return
            }
        } else if reason == .OldDeviceUnavailable {
            if !isHeadsetPluggedIn() {
                self.currentRoute = .Receiver
                return
            }
        }
        
        if reason == .Override || reason == .RouteConfigurationChange {
            fixSession()
        }
    }
}