//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public struct AACellSetting: Equatable {
    let showDate: Bool
    let clenchTop: Bool
    let clenchBottom: Bool
    
    init(showDate: Bool, clenchTop: Bool, clenchBottom: Bool) {
        self.showDate = showDate
        self.clenchTop = clenchTop
        self.clenchBottom = clenchBottom
    }
    
}

public func ==(lhs: AACellSetting, rhs: AACellSetting) -> Bool {
    return lhs.showDate == rhs.showDate && lhs.clenchTop == rhs.clenchTop && lhs.clenchBottom == rhs.clenchBottom
}

private let dateFormatter = NSDateFormatter().initDateFormatter()

public class AACellLayout {
    
    let layouter: AABubbleLayouter
    let key: String
    var height: CGFloat
    let date: String
    
    public init(height: CGFloat, date: Int64, key: String, layouter: AABubbleLayouter) {
        self.date = AACellLayout.formatDate(date)
        self.key = key
        self.height = height
        self.layouter = layouter
    }
    
    public class func formatDate(date: Int64) -> String {
        return dateFormatter.stringFromDate(NSDate(timeIntervalSince1970: NSTimeInterval(Double(date) / 1000.0)))
    }
}

extension NSDateFormatter {
    func initDateFormatter() -> NSDateFormatter {
        dateFormat = "HH:mm"
        return self
    }
}