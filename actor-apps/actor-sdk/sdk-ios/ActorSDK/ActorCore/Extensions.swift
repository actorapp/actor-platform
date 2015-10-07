//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public extension ACCocoaMessenger {
    public func sendUIImage(image: UIImage, peer: ACPeer) {
        let thumb = image.resizeSquare(90, maxH: 90);
        let resized = image.resizeOptimize(1200 * 1200);
        
        let thumbData = UIImageJPEGRepresentation(thumb, 0.55);
        let fastThumb = ACFastThumb(int: jint(thumb.size.width), withInt: jint(thumb.size.height), withByteArray: thumbData!.toJavaBytes())
        
        let descriptor = "/tmp/"+NSUUID().UUIDString
        let path = CocoaFiles.pathFromDescriptor(descriptor);
        
        UIImageJPEGRepresentation(resized, 0.80)!.writeToFile(path, atomically: true)
        
        sendPhotoWithPeer(peer, withName: "image.jpg", withW: jint(resized.size.width), withH: jint(resized.size.height), withThumb: fastThumb, withDescriptor: descriptor)
    }
    
    private func prepareAvatar(image: UIImage) -> String {
        let res = "/tmp/" + NSUUID().UUIDString
        let avatarPath = CocoaFiles.pathFromDescriptor(res)
        let thumb = image.resizeSquare(800, maxH: 800);
        UIImageJPEGRepresentation(thumb, 0.8)!.writeToFile(avatarPath, atomically: true)
        return res
    }
    
    public func changeOwnAvatar(image: UIImage) {
        changeMyAvatarWithDescriptor(prepareAvatar(image))
    }
    
    public func changeGroupAvatar(gid: jint, image: UIImage) {
        changeGroupAvatarWithGid(gid, withDescriptor: prepareAvatar(image))
    }
    
    public func requestFileState(fileId: jlong, notDownloaded: (()->())?, onDownloading: ((progress: Double) -> ())?, onDownloaded: ((reference: String) -> ())?) {
        Actor.requestStateWithFileId(fileId, withCallback: CocoaDownloadCallback(notDownloaded: notDownloaded, onDownloading: onDownloading, onDownloaded: onDownloaded))
    }
    
    public func requestFileState(fileId: jlong, onDownloaded: ((reference: String) -> ())?) {
        Actor.requestStateWithFileId(fileId, withCallback: CocoaDownloadCallback(notDownloaded: nil, onDownloading: nil, onDownloaded: onDownloaded))
    }
}