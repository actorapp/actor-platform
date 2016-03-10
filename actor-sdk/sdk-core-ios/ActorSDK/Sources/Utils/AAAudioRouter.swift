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
    
    public var isManagingEnabled = false {
        didSet(v) {
            fixSession()
        }
    }
    
    public var isEnabled = false {
        didSet(v) {
            fixSession()
        }
    }
    
    public var currentRoute = Route.Receiver {
        didSet(v) {
            fixSession()
        }
    }
    
    public var mode = AVAudioSessionModeDefault {
        didSet(v) {
            fixSession()
        }
    }
    
    public var category = AVAudioSessionCategorySoloAmbient {
        didSet(v) {
            fixSession()
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
    
    func isHeadsetPluggedIn() -> Bool {
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
    
    private func fixSession() {
        let session = AVAudioSession.sharedInstance()
        
        if isManagingEnabled {
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
                print("Audio Session: \(error.description)")
            }
            
            do {
                try session.setActive(isEnabled)
            } catch let error as NSError {
                print("Audio Session: \(error.description)")
            }
        }
    }
}