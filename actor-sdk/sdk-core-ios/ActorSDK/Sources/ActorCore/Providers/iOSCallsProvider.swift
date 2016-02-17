//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

class iOSCallsProvider: NSObject, ACCallsProvider {
    
    var beepPlayer: AVAudioPlayer! = nil
    
    func onCallStartWithCallId(callId: jlong) {
        dispatchOnUi() {
            let rootController = ActorSDK.sharedActor().bindedToWindow.rootViewController!
            rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
        }
    }
    
    func onCallEndWithCallId(callId: jlong) {
        dispatchOnUi() {
            
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