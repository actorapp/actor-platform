//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import PureLayout

public class AACallViewController: AAViewController {
    
    public let binder = AABinder()
    public let callId: jlong
    public let call: ACCallModel
    public let bgImage = UIImageView(image: UIImage.bundled("bg_1.jpg"))
    public let bgImageOverlay = UIView()
    public let senderAvatar: AAAvatarView = AAAvatarView(frameSize: 120, type: .Rounded)
    public let peerTitle = UILabel()
    public let answerCall = UIButton(frame: CGRectMake(0, 0, 80, 80))
    
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
        
        answerCall.backgroundColor = UIColor(rgb: 0xc04945)
        answerCall.setTitle("End Call", forState: .Normal)
        answerCall.setTitleColor(UIColor.whiteColor(), forState: .Normal)
        answerCall.viewDidTap = {
            Actor.endCallWithCallId(self.callId)
            self.navigateDetail(ConversationViewController(peer: self.call.getPeer()))            
            self.dismiss()
        }
        
        bgImage.contentMode = UIViewContentMode.ScaleAspectFill
        bgImageOverlay.opaque = false
        bgImageOverlay.backgroundColor = UIColor(rgb: 0x86aed7)
        peerTitle.textColor = UIColor.whiteColor()
        peerTitle.textAlignment = NSTextAlignment.Center
        peerTitle.font = UIFont.thinSystemFontOfSize(32)
        
        self.view.addSubview(bgImage)
        self.view.addSubview(bgImageOverlay)
        self.view.addSubview(senderAvatar)
        self.view.addSubview(peerTitle)
        self.view.addSubview(answerCall)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        bgImage.frame = self.view.bounds
        bgImageOverlay.frame = self.view.bounds
        
        senderAvatar.frame = CGRectMake((self.view.width - 120) / 2, 100, 120, 120)
        peerTitle.frame = CGRectMake(60, senderAvatar.bottom + 20, view.width - 120, 34)
        answerCall.frame = CGRectMake(0, self.view.height - 48, self.view.width, 48)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        //
        // Binding State
        //
        binder.bind(call.getState()) { (value: ACCallState!) -> () in
            if (ACCallState_Enum.CALLING_INCOMING == value.toNSEnum()) {
                print("Call \(self.callId) incoming")
            } else if (ACCallState_Enum.IN_PROGRESS == value.toNSEnum()) {
                print("Call \(self.callId) in progress")
            } else {
                print("Call \(self.callId) unknown")
            }
        }
        
        //
        // Binding Avatar
        //
        binder.bind(call.getActiveMembers()) { (value: JavaUtilArrayList!) -> () in
            
            print("Bind user")
            
            var users = [ACUserVM]()
            for i in 0..<value.size() {
                let uid = (value.getWithInt(i) as! JavaLangInteger).intValue()
                if (uid != Actor.myUid()) {
                    users.append(Actor.getUserWithUid(uid))
                }
            }
            
            print("Bind user \(users.count)")
            
            if (users.count == 1) {
                self.senderAvatar.bind(users[0].getNameModel().get(), id: users[0].getId(), avatar: users[0].getAvatarModel().get())
            } else {
                // TODO: Multiple Users
            }
        }
        
        //
        // Binding Title
        //
        if (call.getPeer().peerType.toNSEnum() == ACPeerType_Enum.PRIVATE) {
            binder.bind(Actor.getUserWithUid(call.getPeer().peerId).getNameModel(), closure: { (value: String!) -> () in
                self.peerTitle.text = value
            })
        } else if (call.getPeer().peerType.toNSEnum() == ACPeerType_Enum.GROUP) {
            binder.bind(Actor.getGroupWithGid(call.getPeer().peerId).getNameModel(), closure: { (value: String!) -> () in
                self.peerTitle.text = value
            })
        }
        
        UIApplication.sharedApplication().setStatusBarStyle(.LightContent, animated: true)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        binder.unbindAll()
        
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
}