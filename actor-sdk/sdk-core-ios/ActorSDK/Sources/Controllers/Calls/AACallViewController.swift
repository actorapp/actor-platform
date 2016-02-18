//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import PureLayout

public class AACallViewController: AAViewController {
    
    public let binder = AABinder()
    public let callId: jlong
    public let call: ACCallVM
    public let senderAvatar: AAAvatarView = AAAvatarView(frameSize: 120, type: .Rounded)
    public let peerTitle = UILabel()
    public let callState = UILabel()
    public let answerCallButton = UIButton()
    public let declineCallButton = UIButton()
    
    public let muteButton = UIButton()
    public let speakerButton = UIButton()
    public let videoButton = UIButton()
    
    public let debugLabel = UILabel()
    
    var isScheduledDispose = false
    
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
        
        //
        // Buttons
        //
        answerCallButton.setImage(UIImage.bundled("ic_call_36pt")!.tintImage(UIColor.whiteColor()), forState: .Normal)
        answerCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(rgb: 0x54dd64), size: CGSizeMake(72, 72), radius: 36), forState: .Normal)
        answerCallButton.viewDidTap = {
            Actor.answerCallWithCallId(self.callId)
        }
        
        declineCallButton.setImage(UIImage.bundled("ic_call_end_36pt")!.tintImage(UIColor.whiteColor()), forState: .Normal)
        declineCallButton.setBackgroundImage(Imaging.roundedImage(UIColor(rgb: 0xfc2c31), size: CGSizeMake(72, 72), radius: 36), forState: .Normal)
        declineCallButton.viewDidTap = {
            Actor.endCallWithCallId(self.callId)
        }
        
        muteButton.viewDidTap = {
            Actor.toggleCallMuteWithCallId(self.callId)
        }
        
        speakerButton.setImage(UIImage.bundled("ic_call_36pt")!.tintImage(ActorSDK.sharedActor().style.vcTintColor), forState: .Normal)
        speakerButton.setImage(UIImage.bundled("ic_call_36pt")!.tintImage(ActorSDK.sharedActor().style.vcHintColor), forState: .Disabled)
        
        videoButton.setImage(UIImage.bundled("ic_call_36pt")!.tintImage(ActorSDK.sharedActor().style.vcTintColor), forState: .Normal)
        videoButton.setImage(UIImage.bundled("ic_call_36pt")!.tintImage(ActorSDK.sharedActor().style.vcHintColor), forState: .Disabled)
        videoButton.enabled = false
        
        //
        // Peer Info
        //
        
        peerTitle.textColor = ActorSDK.sharedActor().style.vcTextColor
        peerTitle.textAlignment = NSTextAlignment.Center
        peerTitle.font = UIFont.thinSystemFontOfSize(32)
        
        callState.textColor = ActorSDK.sharedActor().style.vcHintColor
        callState.textAlignment = NSTextAlignment.Center
        callState.font = UIFont.thinSystemFontOfSize(32)
        
        debugLabel.textColor = ActorSDK.sharedActor().style.vcTextColor
        debugLabel.lineBreakMode = .ByWordWrapping
        debugLabel.numberOfLines = 0
        
        self.view.backgroundColor = UIColor.whiteColor()
        
        self.view.addSubview(senderAvatar)
        self.view.addSubview(peerTitle)
        self.view.addSubview(callState)
        self.view.addSubview(answerCallButton)
        self.view.addSubview(declineCallButton)
        self.view.addSubview(muteButton)
        self.view.addSubview(speakerButton)
        self.view.addSubview(videoButton)
        self.view.addSubview(debugLabel)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        senderAvatar.frame = CGRectMake((self.view.width - 90) / 2, 100, 90, 90)
        peerTitle.frame = CGRectMake(60, senderAvatar.bottom + 20, view.width - 120, 34)
        callState.frame = CGRectMake(60, peerTitle.bottom + 20, view.width - 120, 34)
        debugLabel.frame = view.bounds
        
        layoutButtons()
    }
    
    private func layoutButtons() {
        muteButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 226, 72, 72)
