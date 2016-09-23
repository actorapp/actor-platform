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

private let dateFormatter = DateFormatter().initDateFormatter()

open class AACellLayout {
    
    let layouter: AABubbleLayouter
    let key: String
    var height: CGFloat
    let date: String
    let anchorDate: String
    
    public init(height: CGFloat, date: Int64, key: String, layouter: AABubbleLayouter) {
        self.date = AACellLayout.formatDate(date)
        self.anchorDate = Actor.getFormatter().formatDate(date)
        self.key = key
        self.height = height
        self.layouter = layouter
    }
    
    open class func formatDate(_ date: Int64) -> String {
        return dateFormatter.string(from: Date(timeIntervalSince1970: TimeInterval(Double(date) / 1000.0)))
    }
    
    open class func pickApproriateSize(_ width: CGFloat, height: CGFloat) -> CGSize {
        let maxW: CGFloat
        let maxH: CGFloat
        if AADevice.isiPad {
            maxW = 240
            maxH = 340
        } else {
            if AADevice.isiPhone5 || AADevice.isiPhone4 {
                maxW = 220
                maxH = 260
            } else {
                maxW = 240
                maxH = 340
            }
        }
        let scaleW = maxW / width
        let scaleH = maxH / height
        let scale = min(scaleW, scaleH)
        return CGSize(width: scale * width, height: scale * height)
    }
}

extension DateFormatter {
    func initDateFormatter() -> DateFormatter {
        dateFormat = "HH:mm"
        return self
    }
}
