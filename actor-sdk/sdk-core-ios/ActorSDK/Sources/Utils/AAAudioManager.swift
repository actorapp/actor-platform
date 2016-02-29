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
    
    private var isCalling = false
    private var isCallStarted = false
    private var ringtonePlaying = false
    private var ringtonePlayer: AVAudioPlayer! = nil
    private var audioRouter = AAAudioRouter()
    
    public override init() {
        super.init()
    }
    
    public func callStart(isOut: Bool) {
        audioRouter.category = AVAudioSessionCategoryPlayAndRecord
        if !isOut {
            audioRouter.mode = AVAudioSessionModeDefault
            audioRouter.currentRoute = .Speaker
            ringtoneStart()
        } else {
            audioRouter.mode = AVAudioSessionModeVoiceChat
            audioRouter.currentRoute = .Receiver
        }
    }
    
    public func callAnswered() {
        ringtoneEnd()
        audioRouter.mode = AVAudioSessionModeVoiceChat
        audioRouter.currentRoute = .Receiver
    }
    
    public func callEnd() {
        ringtoneEnd()
        audioRouter.category = AVAudioSessionCategorySoloAmbient
        audioRouter.mode = AVAudioSessionModeDefault
        audioRouter.currentRoute = .Receiver
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
        vibrate()
    }
    
    private func vibrate() {
        AudioServicesPlaySystemSound(1352)
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(NSEC_PER_SEC)), dispatch_get_main_queue()) { () -> Void in
            if self.ringtonePlaying {
                self.vibrate()
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




