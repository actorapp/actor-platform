//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

let queue = DispatchQueue(label: "My Queue", attributes: []);

class CocoaWebRTCRuntime: NSObject, ARWebRTCRuntime {
    
    
    
    fileprivate var isInited: Bool = false
    fileprivate var peerConnectionFactory: RTCPeerConnectionFactory!
    fileprivate var videoSource: RTCVideoSource!
    fileprivate var videoSourceLoaded = false
    
    override init() {
        
    }
    
    func getUserMedia(withIsAudioEnabled isAudioEnabled: jboolean, withIsVideoEnabled isVideoEnabled: jboolean) -> ARPromise {
        
        return ARPromise { (resolver) -> () in
            queue.async {
                
                self.initRTC()
                
                // Unfortinatelly building capture source "on demand" causes some weird internal crashes
                self.initVideo()
                
                let stream = self.peerConnectionFactory.mediaStream(withLabel: "ARDAMSv0")
                
                //
                // Audio
                //
                if isAudioEnabled {
                    let audio = self.peerConnectionFactory.audioTrack(withID: "audio0")
                    stream?.addAudioTrack(audio)
                }
                
                //
                // Video
                //
                if isVideoEnabled {
                    if self.videoSource != nil {
                        let localVideoTrack = self.peerConnectionFactory.videoTrack(withID: "video0", source: self.videoSource)
                        stream?.addVideoTrack(localVideoTrack)
                    }
                }
                
                resolver.result(MediaStream(stream:stream!))
            }
        }
    }
    
