//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

public var Actor : ACCocoaMessenger {
    get {
        return ActorSDK.sharedActor().messenger
    }
}

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
    
    
    public func sendVideo(url: NSURL, peer: ACPeer) {
        
        if let videoData = NSData(contentsOfURL: url) { // if data have on this local path url go to upload
            
            let descriptor = "/tmp/"+NSUUID().UUIDString
            let path = CocoaFiles.pathFromDescriptor(descriptor);
            
            videoData.writeToFile(path, atomically: true) // write to file
            
            // get video duration
            
            let assetforduration = AVURLAsset(URL: url)
            let videoDuration = assetforduration.duration
            let videoDurationSeconds = CMTimeGetSeconds(videoDuration)
            
            // get thubnail and upload
            
            let movieAsset = AVAsset(URL: url) // video asset
            let imageGenerator = AVAssetImageGenerator(asset: movieAsset)
            var thumbnailTime = movieAsset.duration
            thumbnailTime.value = 25
            
            let orientation = movieAsset.videoOrientation()
            
            do {
                let imageRef = try imageGenerator.copyCGImageAtTime(thumbnailTime, actualTime: nil)
                let thumbnail = UIImage(CGImage: imageRef)
                var thumb = thumbnail.resizeSquare(90, maxH: 90);
                let resized = thumbnail.resizeOptimize(1200 * 1200);
                
                if (orientation.orientation.isPortrait) == true {
                    thumb = thumb.imageRotatedByDegrees(90, flip: false)
                }
                
                let thumbData = UIImageJPEGRepresentation(thumb, 0.55); // thumbnail binary data
                let fastThumb = ACFastThumb(int: jint(resized.size.width), withInt: jint(resized.size.height), withByteArray: thumbData!.toJavaBytes())
                
                print("video upload imageRef = \(imageRef)")
                print("video upload thumbnail = \(thumbnail)")
                //print("video upload thumbData = \(thumbData)")
                print("video upload fastThumb = \(fastThumb)")
                print("video upload videoDurationSeconds = \(videoDurationSeconds)")
                print("video upload width = \(thumbnail.size.width)")
                print("video upload height = \(thumbnail.size.height)")
                
                if (orientation.orientation.isPortrait == true) {
                    sendVideoWithPeer(peer, withName: "video.mp4", withW: jint(thumbnail.size.height/2), withH: jint(thumbnail.size.width/2), withDuration: jint(videoDurationSeconds), withThumb: fastThumb, withDescriptor: descriptor)
                } else {
                    sendVideoWithPeer(peer, withName: "video.mp4", withW: jint(thumbnail.size.width), withH: jint(thumbnail.size.height), withDuration: jint(videoDurationSeconds), withThumb: fastThumb, withDescriptor: descriptor)
                }
                
                
            } catch {
                print("can't get thumbnail image")
            }
        

        }

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
        Actor.requestStateWithFileId(fileId, withCallback: AAFileCallback(notDownloaded: notDownloaded, onDownloading: onDownloading, onDownloaded: onDownloaded))
    }
    
    public func requestFileState(fileId: jlong, onDownloaded: ((reference: String) -> ())?) {
        Actor.requestStateWithFileId(fileId, withCallback: AAFileCallback(notDownloaded: nil, onDownloading: nil, onDownloaded: onDownloaded))
    }
}

//
// Collcections
//

extension JavaUtilAbstractCollection : SequenceType {
    
    public func generate() -> NSFastGenerator {
        return NSFastGenerator(self)
    }
}


public extension JavaUtilList {
    public func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.size() {
            res.append(self.getWithInt(i) as! T)
        }
        return res
    }
}

public extension IOSObjectArray {
    public func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.length() {
            res.append(self.objectAtIndex(UInt(i)) as! T)
        }
        return res
    }
}

extension NSData {
    func toJavaBytes() -> IOSByteArray {
        return IOSByteArray(bytes: UnsafePointer<jbyte>(self.bytes), count: UInt(self.length))
    }
}

//
// Entities
//

public extension ACPeer {
    
