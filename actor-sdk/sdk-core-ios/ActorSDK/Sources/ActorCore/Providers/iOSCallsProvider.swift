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
        
        AAAudioManager.sharedAudio().callStart(Actor.getCallWithCallId(callId))
       
        dispatchOnUi() {
            let rootController = ActorSDK.sharedActor().bindedToWindow.rootViewController!
            if let presented = rootController.presentedViewController {
                presented.dismissViewControllerAnimated(true, completion: { () -> Void in
                    rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
                })
            } else {
                rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
            }
        }
    }
    
    func onCallAnsweredWithCallId(callId: jlong) {
        AAAudioManager.sharedAudio().callAnswered(Actor.getCallWithCallId(callId))
    }
    
    func onCallEndWithCallId(callId: jlong) {
        AAAudioManager.sharedAudio().callEnd(Actor.getCallWithCallId(callId))
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
        if beepPlayer != nil {
            beepPlayer.stop()
            beepPlayer = nil
        }
    }
}