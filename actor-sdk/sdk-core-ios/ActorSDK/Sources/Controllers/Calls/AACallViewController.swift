//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

public class AACallViewController: AAViewController, RTCEAGLVideoViewDelegate {
    
    public let binder = AABinder()
    public let callId: jlong
    public let call: ACCallVM
    public let senderAvatar: AAAvatarView = AAAvatarView()
    public let peerTitle = UILabel()
    public let callState = UILabel()
    
    var remoteView = RTCEAGLVideoView()
    var remoteVideoSize: CGSize!
    var localView = RTCEAGLVideoView()
    var localVideoSize: CGSize!
    
    var localVideoTrack: RTCVideoTrack!
    var remoteVideoTrack: RTCVideoTrack!
    
    public let answerCallButton = UIButton()
    public let answerCallButtonText = UILabel()
    public let declineCallButton = UIButton()
    public let declineCallButtonText = UILabel()
    
    public let muteButton = AACircleButton(size: 72)
    // public let videoButton = AACircleButton(size: 72)
    
    var isScheduledDispose = false
    var timer: NSTimer?
    
    public init(callId: jlong) {
        self.callId = callId
        self.call = ActorSDK.sharedActor().messenger.getCallWithCallId(callId)
        
        super.init()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(rgb: 0x2a4463)
        
        //
        // Buttons
        //
        
        answerCallButton.setImage(UIImage.bundled("ic_call_answer_44")!.tintImage(UIColor.whiteColor()), forState: .Normal)
        answerCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 61/255.0, green: 217/255.0, blue: 90/255.0, alpha: 1.0), size: CGSizeMake(74, 74), radius: 37), forState: .Normal)
        answerCallButton.viewDidTap = {
            Actor.answerCallWithCallId(self.callId)
        }
        answerCallButtonText.font = UIFont.thinSystemFontOfSize(16)
        answerCallButtonText.textColor = UIColor.whiteColor()
        answerCallButtonText.textAlignment = NSTextAlignment.Center
        answerCallButtonText.text = AALocalized("CallsAnswer")
        answerCallButtonText.bounds = CGRectMake(0, 0, 72, 44)
        answerCallButtonText.numberOfLines = 2
        
        declineCallButton.setImage(UIImage.bundled("ic_call_end_44")!.tintImage(UIColor.whiteColor()), forState: .Normal)
        declineCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 217/255.0, green: 80/255.0, blue:61/255.0, alpha: 1.0), size: CGSizeMake(74, 74), radius: 37), forState: .Normal)
        declineCallButton.viewDidTap = {
            Actor.endCallWithCallId(self.callId)
        }
        declineCallButtonText.font = UIFont.thinSystemFontOfSize(16)
        declineCallButtonText.textColor = UIColor.whiteColor()
        declineCallButtonText.textAlignment = NSTextAlignment.Center
        declineCallButtonText.text = AALocalized("CallsDecline")
        declineCallButtonText.bounds = CGRectMake(0, 0, 72, 44)
        declineCallButtonText.numberOfLines = 2
        
        
        //
        // Call Control
        //
        
        muteButton.image = UIImage.bundled("ic_mic_off_44")
        muteButton.title = AALocalized("CallsMute")
        muteButton.alpha = 0
        muteButton.button.viewDidTap = {
            Actor.toggleCallMuteWithCallId(self.callId)
        }
        
        // videoButton.image = UIImage.bundled("ic_video_44")
        // videoButton.title = AALocalized("CallsVideo")
        // videoButton.alpha = 0
        
        // videoButton.button.viewDidTap = {
        //    Actor.toggleVideoEnabledWithCallId(self.callId)
        // }
        
        
        //
        // Video ViewPorts
        //
        
        localView.backgroundColor = UIColor.whiteColor()
        localView.alpha = 0
        localView.layer.cornerRadius = 15
        localView.layer.borderWidth = 1
        localView.layer.borderColor = UIColor.grayColor().CGColor
        localView.layer.shadowRadius = 1
        localView.clipsToBounds = true
        localView.contentMode = .ScaleAspectFit
        
        remoteView.alpha = 0
        remoteView.backgroundColor = UIColor.blackColor()
        remoteView.delegate = self
        remoteView.contentMode = .ScaleAspectFit
        
        
        //
        // Peer Info
        //
        
        peerTitle.textColor = UIColor.whiteColor().alpha(0.87)
        peerTitle.textAlignment = NSTextAlignment.Center
        peerTitle.font = UIFont.thinSystemFontOfSize(42)
        peerTitle.minimumScaleFactor = 0.3
        
        callState.textColor = UIColor.whiteColor()
        callState.textAlignment = NSTextAlignment.Center
        callState.font = UIFont.systemFontOfSize(19)
        
        
        self.view.addSubview(senderAvatar)
        self.view.addSubview(peerTitle)
        self.view.addSubview(remoteView)
        self.view.addSubview(callState)
        self.view.addSubview(answerCallButton)
        self.view.addSubview(answerCallButtonText)
        self.view.addSubview(declineCallButton)
        self.view.addSubview(declineCallButtonText)
        self.view.addSubview(muteButton)
        // self.view.addSubview(videoButton)
        self.view.addSubview(localView)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        senderAvatar.frame = CGRectMake((self.view.width - 104) / 2, 60, 108, 108)
        peerTitle.frame = CGRectMake(22, senderAvatar.bottom + 22, view.width - 44, 42)
        callState.frame = CGRectMake(0, peerTitle.bottom + 8, view.width, 22)
        
        layoutButtons()
        layoutVideo()
    }
    
    public func videoView(videoView: RTCEAGLVideoView!, didChangeVideoSize size: CGSize) {
        if videoView == remoteView {
            self.remoteVideoSize = size
        } else if videoView == localView {
            self.localVideoSize = size
        }
        
        layoutVideo()
    }
    
    private func layoutButtons() {
        
        muteButton.frame = CGRectMake((self.view.width / 3 - 84) / 2, self.view.height - 72 - 49, 84, 72 + 5 + 44)
//        videoButton.frame = CGRectMake(2 * self.view.width / 3 +  (self.view.width / 3 - 84) / 2, self.view.height - 72 - 49, 84, 72 + 5 + 44)
//        if call.isVideoPreferred.boolValue {
//            videoButton.hidden = true
//        } else {
//            videoButton.hidden = false
//        }
        
        if !declineCallButton.hidden || !answerCallButton.hidden {
            if !declineCallButton.hidden && !answerCallButton.hidden {
                declineCallButton.frame = CGRectMake(25, self.view.height - 72 - 49, 72, 72)
                declineCallButtonText.under(declineCallButton.frame, offset: 5)
                answerCallButton.frame = CGRectMake(self.view.width - 72 - 25, self.view.height - 72 - 49, 72, 72)
                answerCallButtonText.under(answerCallButton.frame, offset: 5)
            } else {
                if !answerCallButton.hidden {
                    answerCallButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 72 - 49, 72, 72)
                    answerCallButtonText.under(answerCallButton.frame, offset: 5)
                }
                if !declineCallButton.hidden {
                    // declineCallButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 72 - 49, 72, 72)
                    declineCallButton.frame = CGRectMake(self.view.width - 72 - 25, self.view.height - 72 - 49, 72, 72)
                    declineCallButtonText.under(declineCallButton.frame, offset: 5)
                }
            }
        }
    }
    
    private func layoutVideo() {
        if self.remoteVideoSize == nil {
            remoteView.frame = CGRectMake(0, 0, self.view.width, self.view.height)
        } else {
            remoteView.frame = AVMakeRectWithAspectRatioInsideRect(remoteVideoSize, view.bounds)
        }
        
        if self.localVideoSize == nil {
            localView.frame = CGRectMake(self.view.width - 100 - 10, 10, 100, 120)
        } else {
            let rect = AVMakeRectWithAspectRatioInsideRect(localVideoSize, CGRectMake(0, 0, 120, 120))
            localView.frame = CGRectMake(self.view.width - rect.width - 10, 10, rect.width, rect.height)
        }
    }
    
    private func CGSizeAspectFit(aspectRatio:CGSize, boundingSize:CGSize) -> CGSize {
        var aspectFitSize = boundingSize
        let mW = boundingSize.width / aspectRatio.width
        let mH = boundingSize.height / aspectRatio.height
        if( mH < mW )
        {
            aspectFitSize.width = mH * aspectRatio.width
        }
        else if( mW < mH )
        {
            aspectFitSize.height = mW * aspectRatio.height
        }
        return aspectFitSize
    }
    
    private func CGSizeAspectFill(aspectRatio:CGSize, minimumSize:CGSize) -> CGSize {
        var aspectFillSize = minimumSize
        let mW = minimumSize.width / aspectRatio.width
        let mH = minimumSize.height / aspectRatio.height
        if( mH > mW )
        {
            aspectFillSize.width = mH * aspectRatio.width
        }
        else if( mW > mH )
        {
            aspectFillSize.height = mW * aspectRatio.height
        }
        return aspectFillSize
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // UI Configuration
        
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
        
        UIDevice.currentDevice().proximityMonitoringEnabled = true
        
        //
        // Binding State
        //
        
        binder.bind(call.isAudioEnabled) { (value: JavaLangBoolean!) -> () in
            self.muteButton.filled = !value.booleanValue()
        }
        
        binder.bind(call.state) { (value: ACCallState!) -> () in
            if (ACCallState_Enum.RINGING == value.toNSEnum()) {
                if (self.call.isOutgoing) {
                    
                    self.muteButton.showViewAnimated()
                    // self.videoButton.showViewAnimated()
                    
                    self.answerCallButton.hidden = true
                    self.answerCallButtonText.hidden = true
                    self.declineCallButton.hidden = false
                    self.declineCallButtonText.hidden = true
                    
                    self.callState.text = AALocalized("CallStateRinging")
                } else {
                    self.answerCallButton.hidden = false
                    self.answerCallButtonText.hidden = false
                    self.declineCallButton.hidden = false
                    self.declineCallButtonText.hidden = false
                    self.callState.text = AALocalized("CallStateIncoming")
                }
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.CONNECTING == value.toNSEnum()) {
                
                self.muteButton.showViewAnimated()
                // self.videoButton.showViewAnimated()
                
                self.answerCallButton.hidden = true
                self.answerCallButtonText.hidden = true
                self.declineCallButton.hidden = false
                self.declineCallButtonText.hidden = true
                
                self.callState.text = AALocalized("CallStateConnecting")
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.IN_PROGRESS == value.toNSEnum()) {
                
                self.muteButton.showViewAnimated()
                // self.videoButton.showViewAnimated()
                
                self.answerCallButton.hidden = true
                self.answerCallButtonText.hidden = true
                self.declineCallButton.hidden = false
                self.declineCallButtonText.hidden = true
                
                self.startTimer()
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.ENDED == value.toNSEnum()) {
                
                self.muteButton.hideViewAnimated()
                // self.videoButton.hideViewAnimated()
                
                self.answerCallButton.hidden = true
                self.answerCallButtonText.hidden = true
                self.declineCallButton.hidden = true
                self.declineCallButtonText.hidden = true
                
                self.stopTimer()
                
                self.layoutButtons()
                
                if (!self.isScheduledDispose) {
                    self.isScheduledDispose = true
                    dispatchAfterOnUi(0.8) {
                        self.dismiss()
                    }
                }
            } else {
                fatalError("Unknown Call State!")
            }
        }
        
        
        //
        // Binding Title
        //
        
        if (call.peer.peerType.toNSEnum() == ACPeerType_Enum.PRIVATE) {
            let user = Actor.getUserWithUid(call.peer.peerId)
            binder.bind(user.getNameModel(), closure: { (value: String!) -> () in
                self.peerTitle.text = value
            })
            binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar!) -> () in
                self.senderAvatar.bind(user.getNameModel().get(), id: Int(user.getId()), avatar: value)
            })
        } else if (call.peer.peerType.toNSEnum() == ACPeerType_Enum.GROUP) {
            let group = Actor.getGroupWithGid(call.peer.peerId)
            binder.bind(group.getNameModel(), closure: { (value: String!) -> () in
                self.peerTitle.text = value
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar!) -> () in
                self.senderAvatar.bind(group.getNameModel().get(), id: Int(group.getId()), avatar: value)
            })
        }
        
        
        //
        // Binding Video
        //
        
        // Calls Supported only in Private Chats
        if call.peer.isPrivate {
            
            // Bind Video Button
//            binder.bind(call.isVideoEnabled) { (value: JavaLangBoolean!) -> () in
//                self.videoButton.filled = value.booleanValue()
//            }
            
            // Local Video can be only one, so we can just keep active track reference and handle changes
            binder.bind(call.ownVideoTracks, closure: { (videoTracks: ACArrayListMediaTrack!) in
                var needUnbind = true
                if videoTracks.size() > 0 {
                    
                   let track = (videoTracks.getWithInt(0) as! CocoaVideoTrack).videoTrack
                    if self.localVideoTrack != track {
                        if self.localVideoTrack != nil {
                            self.localVideoTrack.removeRenderer(self.localView)
                        }
                        self.localVideoTrack = track
                        self.localView.showViewAnimated()
                        track.addRenderer(self.localView)
                    }
                    needUnbind = false
                }
                if needUnbind {
                    if self.localVideoTrack != nil {
                        self.localVideoTrack.removeRenderer(self.localView)
                        self.localVideoTrack = nil
                    }
                    self.localView.hideViewAnimated()
                }
            })

            // In Private Calls we can have only one video stream from other side
            // We will assume only one active peer connection
            binder.bind(call.theirVideoTracks, closure: { (videoTracks: ACArrayListMediaTrack!) in
                var needUnbind = true
                if videoTracks.size() > 0 {
                    
                    let track = (videoTracks.getWithInt(0) as! CocoaVideoTrack).videoTrack
                    if self.remoteVideoTrack != track {
                        if self.remoteVideoTrack != nil {
                            self.remoteVideoTrack.removeRenderer(self.remoteView)
                        }
                        self.remoteVideoTrack = track
                        self.remoteView.showViewAnimated()
                        self.senderAvatar.hideViewAnimated()
                        self.peerTitle.hideViewAnimated()
                        track.addRenderer(self.remoteView)
                    }
                    needUnbind = false
                }
                if needUnbind {
                    if self.remoteVideoTrack != nil {
                        self.remoteVideoTrack.removeRenderer(self.remoteView)
                        self.remoteVideoTrack = nil
                    }
                    self.remoteView.hideViewAnimated()
                    self.senderAvatar.showViewAnimated()
                    self.peerTitle.showViewAnimated()
                }
            })

        } else {
            // self.videoButton.filled = false
            // self.videoButton.enabled = false
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIDevice.currentDevice().proximityMonitoringEnabled = false
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
        
        binder.unbindAll()
    }
    
    
    //
    // Timer
    //
    
    func startTimer() {
        timer?.invalidate()
        timer = NSTimer.scheduledTimerWithTimeInterval(1.0, target: self, selector: #selector(AACallViewController.updateTimer), userInfo: nil, repeats: true)
        updateTimer()
    }
    
    func updateTimer() {
        if call.callStart > 0 {
            let end = call.callEnd > 0 ? call.callEnd : jlong(NSDate().timeIntervalSince1970 * 1000)
            let secs = Int((end - call.callStart) / 1000)
            
            let seconds = secs % 60
            let minutes = secs / 60
            
            self.callState.text = NSString(format: "%0.2d:%0.2d", minutes, seconds) as String
        } else {
            self.callState.text = "0:00"
        }
    }
    
    func stopTimer() {
        timer?.invalidate()
        timer = nil
        updateTimer()
    }
}