    public var isGroup: Bool {
        get {
            return self.peerType.ordinal() == ACPeerType.GROUP().ordinal()
        }
    }
    
    public var isPrivate: Bool {
        get {
            return self.peerType.ordinal() == ACPeerType.PRIVATE().ordinal()
        }
    }
}

public extension ACMessage {
    
    public var isOut: Bool {
        get {
            return Actor.myUid() == self.senderId
        }
    }
}

//
// Callbacks
//

public class AACommandCallback: NSObject, ACCommandCallback {
    
    public var resultClosure: ((val: AnyObject!) -> ())?;
    public var errorClosure: ((val:JavaLangException!) -> ())?;
    
    public init<T>(result: ((val:T?) -> ())?, error: ((val:JavaLangException!) -> ())?) {
        super.init()
        self.resultClosure = { (val: AnyObject!) -> () in
            result?(val: val as? T)
        }
        self.errorClosure = error
    }
    
    public func onResult(res: AnyObject!) {
        resultClosure?(val: res)
    }
    
    public func onError(e: JavaLangException!) {
        errorClosure?(val: e)
    }
}

class AAUploadFileCallback : NSObject, ACUploadFileCallback {
    
    let notUploaded: (()->())?
    let onUploading: ((progress: Double) -> ())?
    let onUploadedClosure: (() -> ())?
    
    init(notUploaded: (()->())?, onUploading: ((progress: Double) -> ())?, onUploadedClosure: (() -> ())?) {
        self.onUploading = onUploading
        self.notUploaded = notUploaded
        self.onUploadedClosure = onUploadedClosure;
    }
    
    func onNotUploading() {
        self.notUploaded?()
    }
    
    func onUploaded() {
        self.onUploadedClosure?()
    }
    
    func onUploading(progress: jfloat) {
        self.onUploading?(progress: Double(progress))
    }
}

class AAFileCallback : NSObject, ACFileCallback {
    
    let notDownloaded: (()->())?
    let onDownloading: ((progress: Double) -> ())?
    let onDownloaded: ((fileName: String) -> ())?
    
    init(notDownloaded: (()->())?, onDownloading: ((progress: Double) -> ())?, onDownloaded: ((reference: String) -> ())?) {
        self.notDownloaded = notDownloaded;
        self.onDownloading = onDownloading;
        self.onDownloaded = onDownloaded;
    }
    
    init(onDownloaded: (reference: String) -> ()) {
        self.notDownloaded = nil;
        self.onDownloading = nil;
        self.onDownloaded = onDownloaded;
    }
    
    func onNotDownloaded() {
        self.notDownloaded?();
    }
    
    func onDownloading(progress: jfloat) {
        self.onDownloading?(progress: Double(progress));
    }
    
    func onDownloaded(reference: ARFileSystemReference!) {
        self.onDownloaded?(fileName: reference!.getDescriptor());
    }
}


//
// Markdown
//

//public class ARMDFormattedText {
//    
//    public let isTrivial: Bool
//    public let attributedText: NSAttributedString
//    public let code: [String]
//    
//    public init(attributedText: NSAttributedString, isTrivial: Bool, code: [String]) {
//        self.attributedText = attributedText
//        self.code = code
//        self.isTrivial = isTrivial
//    }
//}

public class TextParser {
    
    public let textColor: UIColor
    public let linkColor: UIColor
    public let fontSize: CGFloat
    
    private let markdownParser = ARMarkdownParser(int: ARMarkdownParser_MODE_FULL)
    
    public init(textColor: UIColor, linkColor: UIColor, fontSize: CGFloat) {
        self.textColor = textColor
        self.linkColor = linkColor
        self.fontSize = fontSize
    }
    
