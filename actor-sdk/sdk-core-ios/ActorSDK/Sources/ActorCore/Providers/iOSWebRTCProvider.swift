//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import RTCPeerConnection

class iOSWebRTCProvider: NSObject, ACWebRTCProvider {
    
    private var controller: ACWebRTCController!!
    private var messenger: ACMessenger!!

    func initWithMessenger(messenger: ACMessenger, withController controller: ACWebRTCController) {
        self.controller = controller
        self.messenger = messenger
    }
    
    func onIncomingCallWithCallId(callId: jlong) {
        
    }
    
    func onOutgoingCallWithCallId(callId: jlong) {
        
    }
    
    func onOfferNeededWithCallId(callId: jlong) {
        
    }
    
    func onAnswerReceivedWithCallId(callId: jlong, withSDP offerSDP: String) {
        
    }
    
    func onOfferReceivedWithCallId(callId: jlong, withSDP offerSDP: String) {
        
    }
    
    func onCandidateWithCallId(callId: jlong, withId id_: String, withLabel label: jint, withSDP sdp: String) {
        
    }
    
    func onCallEndWithCallId(callId: jlong) {
        
    }
}