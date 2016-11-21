//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AARegex {
    
    let internalExpression: NSRegularExpression
    let pattern: String
    
    init(_ pattern: String) {
        self.pattern = pattern
        do {
            self.internalExpression = try NSRegularExpression(pattern: pattern, options: .caseInsensitive)
        } catch {
            fatalError("Incorrect regex: \(pattern)")
        }
    }
    
    func test(_ input: String) -> Bool {
        let matches = self.internalExpression.matches(in: input, options: NSRegularExpression.MatchingOptions(), range:NSMakeRange(0, input.length))
        return matches.count > 0
    }
}