    public func parse(text: String) -> ParsedText {
        let doc = markdownParser.processDocumentWithNSString(text)
        
        if doc.isTrivial() {
            let nAttrText = NSMutableAttributedString(string: text)
            let range = NSRange(location: 0, length: nAttrText.length)
            nAttrText.yy_setColor(textColor, range: range)
            nAttrText.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
            return ParsedText(attributedText: nAttrText, isTrivial: true, code: [])
        }
        
        var sources = [String]()
        
        let sections: [ARMDSection] = doc.getSections().toSwiftArray()
        let nAttrText = NSMutableAttributedString()
        var isFirst = true
        for s in sections {
            if !isFirst {
                nAttrText.appendAttributedString(NSAttributedString(string: "\n"))
            }
            isFirst = false
            
            if s.getType() == ARMDSection_TYPE_CODE {
                
                let str = NSMutableAttributedString(string: AALocalized("ActionOpenCode"))
                let range = NSRange(location: 0, length: str.length)
                
                let highlight = YYTextHighlight()
                highlight.userInfo = ["url" :  "source:///\(sources.count)"]
                str.yy_setTextHighlight(highlight, range: range)
                str.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
                str.yy_setColor(linkColor, range: range)
                
                nAttrText.appendAttributedString(str)
                
                sources.append(s.getCode().getCode())
            } else if s.getType() == ARMDSection_TYPE_TEXT {
                let child: [ARMDText] = s.getText().toSwiftArray()
                for c in child {
                    nAttrText.appendAttributedString(buildText(c, fontSize: fontSize))
                }
            } else {
                fatalError("Unsupported section type")
            }
        }
        
        return ParsedText(attributedText: nAttrText, isTrivial: false, code: sources)
    }
    
    private func buildText(text: ARMDText, fontSize: CGFloat) -> NSAttributedString {
        if let raw = text as? ARMDRawText {
            let res = NSMutableAttributedString(string: raw.getRawText())
            let range = NSRange(location: 0, length: res.length)
            res.beginEditing()
            res.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
            res.yy_setColor(textColor, range: range)
            res.endEditing()
            return res
        } else if let span = text as? ARMDSpan {
            let res = NSMutableAttributedString()
            res.beginEditing()
            
            // Processing child texts
            let child: [ARMDText] = span.getChild().toSwiftArray()
            for c in child {
                res.appendAttributedString(buildText(c, fontSize: fontSize))
            }
            
            // Setting span elements
            if span.getSpanType() == ARMDSpan_TYPE_BOLD {
                res.appendFont(UIFont.boldSystemFontOfSize(fontSize))
            } else if span.getSpanType() == ARMDSpan_TYPE_ITALIC {
                res.appendFont(UIFont.italicSystemFontOfSize(fontSize))
            } else {
                fatalError("Unsupported span type")
            }
            
            res.endEditing()
            return res
        } else if let url = text as? ARMDUrl {
            
            // Parsing url element
            let nsUrl = NSURL(string: url.getUrl())
            if nsUrl != nil {
                let res = NSMutableAttributedString(string:  url.getUrlTitle())
                let range = NSRange(location: 0, length: res.length)
                let highlight = YYTextHighlight()
                highlight.userInfo = ["url" : url.getUrl()]
                res.yy_setTextHighlight(highlight, range: range)
                res.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
                res.yy_setColor(linkColor, range: range)
                return res
            } else {
                // Unable to parse: show as text
                let res = NSMutableAttributedString(string: url.getUrlTitle())
                let range = NSRange(location: 0, length: res.length)
                res.beginEditing()
                res.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
                res.yy_setColor(textColor, range: range)
                res.endEditing()
                return res
            }
        } else {
            fatalError("Unsupported text type")
        }
    }
}

public class ParsedText {
    
    public let isTrivial: Bool
    public let attributedText: NSAttributedString
    public let code: [String]
    
