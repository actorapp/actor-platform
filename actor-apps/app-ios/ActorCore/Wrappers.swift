//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaCallback: NSObject, ACCommandCallback {
    
    var resultClosure: ((val: AnyObject!) -> ())?;
    var errorClosure: ((val:JavaLangException!) -> ())?;
    
    init<T>(result: ((val:T?) -> ())?, error: ((val:JavaLangException!) -> ())?) {
        super.init()
        self.resultClosure = { (val: AnyObject!) -> () in
            result?(val: val as? T)
        }
        self.errorClosure = error
    }
    
    func onResult(res: AnyObject!) {
        resultClosure?(val: res)
    }
    
    func onError(e: JavaLangException!) {
        errorClosure?(val: e)
    }
}

class CocoaDownloadCallback : NSObject, ACFileCallback {
    
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

class CocoaUploadCallback : NSObject, ACUploadFileCallback {
    
    let notUploaded: (()->())?
    let onUploading: ((progress: Double) -> ())?
    let onUploadedClosure: (() -> ())?
    
    init(notUploaded: (()->())?, onUploading: ((progress: Double) -> ())?, onUploadedClosure: (() -> ())?) {
        self.onUploading = onUploading
        self.notUploaded = notUploaded
        self.onUploadedClosure = onUploadedClosure;
    }
    
    func onNotUploading() {
        self.notUploaded?();
    }
    
    func onUploaded() {
        self.onUploadedClosure?()
    }
    
    func onUploading(progress: jfloat) {
        self.onUploading?(progress: Double(progress))
    }
}

class MDFormattedText {
    
    let isTrivial: Bool
    let attributedText: NSAttributedString
    let code: [String]
    
    init(attributedText: NSAttributedString, isTrivial: Bool, code: [String]) {
        self.attributedText = attributedText
        self.code = code
        self.isTrivial = isTrivial
    }
}

extension ARMarkdownParser {
    
    func parse(text: String, textColor: UIColor, fontSize: CGFloat) -> MDFormattedText {
        
        let doc = self.processDocumentWithNSString(text)
        if doc.isTrivial() {
           return MDFormattedText(attributedText: NSAttributedString(string: text), isTrivial: true, code: [])
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
                let attributes = [NSLinkAttributeName: NSURL(string: "source:///\(sources.count)") as! AnyObject,
                    NSFontAttributeName: UIFont.textFontOfSize(fontSize)]
                nAttrText.appendAttributedString(NSAttributedString(string: "Open Code", attributes: attributes))
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
        
        nAttrText.appendColor(textColor)
        
        return MDFormattedText(attributedText: nAttrText, isTrivial: false, code: sources)
    }
    
    private func buildText(text: ARMDText, fontSize: CGFloat) -> NSAttributedString {
        if let raw = text as? ARMDRawText {
            return NSAttributedString(string: raw.getRawText(), font: UIFont.textFontOfSize(fontSize))
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
                let attributes = [NSLinkAttributeName: nsUrl as! AnyObject,
                    NSFontAttributeName: UIFont.textFontOfSize(fontSize)]
                return NSAttributedString(string: url.getUrlTitle(), attributes: attributes)
            } else {
                // Unable to parse: show as text
                return NSAttributedString(string: url.getUrlTitle(), font: UIFont.textFontOfSize(fontSize))
            }
        } else {
            fatalError("Unsupported text type")
        }
    }
}



