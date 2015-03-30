//
//  BubbleView.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit;

class BubbleCell: UITableViewCell {

    class func measureHeight(message: AMMessage) -> CGFloat {
        var content = message.getContent()!;
        if (content is AMTextContent){
            return BubbleTextCell.measureTextHeight(message)
        } else if (content is AMPhotoContent) {
            return BubbleMediaCell.measureMediaHeight(message)
        } else if (content is AMVideoContent) {
            return BubbleMediaCell.measureMediaHeight(message)
        } else if (content is AMServiceContent){
            return BubbleServiceCell.measureServiceHeight(message);
        } else {
            return BubbleUnsupportedCell.measureUnsupportedHeight(message)
        }
    }
    
    let bubblePadding:CGFloat = 6;
    var bindedMessage: AMMessage? = nil
    
    init(reuseId: String){
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseId);
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func performBind(message: AMMessage) {
        var reuse = false
        if (bindedMessage != nil && bindedMessage?.getRid() == message.getRid()) {
            reuse = true
        }
        bindedMessage = message
        bind(message, reuse: reuse)
    }
    
    func bind(message: AMMessage, reuse: Bool){
        fatalError("bind(message:) has not been implemented")
    }
    
    func formatDate(date:Int64) -> String {
        var dateFormatter = NSDateFormatter();
        dateFormatter.dateFormat = "HH:mm";
        return dateFormatter.stringFromDate(NSDate(timeIntervalSince1970: NSTimeInterval(Double(date) / 1000.0)));
    }
}

