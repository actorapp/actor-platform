//
//  AvatarView.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 13.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

// Cache

private let cacheSize = 10;
private let loadingPool = SThreadPool()
private var avatarCache = Dictionary<Int, SwiftlyLRU<Int64, UIImage>>()

private func checkCache(size: Int, id:Int64) -> UIImage? {
    if let cache = avatarCache[size] {
        if let img = cache[id] {
            return img
        }
    }
    return nil
}

private func putToCache(size: Int, id: Int64, image: UIImage) {
    if let cache = avatarCache[size] {
        cache[id] = image
    } else {
        var cache = SwiftlyLRU<jlong, UIImage>(capacity: cacheSize);
        cache[id] = image
        avatarCache.updateValue(cache, forKey: size)
    }
}

class AvatarView : UIImageView {
    
    var frameSize: Int = 0;
    
    // Request
    var bindedFileId: jlong! = nil;
    var bindedTitle: String! = nil;
    var bindedId: jint! = nil;
    
    var requestId: Int = 0;
    var callback: CocoaDownloadCallback? = nil;

    init(frameSize: Int) {
        self.frameSize = frameSize;
        super.init(image: nil);
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        self.frameSize = Int(min(frame.width, frame.height))
    }
    
    func bind(title: String, id:jint, avatar: AMAvatar!) {
        
        var fileLocation = avatar?.getSmallImage()?.getFileReference();
        
        if (bindedId != nil && bindedId == id) {
            var notChanged = true;
            
            // Is Preview changed
            notChanged = notChanged && bindedTitle.smallValue() == title.smallValue()
            
            // Is avatar changed
            if (fileLocation == nil) {
                if (bindedFileId != nil) {
                    notChanged = false
                }
            } else if (bindedFileId == nil) {
                if (fileLocation != nil) {
                    notChanged = false
                }
            } else {
                if (bindedFileId != fileLocation?.getFileId()) {
                    notChanged = false
                }
            }
            
            if (notChanged) {
                return
            }
        }
        
        unbind()
        
        self.bindedId = id
        self.bindedTitle = title
        
        if (fileLocation == nil) {
            // No avatar: Apply placeholder
            self.image = Imaging.avatarPlaceholder(bindedId, size: frameSize, title: title.smallValue());
            
            return
        }
        
        // Load avatar

        var cached = checkCache(frameSize, Int64(fileLocation!.getFileId()))
        if (cached != nil) {
            self.image = cached
            return
        }
        
        requestId++

        var callbackRequestId = requestId
        self.bindedFileId = fileLocation?.getFileId()
        self.callback = CocoaDownloadCallback(onDownloaded: { (reference) -> () in
            
            if (callbackRequestId != self.requestId) {
                return;
            }
        
            var image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference));
        
            if (image == nil) {
                return
            }
        
            image = image!.roundImage(self.frameSize);
        
            dispatch_async(dispatch_get_main_queue(), {
                if (callbackRequestId != self.requestId) {
                    return;
                }
        
                putToCache(self.frameSize, Int64(self.bindedFileId!), image!)
                self.image = image;
                
            });
        });
        MSG.bindRawFileWith(fileLocation, withAutoStart: true, withCallback: self.callback)
    }
    
    func unbind() {
        self.image = nil
        self.bindedId = nil
        self.bindedTitle = nil
        
        if (bindedFileId != nil) {
            MSG.unbindRawFile(bindedFileId!, withAutoCancel: false, withCallback: callback)
            bindedFileId = nil
            callback = nil
            requestId++;
        }
    }
    
}