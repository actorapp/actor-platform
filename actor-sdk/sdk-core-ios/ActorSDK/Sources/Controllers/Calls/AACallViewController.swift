//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

open class AACallViewController: AAViewController, RTCEAGLVideoViewDelegate {
    
    open let binder = AABinder()
    open let callId: jlong
    open let call: ACCallVM
    open let senderAvatar: AAAvatarView = AAAvatarView()
    open let peerTitle = UILabel()
    open let callState = UILabel()
    
    var remoteView = RTCEAGLVideoView()
    var remoteVideoSize: CGSize!
    var localView = RTCEAGLVideoView()
    var localVideoSize: CGSize!
    
    var localVideoTrack: RTCVideoTrack!
    var remoteVideoTrack: RTCVideoTrack!
    
    open let answerCallButton = UIButton()
    open let answerCallButtonText = UILabel()
    open let declineCallButton = UIButton()
    open let declineCallButtonText = UILabel()
    
    open let muteButton = AACircleButton(size: 72)
    // public let videoButton = AACircleButton(size: 72)
    
    var isScheduledDispose = false
    var timer: Timer?
    
    public init(callId: jlong) {
        self.callId = callId
        self.call = ActorSDK.sharedActor().messenger.getCallWithCallId(callId)
        
        super.init()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor(rgb: 0x2a4463)
        
        //
        // Buttons
        //
        
        answerCallButton.setImage(UIImage.bundled("ic_call_answer_44")!.tintImage(UIColor.white), for: UIControlState())
        answerCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 61/255.0, green: 217/255.0, blue: 90/255.0, alpha: 1.0), size: CGSize(width: 74, height: 74), radius: 37), for: UIControlState())
        answerCallButton.viewDidTap = {
            Actor.answerCall(withCallId: self.callId)
        }
        answerCallButtonText.font = UIFont.thinSystemFontOfSize(16)
        answerCallButtonText.textColor = UIColor.white
        answerCallButtonText.textAlignment = NSTextAlignment.center
        answerCallButtonText.text = AALocalized("CallsAnswer")
        answerCallButtonText.bounds = CGRect(x: 0, y: 0, width: 72, height: 44)
        answerCallButtonText.numberOfLines = 2
        
        declineCallButton.setImage(UIImage.bundled("ic_call_end_44")!.tintImage(UIColor.white), for: UIControlState())
        declineCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(red: 217/255.0, green: 80/255.0, blue:61/255.0, alpha: 1.0), size: CGSize(width: 74, height: 74), radius: 37), for: UIControlState())
        declineCallButton.viewDidTap = {
            Actor.endCall(withCallId: self.callId)
        }
        declineCallButtonText.font = UIFont.thinSystemFontOfSize(16)
        declineCallButtonText.textColor = UIColor.white
        declineCallButtonText.textAlignment = NSTextAlignment.center
        declineCallButtonText.text = AALocalized("CallsDecline")
        declineCallButtonText.bounds = CGRect(x: 0, y: 0, width: 72, height: 44)
        declineCallButtonText.numberOfLines = 2
        
        
        //
        // Call Control
        //
        
        muteButton.image = UIImage.bundled("ic_mic_off_44")
        muteButton.title = AALocalized("CallsMute")
        muteButton.alpha = 0
        muteButton.button.viewDidTap = {
            Actor.toggleCallMute(withCallId: self.callId)
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
        
        localView.backgroundColor = UIColor.white
        localView.alpha = 0
        localView.layer.cornerRadius = 15
        localView.layer.borderWidth = 1
        localView.layer.borderColor = UIColor.gray.cgColor
        localView.layer.shadowRadius = 1
        localView.clipsToBounds = true
        localView.contentMode = .scaleAspectFit
        
        remoteView.alpha = 0
        remoteView.backgroundColor = UIColor.black
        remoteView.delegate = self
        remoteView.contentMode = .scaleAspectFit
        
        
        //
        // Peer Info
        //
        
        peerTitle.textColor = UIColor.white.alpha(0.87)
        peerTitle.textAlignment = NSTextAlignment.center
        peerTitle.font = UIFont.thinSystemFontOfSize(42)
        peerTitle.minimumScaleFactor = 0.3
        
        callState.textColor = UIColor.white
        callState.textAlignment = NSTextAlignment.center
        callState.font = UIFont.systemFont(ofSize: 19)
        
        
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
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        senderAvatar.frame = CGRect(x: (self.view.width - 104) / 2, y: 60, width: 108, height: 108)
        peerTitle.frame = CGRect(x: 22, y: senderAvatar.bottom + 22, width: view.width - 44, height: 42)
        callState.frame = CGRect(x: 0, y: peerTitle.bottom + 8, width: view.width, height: 22)
        
        layoutButtons()
        layoutVideo()
    }
    
    open func videoView(_ videoView: RTCEAGLVideoView!, didChangeVideoSize size: CGSize) {
        if videoView == remoteView {
            self.remoteVideoSize = size
        } else if videoView == localView {
            self.localVideoSize = size
        }
        
        layoutVideo()
    }
    
    fileprivate func layoutButtons() {
        
        muteButton.frame = CGRect(x: (self.view.width / 3 - 84) / 2, y: self.view.height - 72 - 49, width: 84, height: 72 + 5 + 44)
//        videoButton.frame = CGRectMake(2 * self.view.width / 3 +  (self.view.width / 3 - 84) / 2, self.view.height - 72 - 49, 84, 72 + 5 + 44)
//        if call.isVideoPreferred.boolValue {
//            videoButton.hidden = true
//        } else {
//            videoButton.hidden = false
//        }
        
        if !declineCallButton.isHidden || !answerCallButton.isHidden {
            if !declineCallButton.isHidden && !answerCallButton.isHidden {
                declineCallButton.frame = CGRect(x: 25, y: self.view.height - 72 - 49, width: 72, height: 72)
                declineCallButtonText.under(declineCallButton.frame, offset: 5)
                answerCallButton.frame = CGRect(x: self.view.width - 72 - 25, y: self.view.height - 72 - 49, width: 72, height: 72)
                answerCallButtonText.under(answerCallButton.frame, offset: 5)
            } else {
                if !answerCallButton.isHidden {
                    answerCallButton.frame = CGRect(x: (self.view.width - 72) / 2, y: self.view.height - 72 - 49, width: 72, height: 72)
                    answerCallButtonText.under(answerCallButton.frame, offset: 5)
                }
                if !declineCallButton.isHidden {
                    // declineCallButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 72 - 49, 72, 72)
                    declineCallButton.frame = CGRect(x: self.view.width - 72 - 25, y: self.view.height - 72 - 49, width: 72, height: 72)
                    declineCallButtonText.under(declineCallButton.frame, offset: 5)
                }
            }
        }
    }
    
    fileprivate func layoutVideo() {
        if self.remoteVideoSize == nil {
            remoteView.frame = CGRect(x: 0, y: 0, width: self.view.width, height: self.view.height)
        } else {
            remoteView.frame = AVMakeRect(aspectRatio: remoteVideoSize, insideRect: view.bounds)
        }
        
        if self.localVideoSize == nil {
            localView.frame = CGRect(x: self.view.width - 100 - 10, y: 10, width: 100, height: 120)
        } else {
            let rect = AVMakeRect(aspectRatio: localVideoSize, insideRect: CGRect(x: 0, y: 0, width: 120, height: 120))
            localView.frame = CGRect(x: self.view.width - rect.width - 10, y: 10, width: rect.width, height: rect.height)
        }
    }
    
    fileprivate func CGSizeAspectFit(_ aspectRatio:CGSize, boundingSize:CGSize) -> CGSize {
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
    
    fileprivate func CGSizeAspectFill(_ aspectRatio:CGSize, minimumSize:CGSize) -> CGSize {
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
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // UI Configuration
        
        UIApplication.shared.setStatusBarStyle(UIStatusBarStyle.lightContent, animated: true)
        
        UIDevice.current.isProximityMonitoringEnabled = true
        
        //
        // Binding State
        //
        
        binder.bind(call.isAudioEnabled) { (value: JavaLangBoolean?) -> () in
            self.muteButton.filled = !value!.booleanValue()
        }
        
        binder.bind(call.state) { (value: ACCallState?) -> () in
            if (ACCallState_Enum.RINGING == value!.toNSEnum()) {
                if (self.call.isOutgoing) {
                    
                    self.muteButton.showViewAnimated()
                    // self.videoButton.showViewAnimated()
                    
                    self.answerCallButton.isHidden = true
                    self.answerCallButtonText.isHidden = true
                    self.declineCallButton.isHidden = false
                    self.declineCallButtonText.isHidden = true
                    
                    self.callState.text = AALocalized("CallStateRinging")
                } else {
                    self.answerCallButton.isHidden = false
                    self.answerCallButtonText.isHidden = false
                    self.declineCallButton.isHidden = false
                    self.declineCallButtonText.isHidden = false
                    self.callState.text = AALocalized("CallStateIncoming")
                }
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.CONNECTING == value!.toNSEnum()) {
                
                self.muteButton.showViewAnimated()
                // self.videoButton.showViewAnimated()
                
                self.answerCallButton.isHidden = true
                self.answerCallButtonText.isHidden = true
                self.declineCallButton.isHidden = false
                self.declineCallButtonText.isHidden = true
                
                self.callState.text = AALocalized("CallStateConnecting")
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.IN_PROGRESS == value!.toNSEnum()) {
                
                self.muteButton.showViewAnimated()
                // self.videoButton.showViewAnimated()
                
                self.answerCallButton.isHidden = true
                self.answerCallButtonText.isHidden = true
                self.declineCallButton.isHidden = false
                self.declineCallButtonText.isHidden = true
                
                self.startTimer()
                
                self.layoutButtons()
                
            } else if (ACCallState_Enum.ENDED == value!.toNSEnum()) {
                
                self.muteButton.hideViewAnimated()
                // self.videoButton.hideViewAnimated()
                
                self.answerCallButton.isHidden = true
                self.answerCallButtonText.isHidden = true
                self.declineCallButton.isHidden = true
                self.declineCallButtonText.isHidden = true
                
                self.stopTimer()
                
                self.layoutButtons()
                
                if (!self.isScheduledDispose) {
                    self.isScheduledDispose = true
                    dispatchAfterOnUi(0.8) {
                        self.dismissController()
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
            binder.bind(user.getNameModel(), closure: { (value: String?) -> () in
                self.peerTitle.text = value
            })
            binder.bind(user.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
                self.senderAvatar.bind(user.getNameModel().get(), id: Int(user.getId()), avatar: value)
            })
        } else if (call.peer.peerType.toNSEnum() == ACPeerType_Enum.GROUP) {
            let group = Actor.getGroupWithGid(call.peer.peerId)
            binder.bind(group.getNameModel(), closure: { (value: String?) -> () in
                self.peerTitle.text = value
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar?) -> () in
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
            binder.bind(call.ownVideoTracks, closure: { (videoTracks: ACArrayListMediaTrack?) in
                var needUnbind = true
                if videoTracks!.size() > 0 {
                    
                   let track = (videoTracks!.getWith(0) as! CocoaVideoTrack).videoTrack
                    if self.localVideoTrack != track {
                        if self.localVideoTrack != nil {
                            self.localVideoTrack.remove(self.localView)
                        }
                        self.localVideoTrack = track
                        self.localView.showViewAnimated()
                        track.add(self.localView)
                    }
                    needUnbind = false
                }
                if needUnbind {
                    if self.localVideoTrack != nil {
                        self.localVideoTrack.remove(self.localView)
                        self.localVideoTrack = nil
                    }
                    self.localView.hideViewAnimated()
                }
            })

            // In Private Calls we can have only one video stream from other side
            // We will assume only one active peer connection
            binder.bind(call.theirVideoTracks, closure: { (videoTracks: ACArrayListMediaTrack?) in
                var needUnbind = true
                if videoTracks!.size() > 0 {
                    
                    let track = (videoTracks!.getWith(0) as! CocoaVideoTrack).videoTrack
                    if self.remoteVideoTrack != track {
                        if self.remoteVideoTrack != nil {
                            self.remoteVideoTrack.remove(self.remoteView)
                        }
                        self.remoteVideoTrack = track
                        self.remoteView.showViewAnimated()
                        self.senderAvatar.hideViewAnimated()
                        self.peerTitle.hideViewAnimated()
                        track.add(self.remoteView)
                    }
                    needUnbind = false
                }
                if needUnbind {
                    if self.remoteVideoTrack != nil {
                        self.remoteVideoTrack.remove(self.remoteView)
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
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIDevice.current.isProximityMonitoringEnabled = false
        UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
        
        binder.unbindAll()
    }
    
    
    //
    // Timer
    //
    
    func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(AACallViewController.updateTimer), userInfo: nil, repeats: true)
        updateTimer()
    }
    
    func updateTimer() {
        if call.callStart > 0 {
            let end = call.callEnd > 0 ? call.callEnd : jlong(Date().timeIntervalSince1970 * 1000)
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
