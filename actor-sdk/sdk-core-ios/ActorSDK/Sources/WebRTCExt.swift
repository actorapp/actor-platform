//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import BrightFutures

class AAPeerConnectionDelegate: NSObject, RTCPeerConnectionDelegate {
    
    var onCandidateReceived: ((RTCICECandidate)->())?
    var onStreamAdded: ((RTCMediaStream) -> ())?
    
    func peerConnection(peerConnection: RTCPeerConnection!, signalingStateChanged stateChanged: RTCSignalingState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, addedStream stream: RTCMediaStream!) {
        onStreamAdded?(stream)
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, removedStream stream: RTCMediaStream!) {
        
    }
    
    func peerConnectionOnRenegotiationNeeded(peerConnection: RTCPeerConnection!) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceConnectionChanged newState: RTCICEConnectionState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, iceGatheringChanged newState: RTCICEGatheringState) {
        
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, gotICECandidate candidate: RTCICECandidate!) {
        onCandidateReceived?(candidate)
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didOpenDataChannel dataChannel: RTCDataChannel!) {
        
    }
}

class AASessionDescriptionCreateDelegate: NSObject, RTCSessionDescriptionDelegate {
    
    let didCreate: (RTCSessionDescription!, NSError!) -> ()
    let peerConnection: RTCPeerConnection
    
    init(didCreate: (RTCSessionDescription!, NSError!) -> (), peerConnection: RTCPeerConnection) {
        self.didCreate = didCreate
        self.peerConnection = peerConnection
    }

    func peerConnection(peerConnection: RTCPeerConnection!, didCreateSessionDescription sdp: RTCSessionDescription!, error: NSError!) {
        didCreate(sdp!, error)
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didSetSessionDescriptionWithError error: NSError!) {
        
    }
}


private var sessionSetTarget = "descTarget"
class AASessionDescriptionSetDelegate: NSObject, RTCSessionDescriptionDelegate {
    
    let didSet: (NSError!) -> ()
    let peerConnection: RTCPeerConnection
    
    init(didSet: (NSError!) -> (), peerConnection: RTCPeerConnection) {
        self.didSet = didSet
        self.peerConnection = peerConnection
        super.init()
        
        setAssociatedObject(peerConnection, value: self, associativeKey: &sessionSetTarget)
    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didCreateSessionDescription sdp: RTCSessionDescription!, error: NSError!) {

    }
    
    func peerConnection(peerConnection: RTCPeerConnection!, didSetSessionDescriptionWithError error: NSError!) {
        
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
    
    private func intDelegate() -> AAPeerConnectionDelegate {
        let stored = self.delegate as? AAPeerConnectionDelegate
        if (stored != nil) {
            return stored!
        }
        
        let nDelegate = AAPeerConnectionDelegate()
        self.delegate = nDelegate
        setAssociatedObject(self, value: nDelegate, associativeKey: &targetReference)
        return nDelegate
    }
    
    func createAnswer(constraints: RTCMediaConstraints, didCreate: (RTCSessionDescription!, NSError!) -> ()) {
        createAnswerWithDelegate(AASessionDescriptionCreateDelegate(didCreate: didCreate, peerConnection: self), constraints: constraints)
    }
    
    func createOffer(constraints: RTCMediaConstraints, didCreate: (RTCSessionDescription!, NSError!) -> ()) {
        createOfferWithDelegate(AASessionDescriptionCreateDelegate(didCreate: didCreate, peerConnection: self), constraints: constraints)
    }
    
    func setLocalDescription(sdp: RTCSessionDescription, didSet: (NSError!) -> ()) {
        setLocalDescriptionWithDelegate(AASessionDescriptionSetDelegate(didSet: didSet, peerConnection: self), sessionDescription: sdp)
    }
    
    func setRemoteDescription(sdp: RTCSessionDescription, didSet: (NSError!) -> ()) {
        setRemoteDescriptionWithDelegate(AASessionDescriptionSetDelegate(didSet: didSet, peerConnection: self), sessionDescription: sdp)
    }
}





