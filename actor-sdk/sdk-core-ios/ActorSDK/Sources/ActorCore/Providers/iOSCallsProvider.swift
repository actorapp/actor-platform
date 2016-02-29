//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

class iOSCallsProvider: NSObject, ACCallsProvider {
    
    var beepPlayer: AVAudioPlayer! = nil
    var ringtonePlayer: AVAudioPlayer! = nil
    var latestNotification: UILocalNotification!
    
    func onCallStartWithCallId(callId: jlong) {
        
        AAAudioManager.sharedAudio().callStart(Actor.getCallWithCallId(callId).isOutgoing)
       
        if !Actor.getCallWithCallId(callId).isOutgoing {
            showNotification(callId)
        }
        
        dispatchOnUi() {
            let rootController = ActorSDK.sharedActor().bindedToWindow.rootViewController!
            rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
        }
    }
    
    func onCallAnsweredWithCallId(callId: jlong) {
        AAAudioManager.sharedAudio().callAnswered()
        hideNotification()
    }
    
    func onCallEndWithCallId(callId: jlong) {
        AAAudioManager.sharedAudio().callEnd()
        hideNotification()
    }
    
    private func showNotification(callId: jlong) {
        dispatchOnUi() {
            let callVm = Actor.getCallWithCallId(callId)
            if (self.latestNotification != nil) {
                UIApplication.sharedApplication().cancelLocalNotification(self.latestNotification)
                self.latestNotification = nil
            }
            self.latestNotification = UILocalNotification()
            if callVm.peer.isGroup {
                let groupName = Actor.getGroupWithGid(callVm.peer.peerId).getNameModel().get()
                self.latestNotification.alertBody = "Group Call \(groupName)"
            } else if callVm.peer.isPrivate {
                let userName = Actor.getUserWithUid(callVm.peer.peerId).getNameModel().get()
                self.latestNotification.alertBody = "Call from \(userName)"
            }
            self.latestNotification.soundName = "ringtone.m4a"
            UIApplication.sharedApplication().presentLocalNotificationNow(self.latestNotification)
        }
    }
    
    private func hideNotification() {
        dispatchOnUi() {
            if (self.latestNotification != nil) {
                UIApplication.sharedApplication().cancelLocalNotification(self.latestNotification)
                self.latestNotification = nil
            }
        }
    }
    
    func startOutgoingBeep() {
        
        if (beepPlayer == nil) {
            do {
                beepPlayer = try AVAudioPlayer(contentsOfURL: NSURL(fileURLWithPath: NSBundle.framework.pathForResource("tone", ofType: "m4a")!))
                beepPlayer.prepareToPlay()
                beepPlayer.numberOfLoops = -1
            } catch let error as NSError {
                print("Error: \(error.description)")
            }
        }

        beepPlayer.play()
    }
    
    func stopOutgoingBeep() {
        beepPlayer.stop()
    }
}