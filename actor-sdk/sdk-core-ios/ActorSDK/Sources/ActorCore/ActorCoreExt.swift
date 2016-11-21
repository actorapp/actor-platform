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
    
    public func sendUIImage(_ image: Data, peer: ACPeer, animated:Bool) {
        
        let imageFromData = UIImage(data:image)
        let thumb = imageFromData!.resizeSquare(90, maxH: 90);
        let resized = imageFromData!.resizeOptimize(1200 * 1200);
        
        let thumbData = UIImageJPEGRepresentation(thumb, 0.55);
        let fastThumb = ACFastThumb(int: jint(thumb.size.width), with: jint(thumb.size.height), with: thumbData!.toJavaBytes())
        
        let descriptor = "/tmp/"+UUID().uuidString
        let path = CocoaFiles.pathFromDescriptor(descriptor);
        
        animated ? ((try? image.write(to: URL(fileURLWithPath: path), options: [.atomic])) != nil) : ((try? UIImageJPEGRepresentation(resized, 0.80)!.write(to: URL(fileURLWithPath: path), options: [.atomic])) != nil)
        
        animated ? sendAnimation(with: peer, withName: "image.gif", withW: jint(resized.size.width), withH:jint(resized.size.height), with: fastThumb, withDescriptor: descriptor) : sendPhoto(with: peer, withName: "image.jpg", withW: jint(resized.size.width), withH: jint(resized.size.height), with: fastThumb, withDescriptor: descriptor)
    }
    
    public func sendVideo(_ url: URL, peer: ACPeer) {
        
        if let videoData = try? Data(contentsOf: url) { // if data have on this local path url go to upload
            
            let descriptor = "/tmp/"+UUID().uuidString
            let path = CocoaFiles.pathFromDescriptor(descriptor);
            
            try? videoData.write(to: URL(fileURLWithPath: path), options: [.atomic]) // write to file
            
            // get video duration
            
            let assetforduration = AVURLAsset(url: url)
            let videoDuration = assetforduration.duration
            let videoDurationSeconds = CMTimeGetSeconds(videoDuration)
            
            // get thubnail and upload
            
            let movieAsset = AVAsset(url: url) // video asset
            let imageGenerator = AVAssetImageGenerator(asset: movieAsset)
            var thumbnailTime = movieAsset.duration
            thumbnailTime.value = 25
            
            let orientation = movieAsset.videoOrientation()
            
            do {
                let imageRef = try imageGenerator.copyCGImage(at: thumbnailTime, actualTime: nil)
                let thumbnail = UIImage(cgImage: imageRef)
                var thumb = thumbnail.resizeSquare(90, maxH: 90);
                let resized = thumbnail.resizeOptimize(1200 * 1200);
                
                if (orientation.orientation.isPortrait) == true {
                    thumb = thumb.imageRotatedByDegrees(90, flip: false)
                }
                
                let thumbData = UIImageJPEGRepresentation(thumb, 0.55); // thumbnail binary data
                let fastThumb = ACFastThumb(int: jint(resized.size.width), with: jint(resized.size.height), with: thumbData!.toJavaBytes())
                
                print("video upload imageRef = \(imageRef)")
                print("video upload thumbnail = \(thumbnail)")
                //print("video upload thumbData = \(thumbData)")
                print("video upload fastThumb = \(fastThumb)")
                print("video upload videoDurationSeconds = \(videoDurationSeconds)")
                print("video upload width = \(thumbnail.size.width)")
                print("video upload height = \(thumbnail.size.height)")
                
                if (orientation.orientation.isPortrait == true) {
                    self.sendVideo(with: peer, withName: "video.mp4", withW: jint(thumbnail.size.height/2), withH: jint(thumbnail.size.width/2), withDuration: jint(videoDurationSeconds), with: fastThumb, withDescriptor: descriptor)
                } else {
                    self.sendVideo(with: peer, withName: "video.mp4", withW: jint(thumbnail.size.width), withH: jint(thumbnail.size.height), withDuration: jint(videoDurationSeconds), with: fastThumb, withDescriptor: descriptor)
                }
                
                
            } catch {
                print("can't get thumbnail image")
            }
        

        }

    }
    
    fileprivate func prepareAvatar(_ image: UIImage) -> String {
        let res = "/tmp/" + UUID().uuidString
        let avatarPath = CocoaFiles.pathFromDescriptor(res)
        let thumb = image.resizeSquare(800, maxH: 800);
        try? UIImageJPEGRepresentation(thumb, 0.8)!.write(to: URL(fileURLWithPath: avatarPath), options: [.atomic])
        return res
    }
    
    public func changeOwnAvatar(_ image: UIImage) {
        changeMyAvatar(withDescriptor: prepareAvatar(image))
    }
    
    public func changeGroupAvatar(_ gid: jint, image: UIImage) -> String {
        let fileName = prepareAvatar(image)
        self.changeGroupAvatar(withGid: gid, withDescriptor: fileName)
        return fileName
    }
    
    public func requestFileState(_ fileId: jlong, notDownloaded: (()->())?, onDownloading: ((_ progress: Double) -> ())?, onDownloaded: ((_ reference: String) -> ())?) {
        Actor.requestState(withFileId: fileId, with: AAFileCallback(notDownloaded: notDownloaded, onDownloading: onDownloading, onDownloaded: onDownloaded))
    }
    
    public func requestFileState(_ fileId: jlong, onDownloaded: ((_ reference: String) -> ())?) {
        Actor.requestState(withFileId: fileId, with: AAFileCallback(notDownloaded: nil, onDownloading: nil, onDownloaded: onDownloaded))
    }
}

