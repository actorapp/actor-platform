//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

class CocoaWebRTCRuntime: NSObject, ARWebRTCRuntime {
    
    private var isInited: Bool = false
    private var isLocalTrackInited: Bool = false
    private var peerConnectionFactory: RTCPeerConnectionFactory!
    
    private var videoCapturer: RTCVideoCapturer!
    private var videoSource: RTCVideoSource!
    private var localVideoTrack: RTCVideoTrack!
    private var mediaStream: MediaStream!
    
    override init() {
        
    }
    
    func initLocalVideoTrack()  {
        if !isLocalTrackInited{
            isLocalTrackInited = true

        var cameraID: String?
        for captureDevice in AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo) {
            
            if captureDevice.position == AVCaptureDevicePosition.Front {
                cameraID = captureDevice.localizedName
            }
        }
        if(cameraID != nil){
            self.videoCapturer = RTCVideoCapturer(deviceName: cameraID)
         
            self.videoSource = self.peerConnectionFactory.videoSourceWithCapturer(
                self.videoCapturer,
                constraints: RTCMediaConstraints())
            
                self.localVideoTrack = self.peerConnectionFactory
                .videoTrackWithID("ARDAMSv0", source: self.videoSource)}
        }
    }
    
    func getUserMediaWithIsVideoEnabled(isVideoEnabled: jboolean) -> ARPromise {
        
        initRTC()
        initLocalVideoTrack()
        
        let stream = peerConnectionFactory.mediaStreamWithLabel("ARDAMSv0")
        let audio = peerConnectionFactory.audioTrackWithID("audio0")
        
        if(isVideoEnabled){
            
            if(localVideoTrack != nil){
                stream.addVideoTrack(localVideoTrack)
            }
        }
        stream.addAudioTrack(audio)
        
        self.mediaStream = MediaStream(stream:stream)
        return ARPromise.success(self.mediaStream)
    }
    
    func createPeerConnectionWithServers(webRTCIceServers: IOSObjectArray!, withSettings settings: ARWebRTCSettings!) -> ARPromise {
        
        initRTC()
      
        let servers: [ARWebRTCIceServer] = webRTCIceServers.toSwiftArray()
        return ARPromise { (resolver) -> () in
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0)) { () -> Void in
                resolver.result(CocoaWebRTCPeerConnection(servers: servers, peerConnectionFactory: self.peerConnectionFactory, mediaStrim:self.mediaStream))
                
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

class MediaStream: NSObject, ARWebRTCMediaStream {
    
    let stream: RTCMediaStream
    
    init(stream: RTCMediaStream){
        self.stream = stream
    }
    
    func isAudioEnabled() -> jboolean {
        return ActorSDK.sharedActor().enableVideoCalls
    }
    
    func setAudioEnabledWithBoolean(isEnabled: jboolean) {
        setEnabledAudioWithBoolean(isEnabled)
    }
    
    func isVideoEnabled() -> jboolean {
        return ActorSDK.sharedActor().enableVideoCalls
    }
    
    func setVideoEnabledWithBoolean(isEnabled: jboolean) {
        setEnabledVideoWithBoolean(isEnabled)
    }
    
    func isEnabled() -> jboolean {
        return true
    }
    
    func setEnabledAudioWithBoolean(isEnabled: jboolean) {
        for i in stream.audioTracks {
            (i as? RTCMediaStreamTrack)?.setEnabled(isEnabled)
        }
    }
    
    func setEnabledVideoWithBoolean(isEnabled: jboolean) {
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
    private var localMediastreem:MediaStream!
    
    init(servers: [ARWebRTCIceServer], peerConnectionFactory: RTCPeerConnectionFactory , mediaStrim:MediaStream) {
        self.peerConnectionFactory = peerConnectionFactory
        self.localMediastreem = mediaStrim
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
    
    func addOwnStream(stream: ARWebRTCMediaStream) {
        if let str = stream as? MediaStream {
            
            peerConnection.addStream(str.stream)
        }
    }
    
    func removeOwnStream(stream: ARWebRTCMediaStream) {
        if let str = stream as? MediaStream {
            peerConnection.removeStream(str.stream)
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
        AAAudioManager.sharedAudio().peerConnectionEnded()
    }
    
    //
    // RTCPeerConnectionDelegate
    //
    
    func peerConnection(peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        for c in callbacks {
            print(stream.label)
            
            c.onStreamAdded(MediaStream(stream: stream!))
            c.onOwnStreamAdded(localMediastreem)
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        for c in callbacks {
            c.onStreamRemoved(MediaStream(stream: stream!))
        }
    }
    
    func peerConnectionOnRenegotiationNeeded(peerConnection: RTCPeerConnection!) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        for c in callbacks {
            c.onCandidateWithLabel(jint(candidate.sdpMLineIndex), withId: candidate.sdpMid, withCandidate: candidate.sdp)
        }
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didOpenDataChannel dataChannel: RTCDataChannel!) {
        
    }
}