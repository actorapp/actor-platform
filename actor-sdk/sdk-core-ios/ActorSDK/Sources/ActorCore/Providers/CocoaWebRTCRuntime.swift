//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaWebRTCRuntime: NSObject, ARWebRTCRuntime {
    
    private let peerConnectionFactory = RTCPeerConnectionFactory()
    
    override init() {
        RTCPeerConnectionFactory.initializeSSL()
    }
    
    func createPeerConnection() -> ARPromise {
        return ARPromises.success(CocoaWebRTCPeerConnection(peerConnectionFactory: peerConnectionFactory))
    }
    
    func getUserAudio() -> ARPromise {
        let audio = peerConnectionFactory.audioTrackWithID("audio0")
        let mediaStream = peerConnectionFactory.mediaStreamWithLabel("ARDAMSa0")
        mediaStream.addAudioTrack(audio)
        return ARPromises.success(MediaStream(stream: mediaStream))
    }
}

class MediaStream: NSObject, ARWebRTCMediaStream {
    
    let stream: RTCMediaStream
    
    init(stream: RTCMediaStream){
        self.stream = stream
    }
    
    func isEnabled() -> jboolean {
        return true
    }
    
    func setEnabledWithBoolean(isEnabled: jboolean) {
        for i in stream.audioTracks {
            (i as? RTCMediaStreamTrack)?.setEnabled(isEnabled)
        }
        for i in stream.videoTracks {
            (i as? RTCMediaStreamTrack)?.setEnabled(isEnabled)
        }
    }
    
    func close() {
        for i in stream.audioTracks {
            (i as? RTCMediaStreamTrack)?.setEnabled(false)
        }
        for i in stream.videoTracks {
            (i as? RTCMediaStreamTrack)?.setEnabled(false)
        }
    }
}

class CocoaWebRTCPeerConnection: NSObject, ARWebRTCPeerConnection, RTCPeerConnectionDelegate {
    
    private var peerConnection: RTCPeerConnection!
    private var callbacks = [ARWebRTCPeerConnectionCallback]()
    private let peerConnectionFactory: RTCPeerConnectionFactory
    init(peerConnectionFactory: RTCPeerConnectionFactory) {
        self.peerConnectionFactory = peerConnectionFactory
        super.init()
        let iceServers = [
            RTCICEServer(URI: NSURL(string: "stun:62.4.22.219:3478"), username: "", password: ""),
            RTCICEServer(URI: NSURL(string: "turn:62.4.22.219:3478?transport=tcp"), username: "actor", password: "password"),
            RTCICEServer(URI: NSURL(string: "turn:62.4.22.219:3478?transport=udp"), username: "actor", password: "password")
        ]
        peerConnection = peerConnectionFactory.peerConnectionWithICEServers(iceServers, constraints: RTCMediaConstraints(), delegate: self)
    }
    
    func addCallback(callback: ARWebRTCPeerConnectionCallback) {
        if !callbacks.contains({ callback.isEqual($0) }) {
            callbacks.append(callback)
        }
    }
    
    func removeCallback(callback: ARWebRTCPeerConnectionCallback) {
        let index = callbacks.indexOf({ callback.isEqual($0) })
        if index != nil {
            callbacks.removeAtIndex(index!)
        }
    }
    func addCandidateWithIndex(index: jint, withId id_: String, withSDP sdp: String) {
        peerConnection.addICECandidate(RTCICECandidate(mid: id_, index: Int(index), sdp: sdp))
    }
    
    func addOwnStream(stream: ARWebRTCMediaStream) {
        if let audio = stream as? RTCAudioTrack {
            print("addOwnStream")
            let mediaStream = peerConnectionFactory.mediaStreamWithLabel("ARDAMSa0")
            mediaStream.addAudioTrack(audio)
            peerConnection.addStream(mediaStream)
        }
    }
    
    func createAnswer() -> ARPromise {
        return ARPromise(closure: { (resolver) -> () in
            self.peerConnection.createAnswer(RTCMediaConstraints(), didCreate: { (desc, error) -> () in
                if error == nil {
                    resolver.result(ARWebRTCSessionDescription(type: "answer", withSDP: desc.description))
                } else {
                    resolver.error(JavaLangException(NSString: "Error \(error.description)"))
                }
            })
        })
    }
    
    func creteOffer() -> ARPromise {
        return ARPromise(closure: { (resolver) -> () in
            self.peerConnection.createOffer(RTCMediaConstraints(), didCreate: { (desc, error) -> () in
                if error == nil {
                    resolver.result(ARWebRTCSessionDescription(type: "offer", withSDP: desc.description))
                } else {
                    resolver.error(JavaLangException(NSString: "Error \(error.description)"))
                }
            })
        })
    }
    
    func setRemoteDescription(description_: ARWebRTCSessionDescription) -> ARPromise {
        return ARPromise(executor: AAPromiseFunc(closure: { (resolver) -> () in
            self.peerConnection.setRemoteDescription(RTCSessionDescription(type: description_.type, sdp: description_.sdp), didSet: { (error) -> () in
                if (error == nil) {
                    resolver.result(description_)
                } else {
                    resolver.error(JavaLangException(NSString: "Error \(error.description)"))
                }
            })
        }))
    }
    
    func setLocalDescription(description_: ARWebRTCSessionDescription) -> ARPromise {
        return ARPromise(executor: AAPromiseFunc(closure: { (resolver) -> () in
            self.peerConnection.setLocalDescription(RTCSessionDescription(type: description_.type, sdp: description_.sdp), didSet: { (error) -> () in
                if (error == nil) {
                    resolver.result(description_)
                } else {
                    resolver.error(JavaLangException(NSString: "Error \(error.description)"))
                }
            })
        }))

    }
    
    func close() {
        peerConnection.close()
    }
    
    
    //
    // RTCPeerConnectionDelegate
    //
    
    func peerConnection(peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        print("signalingStateChanged \(stateChanged)")
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        print("addedStream")
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        print("removedStream")
    }
    
    func peerConnectionOnRenegotiationNeeded(peerConnection: RTCPeerConnection!) {
        print("onRenegotiationNeeded")
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        print("iceConnectionChanged \(newState)")
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        print("iceGatheringChanged \(newState)")
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        print("gotICECandidate \(candidate)")
        for c in callbacks {
            c.onCandidateWithLabel(jint(candidate.sdpMLineIndex), withId: candidate.sdpMid, withCandidate: candidate.sdp)
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didOpenDataChannel dataChannel: RTCDataChannel!) {
        
    }
}