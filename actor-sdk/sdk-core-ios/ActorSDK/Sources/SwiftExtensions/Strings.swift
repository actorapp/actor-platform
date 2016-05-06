//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import j2objc

public extension String {
    
    public var isEmpty: Bool { return self.characters.isEmpty }
    
    public var length: Int { return self.characters.count }
    
    public func indexOf(str: String) -> Int? {
        if let range = rangeOfString(str) {
            return startIndex.distanceTo(range.startIndex)
        } else {
            return nil
        }
    }
    
    public func trim() -> String {
        return stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceCharacterSet());
    }
    
    public subscript (i: Int) -> Character {
        return self[self.startIndex.advancedBy(i)]
    }
    
    public subscript (i: Int) -> String {
        return String(self[i] as Character)
    }
    
    public func first(count: Int) -> String {
        let realCount = min(count, length);
        return substringToIndex(startIndex.advancedBy(realCount));
    }
    
    public func skip(count: Int) -> String {
        let realCount = min(count, length);
        return substringFromIndex(startIndex.advancedBy(realCount))
    }
    
    
    public func strip(set: NSCharacterSet) -> String {
        return componentsSeparatedByCharactersInSet(set).joinWithSeparator("")
    }
    
    public func replace(src: String, dest:String) -> String {
        return stringByReplacingOccurrencesOfString(src, withString: dest, options: NSStringCompareOptions(), range: nil)
    }
    
    public func toLong() -> Int64? {
        return NSNumberFormatter().numberFromString(self)?.longLongValue
    }
    
    public func toJLong() -> jlong {
        return jlong(toLong()!)
    }
    
    public func smallValue() -> String {
        let trimmed = trim();
        if (trimmed.isEmpty){
            return "#";
        }
        let letters = NSCharacterSet.letterCharacterSet()
        let res: String = self[0];
        if (res.rangeOfCharacterFromSet(letters) != nil) {
            return res.uppercaseString;
        } else {
            return "#";
        }
    }
    
    public func hasPrefixInWords(prefix: String) -> Bool {
        var components = self.componentsSeparatedByString(" ")
        for i in 0..<components.count {
            if components[i].lowercaseString.hasPrefix(prefix.lowercaseString) {
                return true
            }
        }
        return false
    }
    
    public func contains(text: String) -> Bool {
        return self.rangeOfString(text, options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil, locale: nil) != nil
    }
    
    public func startsWith(text: String) -> Bool {
        let range = rangeOfString(text)
        if range != nil {
            return range!.startIndex == startIndex
        }
        return false
    }
    
    public func rangesOfString(text: String) -> [Range<String.Index>] {
        var res = [Range<String.Index>]()
        
        var searchRange = Range<String.Index>(start: self.startIndex, end: self.endIndex)
        while true {
            let found = self.rangeOfString(text, options: NSStringCompareOptions.CaseInsensitiveSearch, range: searchRange, locale: nil)
            if found != nil {
                res.append(found!)
                searchRange = Range<String.Index>(start: found!.endIndex, end: self.endIndex)
            } else {
                break
            }
        }
        
        return res
    }
    
    public func repeatString(count: Int) -> String {
        var res = ""
        for _ in 0..<count {
            res += self
        }
        return res
    }
        
    public func isValidUrl () -> Bool {
            if let url = NSURL(string: self) {
                return UIApplication.sharedApplication().canOpenURL(url)
            }
        return false
    }

    public var ns: NSString {
        return self as NSString
    }
    public var pathExtension: String? {
        return ns.pathExtension
    }
    public var lastPathComponent: String? {
        return ns.lastPathComponent
    }
    
    public var asNS: NSString { return (self as NSString) }
}

public extension NSAttributedString {
    
    public func append(text: NSAttributedString) -> NSAttributedString {
        let res = NSMutableAttributedString()
        res.appendAttributedString(self)
        res.appendAttributedString(text)
        return res
    }
    
    public func append(text: String, font: UIFont) -> NSAttributedString {
        return append(NSAttributedString(string: text, attributes: [NSFontAttributeName: font]))
    }
    
    public convenience init(string: String, font: UIFont) {
        self.init(string: string, attributes: [NSFontAttributeName: font])
    }
}

public extension NSMutableAttributedString {
    
    public func appendFont(font: UIFont) {
        self.addAttribute(NSFontAttributeName, value: font, range: NSMakeRange(0, self.length))
    }
    
    public func appendColor(color: UIColor) {
        self.addAttribute(NSForegroundColorAttributeName, value: color.CGColor, range: NSMakeRange(0, self.length))
    }
}


