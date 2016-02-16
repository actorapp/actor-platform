//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class iOSCallsProvider: NSObject, ACCallsProvider {
    
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
    
//    private var controller: ACWebRTCController!
//    private var messenger: ACMessenger!
//    
//    private lazy var queue: dispatch_queue_t = dispatch_queue_create("webrtc_queue", DISPATCH_QUEUE_SERIAL)
//    private let peerConnectionFactory = RTCPeerConnectionFactory()
//    private let mediaConstraints = RTCMediaConstraints()
//    
//    private var runningCallId: jlong?
//    private var peerConnection: RTCPeerConnection!
//    private var voiceCapture: RTCAudioTrack!
//    
//    override init() {
//        RTCPeerConnectionFactory.initializeSSL()
//    }
//
//    func initWithMessenger(messenger: ACMessenger, withController controller: ACWebRTCController) {
//        self.controller = controller
//        self.messenger = messenger
//    }
//    
//    //
//    // Start/End of calls
//    //
//    
//    func onIncomingCallWithCallId(callId: jlong) {
//        dispatchSync() {
//            print("onIncomingCallWithCallId")
//            self.runningCallId = callId
//            dispatchOnUi() { () -> Void in
//                let rootController = ActorSDK.sharedActor().bindedToWindow.rootViewController!
//                rootController.presentViewController(AACallViewController(callId: callId), animated: true, completion: nil)
//            }
//        }
//    }
//    
//    func onOutgoingCallWithCallId(callId: jlong) {
//        dispatchSync() {
//            print("onOutgoingCallWithCallId")
//            self.runningCallId = callId
//        }
//    }
//    
//    func onCallEndWithCallId(callId: jlong) {
//        dispatchSync() {
//            if (self.peerConnection != nil) {
//                self.peerConnection.close()
//                self.peerConnection = nil
//            }
//            self.voiceCapture = nil
//            self.runningCallId = -1
//        }
//    }
//    
//    //
//    // In process
//    //
//    
//    private func createPeerConnection(callId: jlong) {
//        //
//        // Create Peer Connection
//        //
//        let iceServers = [
//            RTCICEServer(URI: NSURL(string: "stun:62.4.22.219:3478"), username: "", password: ""),
//            RTCICEServer(URI: NSURL(string: "turn:62.4.22.219:3478?transport=tcp"), username: "actor", password: "password"),
//            RTCICEServer(URI: NSURL(string: "turn:62.4.22.219:3478?transport=udp"), username: "actor", password: "password")
//        ]
//        peerConnection = peerConnectionFactory.peerConnectionWithICEServers(iceServers, constraints: mediaConstraints, delegate: nil)
//        
//        //
//        // Crete Media Stream
//        //
//        voiceCapture = peerConnectionFactory.audioTrackWithID("audio0")
//        let mediaStream = peerConnectionFactory.mediaStreamWithLabel("ARDAMSa0")
//        mediaStream.addAudioTrack(voiceCapture)
//        peerConnection.addStream(mediaStream)
//        
//        //
//        // Handling events from peer connection
//        //
//        peerConnection.onCandidateReceived = { (candidate) in
//            self.dispatchAsync(callId) { () -> () in
//                print("On Candidate arrived")
//                self.controller.sendCandidateWithInt(jint(candidate.sdpMLineIndex), withNSString: candidate.sdpMid, withNSString: candidate.sdp)
//            }
//        }
//        peerConnection.onStreamAdded = { (stream) in
//            self.dispatchAsync(callId) { () -> () in
//                print("On stream added")
//            }
//        }
//    }
//    
//    func onOfferNeededWithCallId(callId: jlong) {
//        self.dispatchAsync(callId) { () -> () in
//            print("onOfferNeededWithCallId")
//        }
//    }
//    
//    func onAnswerReceivedWithCallId(callId: jlong, withSDP offerSDP: String) {
//        self.dispatchAsync(callId) { () -> () in
//            print("onAnswerReceivedWithCallId")
//        }
//    }
//    
//    func onOfferReceivedWithCallId(callId: jlong, withSDP offerSDP: String) {
//        
//        //
//        // Stages:
//        // 1. Create Peer Connection
//        // 2. Set Remote description
//        // 3. Create Answer
//        // 4. Set Local description from answer
//        // 5. Send Answer and enable candidate receiving
//        //
//        
//        self.dispatchAsync(callId) { () -> () in
//            self.createPeerConnection(callId)
//            self.peerConnection.setRemoteDescription(RTCSessionDescription(type: "offer", sdp: offerSDP)) { (error) -> () in
//                self.dispatchAsync(callId) { () -> () in
//                    if (error == nil) {
//                        self.peerConnection.createAnswer(self.mediaConstraints) { (sdp, error) -> () in
//                            self.dispatchAsync(callId) { () -> () in
//                                if (error == nil) {
//                                    self.peerConnection.setLocalDescription(sdp) { (error) -> () in
//                                        self.dispatchAsync(callId) { () -> () in
//                                            if (error == nil) {
//                                                self.controller.sendAnswerWithNSString(sdp.description)
//                                                self.controller.readyForCandidates()
//                                            } else {
//                                                self.controller.endCall()
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    self.controller.endCall()
//                                }
//                            }
//                        }
//                    } else {
//                        self.controller.endCall()
//                    }
//                }
//            }
//        }
//    }
//    
//    func onCandidateWithCallId(callId: jlong, withId id_: String, withLabel label: jint, withSDP sdp: String) {
//        dispatchAsync(callId) { () -> () in
//            self.peerConnection.addICECandidate(RTCICECandidate(mid: id_, index: Int(label), sdp: sdp))
//        }
//    }
//    
//    
//    //
//    // Dispatching
//    //
//    
//    private func dispatchSync(closure: ()->()) {
//        dispatch_sync(queue) { () -> Void in
//            closure()
//        }
//    }
//    
//    private func dispatchAsync(callId: jlong, closure: ()->()) {
//        if (self.runningCallId == callId) {
//            dispatch_async(queue) { () -> Void in
//                if (self.runningCallId == callId) {
//                    closure()
//                }
//            }
//        }
//    }
}