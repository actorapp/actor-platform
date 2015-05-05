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
}