//
// Collcections
//

extension JavaUtilAbstractCollection : Sequence {
    
    public func makeIterator() -> NSFastEnumerationIterator {
        return NSFastEnumerationIterator(self)
    }
}


public extension JavaUtilList {
    public func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.size() {
            res.append(self.getWith(i) as! T)
        }
        return res
    }
}

public extension IOSObjectArray {
    public func toSwiftArray<T>() -> [T] {
        var res = [T]()
        for i in 0..<self.length() {
            res.append(self.object(at: UInt(i)) as! T)
        }
        return res
    }
}

public extension Data {
    public func toJavaBytes() -> IOSByteArray {
        return IOSByteArray(bytes: (self as NSData).bytes.bindMemory(to: jbyte.self, capacity: self.count), count: UInt(self.count))
    }
}

//
// Entities
//

public extension ACPeer {
    
    public var isGroup: Bool {
        get {
            return self.peerType.ordinal() == ACPeerType.group().ordinal()
        }
    }
    
    public var isPrivate: Bool {
        get {
            return self.peerType.ordinal() == ACPeerType.private().ordinal()
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

open class AACommandCallback: NSObject, ACCommandCallback {
    
    open var resultClosure: ((_ val: Any?) -> ())?;
    open var errorClosure: ((_ val:JavaLangException?) -> ())?;
    
    public init<T>(result: ((_ val:T?) -> ())?, error: ((_ val:JavaLangException?) -> ())?) {
        super.init()
        self.resultClosure = { (val: Any!) -> () in
            (result?(val as? T))!
        }
        self.errorClosure = error
    }
    
    open func onResult(_ res: Any!) {
        resultClosure?(res)
    }
    
    open func onError(_ e: JavaLangException!) {
        errorClosure?(e)
    }
}

class AAUploadFileCallback : NSObject, ACUploadFileCallback {
    
    let notUploaded: (()->())?
    let onUploading: ((_ progress: Double) -> ())?
    let onUploadedClosure: (() -> ())?
    
    init(notUploaded: (()->())?, onUploading: ((_ progress: Double) -> ())?, onUploadedClosure: (() -> ())?) {
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
    
    func onUploading(_ progress: jfloat) {
        self.onUploading?(Double(progress))
    }
}

class AAFileCallback : NSObject, ACFileCallback {
    
    let notDownloaded: (()->())?
    let onDownloading: ((_ progress: Double) -> ())?
    let onDownloaded: ((_ fileName: String) -> ())?
    
    init(notDownloaded: (()->())?, onDownloading: ((_ progress: Double) -> ())?, onDownloaded: ((_ reference: String) -> ())?) {
        self.notDownloaded = notDownloaded;
        self.onDownloading = onDownloading;
        self.onDownloaded = onDownloaded;
    }
    
    init(onDownloaded: @escaping (_ reference: String) -> ()) {
        self.notDownloaded = nil;
        self.onDownloading = nil;
        self.onDownloaded = onDownloaded;
    }
    
    func onNotDownloaded() {
        self.notDownloaded?();
    }
    
    func onDownloading(_ progress: jfloat) {
        self.onDownloading?(Double(progress));
    }
    
    func onDownloaded(_ reference: ARFileSystemReference!) {
        self.onDownloaded?(reference!.getDescriptor());
    }
}


//
// Markdown
//

open class TextParser {
    
    open let textColor: UIColor
    open let linkColor: UIColor
    open let fontSize: CGFloat
    
    fileprivate let markdownParser = ARMarkdownParser(int: ARMarkdownParser_MODE_FULL)
    
    public init(textColor: UIColor, linkColor: UIColor, fontSize: CGFloat) {
        self.textColor = textColor
        self.linkColor = linkColor
        self.fontSize = fontSize
    }
    
    open func parse(_ text: String) -> ParsedText {
        let doc = markdownParser?.processDocument(with: text)
        
        if (doc?.isTrivial())! {
            let nAttrText = NSMutableAttributedString(string: text)
            let range = NSRange(location: 0, length: nAttrText.length)
            nAttrText.yy_setColor(textColor, range: range)
            nAttrText.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
            return ParsedText(attributedText: nAttrText, isTrivial: true, code: [])
        }
        
        var sources = [String]()
        
        let sections: [ARMDSection] = doc!.getSections().toSwiftArray()
        let nAttrText = NSMutableAttributedString()
        var isFirst = true
        for s in sections {
            if !isFirst {
                nAttrText.append(NSAttributedString(string: "\n"))
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
                
                nAttrText.append(str)
                
                sources.append(s.getCode().getCode())
            } else if s.getType() == ARMDSection_TYPE_TEXT {
                let child: [ARMDText] = s.getText().toSwiftArray()
                for c in child {
                    nAttrText.append(buildText(c, fontSize: fontSize))
                }
            } else {
                fatalError("Unsupported section type")
            }
        }
        
        return ParsedText(attributedText: nAttrText, isTrivial: false, code: sources)
    }
    
    fileprivate func buildText(_ text: ARMDText, fontSize: CGFloat) -> NSAttributedString {
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
                res.append(buildText(c, fontSize: fontSize))
            }
            
            // Setting span elements
            if span.getType() == ARMDSpan_TYPE_BOLD {
                res.appendFont(UIFont.boldSystemFont(ofSize: fontSize))
            } else if span.getType() == ARMDSpan_TYPE_ITALIC {
                res.appendFont(UIFont.italicSystemFont(ofSize: fontSize))
            } else {
                fatalError("Unsupported span type")
            }
            
            res.endEditing()
            return res
        } else if let url = text as? ARMDUrl {
            
            // Parsing url element
            let nsUrl = URL(string: url.getUrl())
            if nsUrl != nil {
                let res = NSMutableAttributedString(string:  url.getTitle())
                let range = NSRange(location: 0, length: res.length)
                let highlight = YYTextHighlight()
                highlight.userInfo = ["url" : url.getUrl()]
                res.yy_setTextHighlight(highlight, range: range)
                res.yy_setFont(UIFont.textFontOfSize(fontSize), range: range)
                res.yy_setColor(linkColor, range: range)
                return res
            } else {
                // Unable to parse: show as text
                let res = NSMutableAttributedString(string: url.getTitle())
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

open class ParsedText {
    
    open let isTrivial: Bool
    open let attributedText: NSAttributedString
    open let code: [String]
    
    public init(attributedText: NSAttributedString, isTrivial: Bool, code: [String]) {
        self.attributedText = attributedText
        self.code = code
        self.isTrivial = isTrivial
    }
}

//
// Promises
//

open class AAPromiseFunc: NSObject, ARPromiseFunc {
    
    let closure: (_ resolver: ARPromiseResolver) -> ()
    init(closure: @escaping (_ resolver: ARPromiseResolver) -> ()){
        self.closure = closure
    }
    
    open func exec(_ resolver: ARPromiseResolver) {
        closure(resolver)
    }
}

extension ARPromise {
    convenience init(closure: @escaping (_ resolver: ARPromiseResolver) -> ()) {
        self.init(executor: AAPromiseFunc(closure: closure))
    }
}

//
// Data Binding
//

open class AABinder {
    
    fileprivate var bindings : [BindHolder] = []
    
    public init() {
        
    }
    
    open func bind<T1,T2,T3>(_ valueModel1:ARValue, valueModel2:ARValue, valueModel3:ARValue, closure: @escaping (_ value1:T1?, _ value2:T2?, _ value3:T3?) -> ()) {
        
        let listener1 = BindListener { (_value1) -> () in
            closure(_value1 as? T1, valueModel2.get() as? T2, valueModel3.get() as? T3)
        };
        let listener2 = BindListener { (_value2) -> () in
            closure(valueModel1.get() as? T1, _value2 as? T2, valueModel3.get() as? T3)
        };
        let listener3 = BindListener { (_value3) -> () in
            closure(valueModel1.get() as? T1,  valueModel2.get() as? T2, _value3 as? T3)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        bindings.append(BindHolder(valueModel: valueModel3, listener: listener3))
        valueModel1.subscribe(with: listener1, notify: false)
        valueModel2.subscribe(with: listener2, notify: false)
        valueModel3.subscribe(with: listener3, notify: false)
        closure(valueModel1.get() as? T1, valueModel2.get() as? T2, valueModel3.get() as? T3)
    }
    
    
    open func bind<T1,T2>(_ valueModel1:ARValue, valueModel2:ARValue, closure: @escaping (_ value1:T1?, _ value2:T2?) -> ()) {
        let listener1 = BindListener { (_value1) -> () in
            closure(_value1 as? T1, valueModel2.get() as? T2)
        };
        let listener2 = BindListener { (_value2) -> () in
            closure(valueModel1.get() as? T1, _value2 as? T2)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        valueModel1.subscribe(with: listener1, notify: false)
        valueModel2.subscribe(with: listener2, notify: false)
        closure(valueModel1.get() as? T1, valueModel2.get() as? T2)
    }
    
    open func bind<T>(_ value:ARValue, closure: @escaping (_ value: T?)->()) {
        let listener = BindListener { (value2) -> () in
            closure(value2 as? T)
        };
        let holder = BindHolder(valueModel: value, listener: listener)
        bindings.append(holder)
        value.subscribe(with: listener)
    }
    
    open func unbindAll() {
        for holder in bindings {
            holder.valueModel.unsubscribe(with: holder.listener)
        }
        bindings.removeAll(keepingCapacity: true)
    }
    
}

class BindListener: NSObject, ARValueChangedListener {
    
    var closure: ((_ value: Any?)->())?
    
    init(closure: @escaping (_ value: Any?)->()) {
        self.closure = closure
    }
    
    func onChanged(_ val: Any!, withModel valueModel: ARValue!) {
        closure?(val)
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
