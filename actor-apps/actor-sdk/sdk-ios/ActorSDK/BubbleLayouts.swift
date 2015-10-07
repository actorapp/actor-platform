//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

struct CellSetting: Equatable {
    let showDate: Bool
    let clenchTop: Bool
    let clenchBottom: Bool
    
    init(showDate: Bool, clenchTop: Bool, clenchBottom: Bool) {
        self.showDate = showDate
        self.clenchTop = clenchTop
        self.clenchBottom = clenchBottom
    }
    
}

func ==(lhs: CellSetting, rhs: CellSetting) -> Bool {
    return lhs.showDate == rhs.showDate && lhs.clenchTop == rhs.clenchTop && lhs.clenchBottom == rhs.clenchBottom
}

class CellLayout {
    
    let key: String
    var height: CGFloat
    let date: String
    
    init(height: CGFloat, date: Int64, key: String) {
        self.date = CellLayout.formatDate(date)
        self.key = key
        self.height = height
    }
    
    class func formatDate(date: Int64) -> String {
        let dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "HH:mm"
        return dateFormatter.stringFromDate(NSDate(timeIntervalSince1970: NSTimeInterval(Double(date) / 1000.0)))
    }
}