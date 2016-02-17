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
        dispatchOnUi() {
            
            if ((Actor.getCallWithCallId(callId).state.get() as! ACCallState).toNSEnum() == ACCallState_Enum.CALLING_INCOMING) {
                self.startRingtone()
            } else {
                self.stopRingtone()
            }
            
            let rootController = ActorSDK.sharedActor().bindedToWindow.rootViewController!
            rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
        }
    }
    
    func onCallAnsweredWithCallId(callId: jlong) {
        dispatchOnUi() {
            self.stopRingtone()
        }
    }
    
    func onCallEndWithCallId(callId: jlong) {
        dispatchOnUi() {
            self.stopRingtone()
        }
    }
    
    private func startRingtone() {
        
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategorySoloAmbient, withOptions:
                AVAudioSessionCategoryOptions.MixWithOthers)
            try AVAudioSession.sharedInstance().setMode(AVAudioSessionModeDefault)
            try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSessionPortOverride.Speaker)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch let error as NSError {
            print("Error: \(error.description)")
        }
        
        if (self.ringtonePlayer == nil) {
            do {
                self.ringtonePlayer = try AVAudioPlayer(contentsOfURL: NSURL(fileURLWithPath: NSBundle.framework.pathForResource("ringtone", ofType: "m4a")!))
                self.ringtonePlayer.prepareToPlay()
                self.ringtonePlayer.numberOfLoops = -1
            } catch {
                
            }
        }
        
        AudioServicesPlaySystemSound(1352)
        
        self.ringtonePlayer.play()
        
        if (self.latestNotification != nil) {
            UIApplication.sharedApplication().cancelLocalNotification(self.latestNotification)
            self.latestNotification = nil
        }
        self.latestNotification = UILocalNotification()
        self.latestNotification.alertBody = "Answer it!"
        UIApplication.sharedApplication().presentLocalNotificationNow(self.latestNotification)
    }
    
    private func stopRingtone() {
        ringtonePlayer.pause()
        if (self.latestNotification != nil) {
            UIApplication.sharedApplication().cancelLocalNotification(self.latestNotification)
            self.latestNotification = nil
        }
    }
    
    func startOutgoingBeep() {
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord, withOptions:
                AVAudioSessionCategoryOptions.AllowBluetooth)
            try AVAudioSession.sharedInstance().setMode(AVAudioSessionModeVoiceChat)
            try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSessionPortOverride.None)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch let error as NSError {
            print("Error: \(error.description)")
        }
        
        if (beepPlayer == nil) {
            do {
                beepPlayer = try AVAudioPlayer(contentsOfURL: NSURL(fileURLWithPath: NSBundle.framework.pathForResource("tone", ofType: "m4a")!))
                beepPlayer.prepareToPlay()
                beepPlayer.numberOfLoops = -1
            } catch {
                
            }
        }

        beepPlayer.play()
    }
    
    func stopOutgoingBeep() {
        beepPlayer.stop()
        
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord)
            try AVAudioSession.sharedInstance().setMode(AVAudioSessionModeVoiceChat)
            try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSessionPortOverride.None)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch let error as NSError {
            print("Error: \(error.description)")
        }
    }
}