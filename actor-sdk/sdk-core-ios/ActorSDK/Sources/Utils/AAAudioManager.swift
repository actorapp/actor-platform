//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

public class AAAudioManager: NSObject, AVAudioPlayerDelegate {
    
    private static let sharedManager = AAAudioManager()
    
    public static func sharedAudio() -> AAAudioManager {
        return sharedManager
    }
    
    private var isRinging = false
    private var ringtonePlaying = false
    private var ringtonePlayer: AVAudioPlayer! = nil
    private var audioRouter = AAAudioRouter()
    
    private var ringtoneSound:SystemSoundID = 0
    private var isVisible = false
    
    private var isEnabled: Bool = false
    private var openedConnections: Int = 0
    
    public override init() {
        super.init()
        
    }
    
    public func appVisible() {
        isVisible = true
    }
    
    public func appHidden() {
        isVisible = false
    }
    
    public func callStart(call: ACCallVM) {
        if !call.isOutgoing {
            isRinging = true
            if isVisible {
                isEnabled = true
                audioRouter.batchedUpdate {
                    audioRouter.category = AVAudioSessionCategoryPlayAndRecord
                    audioRouter.mode = AVAudioSessionModeDefault
                    audioRouter.currentRoute = .Speaker
                    audioRouter.isEnabled = isEnabled
                }
                ringtoneStart()
            } else {
                notificationRingtone(call)
            }
            vibrate()
        } else {
            isEnabled = true
            audioRouter.category = AVAudioSessionCategoryPlayAndRecord
            audioRouter.mode =  AVAudioSessionModeVoiceChat
            audioRouter.currentRoute = .Receiver
            audioRouter.isEnabled = isEnabled
        }
    }
    
    public func callAnswered(call: ACCallVM) {
        ringtoneEnd()
        isRinging = false
        audioRouter.mode = AVAudioSessionModeVoiceChat
        audioRouter.currentRoute = .Receiver
    }
    
    public func callEnd(call: ACCallVM) {
        ringtoneEnd()
        isRinging = false
        isEnabled = false
        audioRouter.category = AVAudioSessionCategorySoloAmbient
        audioRouter.mode = AVAudioSessionModeDefault
        audioRouter.currentRoute = .Receiver
        audioRouter.isEnabled = isEnabled
    }
    
    public func peerConnectionStarted() {
        openedConnections += 1
        print("📡 AudioManager: peerConnectionStarted \(self.openedConnections)")
        audioRouter.isRTCEnabled = openedConnections > 0
    }
    
    public func peerConnectionEnded() {
        openedConnections -= 1
        print("📡 AudioManager: peerConnectionEnded \(self.openedConnections)")
        audioRouter.isRTCEnabled = openedConnections > 0
    }
    
    private func ringtoneStart() {
        if ringtonePlaying {
            return
        }
        
        ringtonePlaying = true
        
        do {
            self.ringtonePlayer = try AVAudioPlayer(contentsOfURL: NSURL(fileURLWithPath: NSBundle.framework.pathForResource("ringtone", ofType: "m4a")!))
            self.ringtonePlayer.delegate = self
            self.ringtonePlayer.numberOfLoops = -1
            self.ringtonePlayer.volume = 1.0
            self.ringtonePlayer.play()
        } catch let error as NSError {
            print("Unable to start Ringtone: \(error.description)")
            self.ringtonePlayer = nil
        }
    }
    
    private func vibrate() {
        
        if #available(iOS 9.0, *) {
            AudioServicesPlayAlertSoundWithCompletion(1352) {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(NSEC_PER_SEC)), dispatch_get_main_queue()) { () -> Void in
                    if self.isRinging {
                        self.vibrate()
                    }
                }
            }
        } else {
            AudioServicesPlayAlertSound(1352)
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(NSEC_PER_SEC)), dispatch_get_main_queue()) { () -> Void in
                if self.isRinging {
                    self.vibrate()
                }
            }
        }
    }
    
    private func notificationRingtone(call: ACCallVM) {
        
        dispatchOnUi() {
            let notification = UILocalNotification()
            if call.peer.isGroup {
                let groupName = Actor.getGroupWithGid(call.peer.peerId).getNameModel().get()
                notification.alertBody = AALocalized("CallGroupText").replace("{name}", dest: groupName)
                if #available(iOS 8.2, *) {
                    notification.alertTitle = AALocalized("CallGroupTitle")
                }
            } else if call.peer.isPrivate {
                let userName = Actor.getUserWithUid(call.peer.peerId).getNameModel().get()
                notification.alertBody = AALocalized("CallPrivateText").replace("{name}", dest: userName)
                if #available(iOS 8.2, *) {
                    notification.alertTitle = AALocalized("CallPrivateTitle")
                }
            }
            notification.soundName = "ringtone.m4a"
            UIApplication.sharedApplication().presentLocalNotificationNow(notification)
        }
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(10 * NSEC_PER_SEC)), dispatch_get_main_queue()) { () -> Void in
            if self.isRinging {
                self.notificationRingtone(call)
            }
        }
    }
    
    private func ringtoneEnd() {
        if !ringtonePlaying {
            return
        }
        
        if ringtonePlayer != nil {
            ringtonePlayer.stop()
            ringtonePlayer = nil
        }
        ringtonePlaying = false
    }
}




