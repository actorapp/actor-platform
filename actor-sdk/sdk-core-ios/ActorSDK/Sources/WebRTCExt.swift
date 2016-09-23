//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAPeerConnectionDelegate: NSObject, RTCPeerConnectionDelegate {
    
    var onCandidateReceived: ((RTCICECandidate)->())?
    var onStreamAdded: ((RTCMediaStream) -> ())?
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        onStreamAdded?(stream)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        
    }
    
    func peerConnection(onRenegotiationNeeded peerConnection: RTCPeerConnection!) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        onCandidateReceived?(candidate)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, didOpen dataChannel: RTCDataChannel!) {
        
    }
}

class AASessionDescriptionCreateDelegate: NSObject, RTCSessionDescriptionDelegate {
    
    let didCreate: (RTCSessionDescription?, Error?) -> ()
    let peerConnection: RTCPeerConnection
    
    init(didCreate: @escaping (RTCSessionDescription?, Error?) -> (), peerConnection: RTCPeerConnection) {
        self.didCreate = didCreate
        self.peerConnection = peerConnection
    }

    func peerConnection(_ peerConnection: RTCPeerConnection!, didCreateSessionDescription sdp: RTCSessionDescription!, error: Error!) {
        didCreate(sdp!, error)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, didSetSessionDescriptionWithError error: Error!) {
        
    }
}


private var sessionSetTarget = "descTarget"
class AASessionDescriptionSetDelegate: NSObject, RTCSessionDescriptionDelegate {
    
    let didSet: (Error!) -> ()
    let peerConnection: RTCPeerConnection
    
    init(didSet: @escaping (Error!) -> (), peerConnection: RTCPeerConnection) {
        self.didSet = didSet
        self.peerConnection = peerConnection
        super.init()
        
        setAssociatedObject(peerConnection, value: self, associativeKey: &sessionSetTarget)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, didCreateSessionDescription sdp: RTCSessionDescription!, error: Error!) {

    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection!, didSetSessionDescriptionWithError error: Error!) {
        
        setAssociatedObject(peerConnection, value: "", associativeKey: &sessionSetTarget)
        
        didSet(error)
    }
}

private var targetReference = "target"
extension RTCPeerConnection {
    
    public var onCandidateReceived: ((RTCICECandidate)->())? {
        set (value) {
            intDelegate().onCandidateReceived = value
        }
        get {
            return intDelegate().onCandidateReceived
        }
    }
    
    public var onStreamAdded: ((RTCMediaStream) -> ())? {
        set (value) {
            intDelegate().onStreamAdded = value
        }
        get {
            return intDelegate().onStreamAdded
        }
    }
    
    fileprivate func intDelegate() -> AAPeerConnectionDelegate {
        let stored = self.delegate as? AAPeerConnectionDelegate
        if (stored != nil) {
            return stored!
        }
        
        let nDelegate = AAPeerConnectionDelegate()
        self.delegate = nDelegate
        setAssociatedObject(self, value: nDelegate, associativeKey: &targetReference)
        return nDelegate
    }
    
    func createAnswer(_ constraints: RTCMediaConstraints, didCreate: @escaping (RTCSessionDescription?, Error?) -> ()) {
        self.createAnswer(with: AASessionDescriptionCreateDelegate(didCreate: didCreate, peerConnection: self), constraints: constraints)
    }
    
    func createOffer(_ constraints: RTCMediaConstraints, didCreate: @escaping (RTCSessionDescription?, Error?) -> ()) {
        self.createOffer(with: AASessionDescriptionCreateDelegate(didCreate: didCreate, peerConnection: self), constraints: constraints)
    }
    
    func setLocalDescription(_ sdp: RTCSessionDescription, didSet: @escaping (Error!) -> ()) {
        setLocalDescriptionWith(AASessionDescriptionSetDelegate(didSet: didSet, peerConnection: self), sessionDescription: sdp)
    }
    
    func setRemoteDescription(_ sdp: RTCSessionDescription, didSet: @escaping (Error!) -> ()) {
        setRemoteDescriptionWith(AASessionDescriptionSetDelegate(didSet: didSet, peerConnection: self), sessionDescription: sdp)
    }
}





