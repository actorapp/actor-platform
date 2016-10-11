//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

open class AAAudioManager: NSObject, AVAudioPlayerDelegate {
    
    fileprivate static let sharedManager = AAAudioManager()
    
    open static func sharedAudio() -> AAAudioManager {
        return sharedManager
    }
    
    fileprivate var isRinging = false
    fileprivate var ringtonePlaying = false
    fileprivate var ringtonePlayer: AVAudioPlayer! = nil
    fileprivate var audioRouter = AAAudioRouter()
    
    fileprivate var ringtoneSound:SystemSoundID = 0
    fileprivate var isVisible = false
    
    fileprivate var isEnabled: Bool = false
    fileprivate var isVideoPreferred = false
    fileprivate var openedConnections: Int = 0
    
    public override init() {
        super.init()   
    }
    
    open func appVisible() {
        isVisible = true
    }
    
    open func appHidden() {
        isVisible = false
    }
    
    open func callStart(_ call: ACCallVM) {
        isVideoPreferred = call.isVideoPreferred
        
        if !call.isOutgoing {
            isRinging = true
            if isVisible {
                isEnabled = true
                audioRouter.batchedUpdate {
                    audioRouter.category = AVAudioSessionCategoryPlayAndRecord
                    audioRouter.mode = AVAudioSessionModeDefault
                    audioRouter.currentRoute = .speaker
                    audioRouter.isEnabled = true
                }
                ringtoneStart()
            } else {
                notificationRingtone(call)
            }
            vibrate()
        } else {
            isEnabled = true
            audioRouter.batchedUpdate {
                audioRouter.category = AVAudioSessionCategoryPlayAndRecord
                audioRouter.mode =  AVAudioSessionModeVoiceChat
                if isVideoPreferred {
                    audioRouter.currentRoute = .speaker
                } else {
                    audioRouter.currentRoute = .receiver
                }
                audioRouter.isEnabled = true
            }
        }
    }
    
    open func callAnswered(_ call: ACCallVM) {
        ringtoneEnd()
        isRinging = false
        audioRouter.batchedUpdate {
            audioRouter.mode = AVAudioSessionModeVoiceChat
            if isVideoPreferred {
                audioRouter.currentRoute = .speaker
            } else {
                audioRouter.currentRoute = .receiver
            }
        }
    }
    
    open func callEnd(_ call: ACCallVM) {
        ringtoneEnd()
        isRinging = false
        isEnabled = false
        audioRouter.batchedUpdate {
            audioRouter.category = AVAudioSessionCategorySoloAmbient
            audioRouter.mode = AVAudioSessionModeDefault
            audioRouter.currentRoute = .receiver
            audioRouter.isEnabled = false
        }
    }
    
    open func peerConnectionStarted() {
        openedConnections += 1
        print("📡 AudioManager: peerConnectionStarted \(self.openedConnections)")
        audioRouter.isRTCEnabled = openedConnections > 0
    }
    
    open func peerConnectionEnded() {
        openedConnections -= 1
        print("📡 AudioManager: peerConnectionEnded \(self.openedConnections)")
        audioRouter.isRTCEnabled = openedConnections > 0
    }
    
    fileprivate func ringtoneStart() {
        if ringtonePlaying {
            return
        }
        
        ringtonePlaying = true
        
        do {
            self.ringtonePlayer = try AVAudioPlayer(contentsOf: URL(fileURLWithPath: Bundle.framework.path(forResource: "ringtone", ofType: "m4a")!))
            self.ringtonePlayer.delegate = self
            self.ringtonePlayer.numberOfLoops = -1
            self.ringtonePlayer.volume = 1.0
            self.ringtonePlayer.play()
        } catch let error as NSError {
            print("Unable to start Ringtone: \(error.description)")
            self.ringtonePlayer = nil
        }
    }
    
    fileprivate func vibrate() {
        
        if #available(iOS 9.0, *) {
            AudioServicesPlayAlertSoundWithCompletion(1352) {
                DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(Int64(NSEC_PER_SEC)) / Double(NSEC_PER_SEC)) { () -> Void in
                    if self.isRinging {
                        self.vibrate()
                    }
                }
            }
        } else {
            AudioServicesPlayAlertSound(1352)
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(Int64(NSEC_PER_SEC)) / Double(NSEC_PER_SEC)) { () -> Void in
                if self.isRinging {
                    self.vibrate()
                }
            }
        }
    }
    
    fileprivate func notificationRingtone(_ call: ACCallVM) {
        
        dispatchOnUi() {
            let notification = UILocalNotification()
            if call.peer.isGroup {
                let groupName = Actor.getGroupWithGid(call.peer.peerId).getNameModel().get()
                notification.alertBody = AALocalized("CallGroupText").replace("{name}", dest: groupName!)
                if #available(iOS 8.2, *) {
                    notification.alertTitle = AALocalized("CallGroupTitle")
                }
            } else if call.peer.isPrivate {
                let userName = Actor.getUserWithUid(call.peer.peerId).getNameModel().get()
                notification.alertBody = AALocalized("CallPrivateText").replace("{name}", dest: userName!)
                if #available(iOS 8.2, *) {
                    notification.alertTitle = AALocalized("CallPrivateTitle")
                }
            }
            notification.soundName = "ringtone.m4a"
            UIApplication.shared.presentLocalNotificationNow(notification)
        }
        
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(Int64(10 * NSEC_PER_SEC)) / Double(NSEC_PER_SEC)) { () -> Void in
            if self.isRinging {
                self.notificationRingtone(call)
            }
        }
    }
    
    fileprivate func ringtoneEnd() {
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




