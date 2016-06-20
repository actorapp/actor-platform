//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

class CocoaWebRTCRuntime: NSObject, ARWebRTCRuntime {
    
    private var isInited: Bool = false
    private var peerConnectionFactory: RTCPeerConnectionFactory!
    private var videoSource: RTCVideoSource!
    private var videoSourceLoaded = false
    
    override init() {
        
    }
    
    func getUserMediaWithIsAudioEnabled(isAudioEnabled: jboolean, withIsVideoEnabled isVideoEnabled: jboolean) -> ARPromise {
        
        initRTC()
        
        let stream = self.peerConnectionFactory.mediaStreamWithLabel("ARDAMSv0")
        
        //
        // Audio
        //
        if isAudioEnabled {
            let audio = self.peerConnectionFactory.audioTrackWithID("audio0")
            stream.addAudioTrack(audio)
        }
        
        //
        // Video
        //
        var videoCapturer: RTCVideoCapturer! = nil
        if isVideoEnabled {
            if !videoSourceLoaded {
                videoSourceLoaded = true
                
                var cameraID: String?
                for captureDevice in AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo) {
                    if captureDevice.position == AVCaptureDevicePosition.Front {
                        cameraID = captureDevice.localizedName
                    }
                }
                
                if(cameraID != nil) {
                    videoCapturer = RTCVideoCapturer(deviceName: cameraID)
                    videoSource = self.peerConnectionFactory.videoSourceWithCapturer(videoCapturer, constraints: RTCMediaConstraints())
                }
            }
            if videoSource != nil {
                let localVideoTrack = self.peerConnectionFactory.videoTrackWithID("video0", source: videoSource)
                stream.addVideoTrack(localVideoTrack)
            }
        }
        
        return ARPromise.success(MediaStream(stream:stream))
    }
    
    func createPeerConnectionWithServers(webRTCIceServers: IOSObjectArray!, withSettings settings: ARWebRTCSettings!) -> ARPromise {
        initRTC()
        let servers: [ARWebRTCIceServer] = webRTCIceServers.toSwiftArray()
        return ARPromise { (resolver) -> () in
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0)) { () -> Void in
                resolver.result(CocoaWebRTCPeerConnection(servers: servers, peerConnectionFactory: self.peerConnectionFactory))
            }
        }
    }
    
    func initRTC() {
        if !isInited {
            isInited = true
            RTCPeerConnectionFactory.initializeSSL()
            peerConnectionFactory = RTCPeerConnectionFactory()
        }
    }
    
    func supportsPreConnections() -> jboolean {
        return false
    }
}

@objc class MediaStream: NSObject, ARWebRTCMediaStream {
    
    let stream: RTCMediaStream
    
    init(stream: RTCMediaStream) {
        self.stream = stream
    }
    
    func close() {
        for i in stream.audioTracks {
            (i as! RTCAudioTrack).setEnabled(false)
            // stream.removeAudioTrack(i as! RTCAudioTrack)
        }
        for i in stream.videoTracks {
            (i as! RTCVideoTrack).setEnabled(false)
            // stream.removeVideoTrack(i as! RTCVideoTrack)
        }
    }
}

class CocoaWebRTCPeerConnection: NSObject, ARWebRTCPeerConnection, RTCPeerConnectionDelegate {
    
    private var peerConnection: RTCPeerConnection!
    private var callbacks = [ARWebRTCPeerConnectionCallback]()
    private let peerConnectionFactory: RTCPeerConnectionFactory
    
    init(servers: [ARWebRTCIceServer], peerConnectionFactory: RTCPeerConnectionFactory) {
        self.peerConnectionFactory = peerConnectionFactory
        super.init()
        
        let iceServers = servers.map { (src) -> RTCICEServer in
            if (src.username == nil || src.credential == nil) {
                return RTCICEServer(URI: NSURL(string: src.url), username: "", password: "")
            } else {
                return RTCICEServer(URI: NSURL(string: src.url), username: src.username, password: src.credential)
            }
        }
        
        peerConnection = peerConnectionFactory.peerConnectionWithICEServers(iceServers, constraints: RTCMediaConstraints(), delegate: self)
        AAAudioManager.sharedAudio().peerConnectionStarted()
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
    
//    func addOwnStream(stream: ARWebRTCMediaStream) {
//        if let str = stream as? MediaStream {
//            peerConnection.addStream(str.stream)
//        }
//    }
//    
    func addOwnStream(stream: ARCountedReference) {
        peerConnection.addStream((stream.get() as! MediaStream).stream)
    }
    
    func removeOwnStream(stream: ARCountedReference) {
        peerConnection.addStream((stream.get() as! MediaStream).stream)
    }
    
//    func removeOwnStream(stream: ARWebRTCMediaStream) {
//        if let str = stream as? MediaStream {
//            peerConnection.removeStream(str.stream)
//        }
//    }
    
    func createAnswer() -> ARPromise {
        return ARPromise(closure: { (resolver) -> () in
//            let constraints = RTCMediaConstraints(mandatoryConstraints: [RTCPair(key: "OfferToReceiveAudio", value: "true"),
//                RTCPair(key: "OfferToReceiveVideo", value: "true")], optionalConstraints: [])
            let constraints = RTCMediaConstraints()
            self.peerConnection.createAnswer(constraints, didCreate: { (desc, error) -> () in
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
            let constraints = RTCMediaConstraints()
//            let constraints = RTCMediaConstraints(mandatoryConstraints: [RTCPair(key: "OfferToReceiveAudio", value: "true"),
//                RTCPair(key: "OfferToReceiveVideo", value: "true")], optionalConstraints: [])
            self.peerConnection.createOffer(constraints, didCreate: { (desc, error) -> () in
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
        AAAudioManager.sharedAudio().peerConnectionEnded()
    }
    
    //
    // RTCPeerConnectionDelegate
    //
    
    
    func peerConnection(peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        for c in callbacks {
            c.onStreamAdded(MediaStream(stream: stream!))
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        for c in callbacks {
            c.onStreamRemoved(MediaStream(stream: stream!))
        }
    }
    
    func peerConnectionOnRenegotiationNeeded(peerConnection: RTCPeerConnection!) {
        for c in callbacks {
            c.onRenegotiationNeeded()
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        for c in callbacks {
            c.onCandidateWithLabel(jint(candidate.sdpMLineIndex), withId: candidate.sdpMid, withCandidate: candidate.sdp)
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        
    }

    func peerConnection(peerConnection: RTCPeerConnection!, didOpenDataChannel dataChannel: RTCDataChannel!) {
        
    }
}