//        muteButton.frame = CGRectMake((self.view.width / 3 - 72) / 2, self.view.height - 226, 72, 72)
//        speakerButton.frame = CGRectMake( self.view.width / 3 +  (self.view.width / 3 - 72) / 2, self.view.height - 226, 72, 72)
//        videoButton.frame = CGRectMake( 2 * self.view.width / 3 +  (self.view.width / 3 - 72) / 2, self.view.height - 226, 72, 72)
        
        if !declineCallButton.hidden || !answerCallButton.hidden {
            if !declineCallButton.hidden && !answerCallButton.hidden {
                declineCallButton.frame = CGRectMake((self.view.width / 2 - 72) / 2, self.view.height - 96, 72, 72)
                answerCallButton.frame = CGRectMake( (self.view.width / 2) + (self.view.width / 2 - 72) / 2, self.view.height - 96, 72, 72)
            } else {
                if !answerCallButton.hidden {
                    answerCallButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 96, 72, 72)
                }
                if !declineCallButton.hidden {
                    declineCallButton.frame = CGRectMake((self.view.width - 72) / 2, self.view.height - 96, 72, 72)
                }
            }
        }
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        //
        // Binding State
        //
        binder.bind(call.state) { (value: ACCallState!) -> () in
            if (ACCallState_Enum.CALLING_INCOMING == value.toNSEnum()) {
                self.answerCallButton.hidden = false
                self.declineCallButton.hidden = false
                self.callState.text = "Incoming call..."
                self.layoutButtons()
            } else if (ACCallState_Enum.CONNECTING == value.toNSEnum()) {
                self.answerCallButton.hidden = true
                self.declineCallButton.hidden = false
                self.callState.text = "Connecting"
                self.layoutButtons()
            } else if (ACCallState_Enum.IN_PROGRESS == value.toNSEnum()) {
                self.answerCallButton.hidden = true
                self.declineCallButton.hidden = false
                self.callState.text = "0:00"
                self.layoutButtons()
            } else if (ACCallState_Enum.CALLING_OUTGOING == value.toNSEnum()){
                self.answerCallButton.hidden = true
                self.declineCallButton.hidden = false
                self.callState.text = "Ringing..."
                self.layoutButtons()
            } else if (ACCallState_Enum.ENDED == value.toNSEnum()) {
                self.callState.text = "Call Ended"
                self.answerCallButton.hidden = true
                self.declineCallButton.hidden = true
                self.layoutButtons()
                if (!self.isScheduledDispose) {
                    self.isScheduledDispose = true
                    dispatchAfterOnUi(0.8) {
                        self.dismiss()
                    }
                }
            } else {
                self.answerCallButton.hidden = false
                self.declineCallButton.hidden = false
                self.callState.text = ""
                self.layoutButtons()
            }
        }
        
        binder.bind(call.isMuted) { (value: JavaLangBoolean!) -> () in
            if (value.booleanValue()) {
                self.muteButton.setImage(UIImage.bundled("ic_mic_off_36pt")!.tintImage(UIColor.whiteColor()), forState: .Normal)
                self.muteButton.setBackgroundImage(Imaging.roundedImage(ActorSDK.sharedActor().style.vcTintColor, radius: 36), forState: .Normal)
            } else {
                self.muteButton.setImage(UIImage.bundled("ic_mic_off_36pt")!.tintImage(ActorSDK.sharedActor().style.vcTintColor), forState: .Normal)
                self.muteButton.setBackgroundImage(Imaging.roundedImage(UIColor.whiteColor(), radius: 36), forState: .Normal)
            }
        }
        
        binder.bind(call.members) { (value: JavaUtilArrayList!) -> () in
            var debugStr = ""
            for i in 0..<value.size() {
                let member = value.getWithInt(i) as! ACCallMember
                debugStr += "\(member.uid)"
                switch(member.state.toNSEnum()) {
                case ACCallMemberState_Enum.ENDED:
                    debugStr += " - Ended"
                    break
                case ACCallMemberState_Enum.CALLING:
                    debugStr += " - Calling"
                    break
                case ACCallMemberState_Enum.IN_PROGRESS:
                    debugStr += " - In Progress"
                    break
                case ACCallMemberState_Enum.CALLING_REACHED:
                    debugStr += " - Reached"
                    break
                default:
                    debugStr += " - Unknown"
                    break
                }
                debugStr += "\n"
            }
            self.debugLabel.text = debugStr
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
                self.senderAvatar.bind(user.getNameModel().get(), id: user.getId(), avatar: value)
            })
        } else if (call.peer.peerType.toNSEnum() == ACPeerType_Enum.GROUP) {
            let group = Actor.getGroupWithGid(call.peer.peerId)
            binder.bind(group.getNameModel(), closure: { (value: String!) -> () in
                self.peerTitle.text = value
            })
            binder.bind(group.getAvatarModel(), closure: { (value: ACAvatar!) -> () in
                self.senderAvatar.bind(group.getNameModel().get(), id: group.getId(), avatar: value)
            })
        }
        
        UIDevice.currentDevice().proximityMonitoringEnabled = true
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        UIDevice.currentDevice().proximityMonitoringEnabled = false
        binder.unbindAll()
    }
}