//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension String {
    
    var length: Int { return self.characters.count }
    
    func trim() -> String {
        return stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceCharacterSet());
    }
    
    subscript (i: Int) -> Character {
        return self[self.startIndex.advancedBy(i)]
    }
    
    subscript (i: Int) -> String {
        return String(self[i] as Character)
    }
    
    func first(count: Int) -> String {
        let realCount = min(count, length);
        return substringToIndex(startIndex.advancedBy(realCount));
    }
    
    func skip(count: Int) -> String {
        let realCount = min(count, length);
        return substringFromIndex(startIndex.advancedBy(realCount))
    }
    
    func strip(set: NSCharacterSet) -> String {
        return componentsSeparatedByCharactersInSet(set).joinWithSeparator("")
    }
    
    func replace(src: String, dest:String) -> String {
        return stringByReplacingOccurrencesOfString(src, withString: dest, options: NSStringCompareOptions(), range: nil)
    }
    
    func toLong() -> Int64? {
        return NSNumberFormatter().numberFromString(self)?.longLongValue
    }
    
    func toJLong() -> jlong {
        return jlong(toLong()!)
    }
    
    func smallValue() -> String {
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
    
    func hasPrefixInWords(prefix: String) -> Bool {
        var components = self.componentsSeparatedByString(" ")
        for i in 0..<components.count {
            if components[i].lowercaseString.hasPrefix(prefix.lowercaseString) {
                return true
            }
        }
        return false
    }
    
    func contains(text: String) -> Bool {
        return self.rangeOfString(text, options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil, locale: nil) != nil
    }
    
    func startsWith(text: String) -> Bool {
        var range = rangeOfString(text)
        if range != nil {
            return range!.startIndex == startIndex
        }
        return false
    }
    
    func rangesOfString(text: String) -> [Range<String.Index>] {
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
    
    func repeatString(count: Int) -> String {
        var res = ""
        for _ in 0..<count {
            res += self
        }
        return res
    }
    
    var asNS: NSString { return (self as NSString) }
    
    func encodeText(key: Int32) -> String {
        var res = ""
        for i in 0..<length {
            res += String(
                Character(
                UnicodeScalar(
                    UInt32(
                        Int32(
                            (self[i] as String).unicodeScalars.first!.value) + key
                    ))
                )
            )
        }
        return res
    }
}

extension NSAttributedString {
    
    func append(text: NSAttributedString) -> NSAttributedString {
        let res = NSMutableAttributedString()
        res.appendAttributedString(self)
        res.appendAttributedString(text)
        return res
    }
    
    func append(text: String, font: UIFont) -> NSAttributedString {
        return append(NSAttributedString(string: text, attributes: [NSFontAttributeName: font]))
    }
    
    convenience init(string: String, font: UIFont) {
        self.init(string: string, attributes: [NSFontAttributeName: font])
    }
}

extension NSMutableAttributedString {
    
    func appendFont(font: UIFont) {
        self.addAttribute(NSFontAttributeName, value: font, range: NSMakeRange(0, self.length))
    }
    
    func appendColor(color: UIColor) {
        self.addAttribute(NSForegroundColorAttributeName, value: color.CGColor, range: NSMakeRange(0, self.length))
    }
}


