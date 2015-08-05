//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

extension String {
    
    func trim() -> String {
        return stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceCharacterSet());
    }
    
    func size() -> Int {
        return count(self);
    }
    
    subscript (i: Int) -> Character {
        return self[advance(self.startIndex, i)]
    }
    
    subscript (i: Int) -> String {
        return String(self[i] as Character)
    }
    
    func first(count: Int) -> String {
        let realCount = min(count, size());
        return substringToIndex(advance(startIndex, realCount));
    }
    
    func strip(set: NSCharacterSet) -> String {
        return "".join(componentsSeparatedByCharactersInSet(set))
    }
    
    func toLong() -> Int64? {
        return NSNumberFormatter().numberFromString(self)?.longLongValue
    }
    
    func smallValue() -> String {
        let trimmed = trim();
        if (trimmed.isEmpty){
            return "#";
        }
        let letters = NSCharacterSet.letterCharacterSet()
        var res: String = self[0];
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
    
    func rangesOfString(text: String) -> [Range<String.Index>] {
        var res = [Range<String.Index>]()
        
        var searchRange = Range<String.Index>(start: self.startIndex, end: self.endIndex)
        while true {
            var found = self.rangeOfString(text, options: NSStringCompareOptions.CaseInsensitiveSearch, range: searchRange, locale: nil)
            if found != nil {
                res.append(found!)
                searchRange = Range<String.Index>(start: found!.endIndex, end: self.endIndex)
            } else {
                break
            }
        }
        
        return res
    }
}