    func createPeerConnection(withServers webRTCIceServers: IOSObjectArray!, with settings: ARWebRTCSettings!) -> ARPromise {
        let servers: [ARWebRTCIceServer] = webRTCIceServers.toSwiftArray()
        return ARPromise { (resolver) -> () in
            queue.async {
                self.initRTC()
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
    
    func initVideo() {
        if !self.videoSourceLoaded {
            self.videoSourceLoaded = true
            
            var cameraID: String?
            for captureDevice in AVCaptureDevice.devices(withMediaType: AVMediaTypeVideo) {
                if (captureDevice as AnyObject).position == AVCaptureDevicePosition.front {
                    cameraID = (captureDevice as AnyObject).localizedName
                }
            }
            
            if(cameraID != nil) {
                let videoCapturer = RTCVideoCapturer(deviceName: cameraID)
                self.videoSource = self.peerConnectionFactory.videoSource(with: videoCapturer, constraints: RTCMediaConstraints())
            }
        }
    }
    
    func supportsPreConnections() -> jboolean {
        return false
    }
}

@objc class MediaStream: NSObject, ARWebRTCMediaStream {
    
    let stream: RTCMediaStream
    let audioTracks: IOSObjectArray
    let videoTracks: IOSObjectArray
    let allTracks: IOSObjectArray
    
    init(stream: RTCMediaStream) {
        self.stream = stream
        
        self.audioTracks = IOSObjectArray(length: UInt(stream.audioTracks.count), type: ARWebRTCMediaTrack_class_())
        self.videoTracks = IOSObjectArray(length: UInt(stream.videoTracks.count), type: ARWebRTCMediaTrack_class_())
        self.allTracks = IOSObjectArray(length: UInt(stream.audioTracks.count + stream.videoTracks.count), type: ARWebRTCMediaTrack_class_())
        
        for i in 0..<stream.audioTracks.count {
            let track = CocoaAudioTrack(audioTrack: stream.audioTracks[i] as! RTCAudioTrack)
            audioTracks.replaceObject(at: UInt(i), with: track)
            allTracks.replaceObject(at: UInt(i), with: track)
        }
        for i in 0..<stream.videoTracks.count {
            let track = CocoaVideoTrack(videoTrack: stream.videoTracks[i] as! RTCVideoTrack)
            videoTracks.replaceObject(at: UInt(i), with: track)
            allTracks.replaceObject(at: UInt(i + audioTracks.length()), with: track)
        }
    }
    
    func getAudioTracks() -> IOSObjectArray! {
        return audioTracks
    }
    
    func getVideoTracks() -> IOSObjectArray! {
        return videoTracks
    }
    
    func getTracks() -> IOSObjectArray! {
        return allTracks
    }
    
    func close() {
        for i in stream.audioTracks {
            (i as! RTCAudioTrack).setEnabled(false)
            stream.removeAudioTrack(i as! RTCAudioTrack)
        }
        for i in stream.videoTracks {
            (i as! RTCVideoTrack).setEnabled(false)
            stream.removeVideoTrack(i as! RTCVideoTrack)
        }
    }
}

open class CocoaAudioTrack: NSObject, ARWebRTCMediaTrack {
    
    open let audioTrack: RTCAudioTrack
    
    public init(audioTrack: RTCAudioTrack) {
        self.audioTrack = audioTrack
    }
    
    open func getType() -> jint {
        return ARWebRTCTrackType_AUDIO
    }
    
    open func setEnabledWithBoolean(_ isEnabled: jboolean) {
        audioTrack.setEnabled(isEnabled)
    }
    
    open func isEnabled() -> jboolean {
        return audioTrack.isEnabled()
    }
}

open class CocoaVideoTrack: NSObject, ARWebRTCMediaTrack {
    
    open let videoTrack: RTCVideoTrack
    
    public init(videoTrack: RTCVideoTrack) {
        self.videoTrack = videoTrack
    }
    
    open func getType() -> jint {
        return ARWebRTCTrackType_VIDEO
    }
    
    open func setEnabledWithBoolean(_ isEnabled: jboolean) {
        videoTrack.setEnabled(isEnabled)
    }
    
    open func isEnabled() -> jboolean {
        return videoTrack.isEnabled()
    }
}

class CocoaWebRTCPeerConnection: NSObject, ARWebRTCPeerConnection, RTCPeerConnectionDelegate {
    
    fileprivate var peerConnection: RTCPeerConnection!
    fileprivate var callbacks = [ARWebRTCPeerConnectionCallback]()
    fileprivate let peerConnectionFactory: RTCPeerConnectionFactory
    fileprivate var ownStreams = [ARCountedReference]()
    
    init(servers: [ARWebRTCIceServer], peerConnectionFactory: RTCPeerConnectionFactory) {
        self.peerConnectionFactory = peerConnectionFactory
        super.init()
        
        let iceServers = servers.map { (src) -> RTCICEServer in
            if (src.username == nil || src.credential == nil) {
                return RTCICEServer(uri: URL(string: src.url), username: "", password: "")
            } else {
                return RTCICEServer(uri: URL(string: src.url), username: src.username, password: src.credential)
            }
        }
        
        peerConnection = peerConnectionFactory.peerConnection(withICEServers: iceServers, constraints: RTCMediaConstraints(), delegate: self)
        AAAudioManager.sharedAudio().peerConnectionStarted()
    }
    
    func add(_ callback: ARWebRTCPeerConnectionCallback) {
        
        if !callbacks.contains(where: { callback.isEqual($0) }) {
            callbacks.append(callback)
        }
    }
    
    func remove(_ callback: ARWebRTCPeerConnectionCallback) {
        let index = callbacks.index(where: { callback.isEqual($0) })
        if index != nil {
            callbacks.remove(at: index!)
        }
    }
    func addCandidate(with index: jint, withId id_: String, withSDP sdp: String) {
        peerConnection.add(RTCICECandidate(mid: id_, index: Int(index), sdp: sdp))
    }
    
    func addOwnStream(_ stream: ARCountedReference) {
        stream.acquire()
        let ms = (stream.get() as! MediaStream)
        ownStreams.append(stream)
        peerConnection.add(ms.stream)
    }
    
    func removeOwnStream(_ stream: ARCountedReference) {
        if ownStreams.contains(stream) {
            let ms = (stream.get() as! MediaStream)
            peerConnection.remove(ms.stream)
            stream.release__()
        }
    }
    
    func createAnswer() -> ARPromise {
        return ARPromise(closure: { (resolver) -> () in
            let constraints = RTCMediaConstraints(mandatoryConstraints: [RTCPair(key: "OfferToReceiveAudio", value: "true"),
                RTCPair(key: "OfferToReceiveVideo", value: "true")], optionalConstraints: [])
            self.peerConnection.createAnswer(constraints!, didCreate: { (desc, error) -> () in
                if error == nil {
                    resolver.result(ARWebRTCSessionDescription(type: "answer", withSDP: desc!.description))
                } else {
                    resolver.error(JavaLangException(nsString: "Error \(error!)"))
                }
            })
        })
    }
    
    func creteOffer() -> ARPromise {
        return ARPromise(closure: { (resolver) -> () in
            let constraints = RTCMediaConstraints(mandatoryConstraints: [RTCPair(key: "OfferToReceiveAudio", value: "true"),
                RTCPair(key: "OfferToReceiveVideo", value: "true")], optionalConstraints: [])
            self.peerConnection.createOffer(constraints!, didCreate: { (desc, error) -> () in
                if error == nil {
                    resolver.result(ARWebRTCSessionDescription(type: "offer", withSDP: desc!.description))
                } else {
                    resolver.error(JavaLangException(nsString: "Error \(error!)"))
                }
            })
        })
    }
    
    func setRemoteDescription(_ description_: ARWebRTCSessionDescription) -> ARPromise {
        return ARPromise(executor: AAPromiseFunc(closure: { (resolver) -> () in
            self.peerConnection.setRemoteDescription(RTCSessionDescription(type: description_.type, sdp: description_.sdp), didSet: { (error) -> () in
                if (error == nil) {
                    resolver.result(description_)
                } else {
                    resolver.error(JavaLangException(nsString: "Error \(error)"))
                }
            })
        }))
    }
    
    func setLocalDescription(_ description_: ARWebRTCSessionDescription) -> ARPromise {
        return ARPromise(executor: AAPromiseFunc(closure: { (resolver) -> () in
            self.peerConnection.setLocalDescription(RTCSessionDescription(type: description_.type, sdp: description_.sdp), didSet: { (error) -> () in
                if (error == nil) {
                    resolver.result(description_)
                } else {
                    resolver.error(JavaLangException(nsString: "Error \(error)"))
                }
            })
        }))
        
    }
    
    func close() {
        for s in ownStreams {
            let ms = s.get() as! MediaStream
            peerConnection.remove(ms.stream)
            s.release__()
        }
        ownStreams.removeAll()
        peerConnection.close()
        AAAudioManager.sharedAudio().peerConnectionEnded()
    }
    
    //
    // RTCPeerConnectionDelegate
    //
    
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        for c in callbacks {
            c.onStreamAdded(MediaStream(stream: stream!))
        }
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        for c in callbacks {
            c.onStreamRemoved(MediaStream(stream: stream!))
        }
    }
    
    func peerConnection(onRenegotiationNeeded peerConnection: RTCPeerConnection!) {
        for c in callbacks {
            c.onRenegotiationNeeded()
        }
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        for c in callbacks {
            c.onCandidate(withLabel: jint(candidate.sdpMLineIndex), withId: candidate.sdpMid, withCandidate: candidate.sdp)
        }
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        
    }

    func peerConnection(_ peerConnection: RTCPeerConnection!, didOpen dataChannel: RTCDataChannel!) {
        
    }
}