    public init(attributedText: NSAttributedString, isTrivial: Bool, code: [String]) {
        self.attributedText = attributedText
        self.code = code
        self.isTrivial = isTrivial
    }
}
//
//public extension ARMarkdownParser {
//    
//    public func parse(text: String, textColor: UIColor, fontSize: CGFloat) -> ARMDFormattedText {
//        
//        let doc = self.processDocumentWithNSString(text)
//        if doc.isTrivial() {
//            let nAttrText = NSMutableAttributedString(string: text)
//            let range = NSRange(location: 0, length: nAttrText.length)
//            nAttrText.yy_setColor(textColor, range: range)
//            nAttrText.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
//            return ARMDFormattedText(attributedText: nAttrText, isTrivial: true, code: [])
//        }
//        
//        var sources = [String]()
//        
//        let sections: [ARMDSection] = doc.getSections().toSwiftArray()
//        let nAttrText = NSMutableAttributedString()
//        var isFirst = true
//        for s in sections {
//            if !isFirst {
//                nAttrText.appendAttributedString(NSAttributedString(string: "\n"))
//            }
//            isFirst = false
//            
//            if s.getType() == ARMDSection_TYPE_CODE {
//                let attributes = [NSLinkAttributeName: NSURL(string: "source:///\(sources.count)") as! AnyObject,
//                    NSFontAttributeName: UIFont.textFontOfSize(fontSize)]
//                nAttrText.appendAttributedString(NSAttributedString(string: "Open Code", attributes: attributes))
//                sources.append(s.getCode().getCode())
//            } else if s.getType() == ARMDSection_TYPE_TEXT {
//                let child: [ARMDText] = s.getText().toSwiftArray()
//                for c in child {
//                    nAttrText.appendAttributedString(buildText(c, fontSize: fontSize))
//                }
//            } else {
//                fatalError("Unsupported section type")
//            }
//        }
//        
////        let range = NSRange(location: 0, length: nAttrText.length)
//        
////        nAttrText.yy_setColor(textColor, range: range)
////        nAttrText.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
//
////        nAttrText.enumerateAttributesInRange(range, options:  NSAttributedStringEnumerationOptions.LongestEffectiveRangeNotRequired) { (attrs, range, objBool) -> Void in
////            var attributeDictionary = NSDictionary(dictionary: attrs)
////            
////            for k in attributeDictionary.allKeys {
////                let v = attributeDictionary.objectForKey(k)
////                
////                print("attr: \(k) -> \(v) at \(range)")
////            }
////        }
////        
//        return ARMDFormattedText(attributedText: nAttrText, isTrivial: false, code: sources)
//    }
//    
//    private func buildText(text: ARMDText, fontSize: CGFloat) -> NSAttributedString {
//        if let raw = text as? ARMDRawText {
////            let res = NSMutableAttributedString(string: raw.getRawText())
////            res.yy_setFont(UIFont.textFontOfSize(fontSize), range: NSRange(location: 0, length: raw.getRawText().length))
////            return res
//            return NSAttributedString(string: raw.getRawText(), font: UIFont.textFontOfSize(fontSize))
//        } else if let span = text as? ARMDSpan {
//            let res = NSMutableAttributedString()
//            res.beginEditing()
//            
//            // Processing child texts
//            let child: [ARMDText] = span.getChild().toSwiftArray()
//            for c in child {
//                res.appendAttributedString(buildText(c, fontSize: fontSize))
//            }
//            
//            // Setting span elements
//            if span.getSpanType() == ARMDSpan_TYPE_BOLD {
//                res.appendFont(UIFont.boldSystemFontOfSize(fontSize))
//            } else if span.getSpanType() == ARMDSpan_TYPE_ITALIC {
//                res.appendFont(UIFont.italicSystemFontOfSize(fontSize))
//            } else {
//                fatalError("Unsupported span type")
//            }
//            
//            res.endEditing()
//            return res
//        } else if let url = text as? ARMDUrl {
//            
//            // Parsing url element
//            let nsUrl = NSURL(string: url.getUrl())
//            if nsUrl != nil {
//                let res = NSMutableAttributedString(string:  url.getUrlTitle())
//                let range = NSRange(location: 0, length: res.length)
//                let highlight = YYTextHighlight()
////                res.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
////                res.yy_setTextHighlightRange(range, color: UIColor.redColor(), backgroundColor: nil, tapAction: nil)
//                // res.yy_setColor(UIColor.greenColor(), range: range)
//                return res
////                let attributes = [NSLinkAttributeName: nsUrl as! AnyObject,
////                    NSFontAttributeName: UIFont.textFontOfSize(fontSize)]
////                return NSAttributedString(string: url.getUrlTitle(), attributes: attributes)
//            } else {
//                // Unable to parse: show as text
//                return NSAttributedString(string: url.getUrlTitle(), font: UIFont.textFontOfSize(fontSize))
//            }
//        } else {
//            fatalError("Unsupported text type")
//        }
//    }
//}
//
//
// Promises
//

