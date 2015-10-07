//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class Regex {
    
    let internalExpression: NSRegularExpression
    let pattern: String
    
    public init(_ pattern: String) {
        self.pattern = pattern
        do {
            self.internalExpression = try NSRegularExpression(pattern: pattern, options: .CaseInsensitive)
        } catch {
            fatalError("Incorrect regex: \(pattern)")
        }
    }
    
    public func test(input: String) -> Bool {
        let matches = self.internalExpression.matchesInString(input, options: NSMatchingOptions(), range:NSMakeRange(0, input.length))
        return matches.count > 0
    }
}