public class AAPromiseFunc: NSObject, ARPromiseFunc {
    
    let closure: (resolver: ARPromiseResolver) -> ()
    init(closure: (resolver: ARPromiseResolver) -> ()){
        self.closure = closure
    }
    
    public func exec(resolver: ARPromiseResolver) {
        closure(resolver: resolver)
    }
}

extension ARPromise {
    convenience init(closure: (resolver: ARPromiseResolver) -> ()) {
        self.init(executor: AAPromiseFunc(closure: closure))
    }
}

//
// Data Binding
//

public class AABinder {
    
    private var bindings : [BindHolder] = []
    
    public init() {
        
    }
    
    public func bind<T1,T2,T3>(valueModel1:ARValue, valueModel2:ARValue, valueModel3:ARValue, closure: (value1:T1!, value2:T2!, value3:T3!) -> ()) {
        
        let listener1 = BindListener { (_value1) -> () in
            closure(value1: _value1 as? T1, value2: valueModel2.get() as? T2, value3: valueModel3.get() as? T3)
        };
        let listener2 = BindListener { (_value2) -> () in
            closure(value1: valueModel1.get() as? T1, value2: _value2 as? T2, value3: valueModel3.get() as? T3)
        };
        let listener3 = BindListener { (_value3) -> () in
            closure(value1: valueModel1.get() as? T1,  value2: valueModel2.get() as? T2, value3: _value3 as? T3)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        bindings.append(BindHolder(valueModel: valueModel3, listener: listener3))
        valueModel1.subscribeWithListener(listener1, notify: false)
        valueModel2.subscribeWithListener(listener2, notify: false)
        valueModel3.subscribeWithListener(listener3, notify: false)
        closure(value1: valueModel1.get() as? T1, value2: valueModel2.get() as? T2, value3: valueModel3.get() as? T3)
    }
    
    
    public func bind<T1,T2>(valueModel1:ARValue, valueModel2:ARValue, closure: (value1:T1!, value2:T2!) -> ()) {
        let listener1 = BindListener { (_value1) -> () in
            closure(value1: _value1 as? T1, value2: valueModel2.get() as? T2)
        };
        let listener2 = BindListener { (_value2) -> () in
            closure(value1: valueModel1.get() as? T1, value2: _value2 as? T2)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        valueModel1.subscribeWithListener(listener1, notify: false)
        valueModel2.subscribeWithListener(listener2, notify: false)
        closure(value1: valueModel1.get() as? T1, value2: valueModel2.get() as? T2)
    }
    
    public func bind<T>(value:ARValue, closure: (value: T!)->()) {
        let listener = BindListener { (value2) -> () in
            closure(value: value2 as? T)
        };
        let holder = BindHolder(valueModel: value, listener: listener)
        bindings.append(holder)
        value.subscribeWithListener(listener)
    }
    
    public func unbindAll() {
        for holder in bindings {
            holder.valueModel.unsubscribeWithListener(holder.listener)
        }
        bindings.removeAll(keepCapacity: true)
    }
    
}

class BindListener: NSObject, ARValueChangedListener {
    
    var closure: ((value: AnyObject?)->())?
    
    init(closure: (value: AnyObject?)->()) {
        self.closure = closure
    }
    
    func onChanged(val: AnyObject!, withModel valueModel: ARValue!) {
        closure?(value: val)
    }
}

class BindHolder {
    var listener: BindListener
    var valueModel: ARValue
    
    init(valueModel: ARValue, listener: BindListener) {
        self.valueModel = valueModel
        self.listener = listener
    }
}