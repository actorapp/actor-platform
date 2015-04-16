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
    
    // MARK: -
    // MARK: Public vars
    
    let bubble = UIImageView()
    let statusActive = UIColor(red: 52/255.0, green: 151/255.0, blue: 249/255.0, alpha: 1.0);
    let statusPassive = UIColor(red: 0/255.0, green: 0/255.0, blue: 0/255.0, alpha: 0.27);
    
    //    let dateColorOut = UIColor(red: 45/255.0, green: 163/255.0, blue: 47/255.0, alpha: 1.0);
    let dateColorOut = UIColor(red: 0/255.0, green: 0/255.0, blue: 0/255.0, alpha: 0.27);
    let dateColorIn = UIColor(red: 151/255.0, green: 151/255.0, blue: 151/255.0, alpha: 1.0);
    
    let messageTextColor = UIColor(red: 20/255.0, green: 22/255.0, blue: 23/255.0, alpha: 1.0);
    
    let bubblePadding: CGFloat = 6;
    let bubbleMediaPadding: CGFloat = 10;
    var bindedMessage: AMMessage? = nil
    
    var bubbleVerticalSpacing: CGFloat = 6.0
    
    // MARK: -
    // MARK: Constructors

    init(reuseId: String){
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseId);
        
        bubble.userInteractionEnabled = true
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    
    // MARK: -
    // MARK: Getters

    class func measureHeight(message: AMMessage) -> CGFloat {
        var content = message.getContent()!;
        if (content is AMTextContent) {
            return BubbleTextCell.measureTextHeight(message) + BubbleTextCell.bubbleTopPadding() + BubbleTextCell.bubbleBottomPadding()
        } else if (content is AMPhotoContent) {
            return BubbleMediaCell.measureMediaHeight(message) + BubbleMediaCell.bubbleTopPadding() + BubbleMediaCell.bubbleBottomPadding()
        } else if (content is AMVideoContent) {
            return BubbleMediaCell.measureMediaHeight(message) + BubbleMediaCell.bubbleTopPadding() + BubbleMediaCell.bubbleBottomPadding()
        } else if (content is AMServiceContent) {
            return BubbleServiceCell.measureServiceHeight(message) + BubbleServiceCell.bubbleTopPadding() + BubbleServiceCell.bubbleBottomPadding()
        } else if (content is AMDocumentContent) {
            return AABubbleDocumentCell.measureServiceHeight(message) + AABubbleDocumentCell.bubbleTopPadding() + AABubbleDocumentCell.bubbleBottomPadding()
        } else {
            return AABubbleUnsupportedCell.measureUnsupportedHeight(message) + AABubbleUnsupportedCell.bubbleTopPadding() + AABubbleUnsupportedCell.bubbleBottomPadding()
        }
    }
    
    func formatDate(date: Int64) -> String {
        var dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "HH:mm"
        return dateFormatter.stringFromDate(NSDate(timeIntervalSince1970: NSTimeInterval(Double(date) / 1000.0)))
    }
    
    // MARK: -
    // MARK: MenuController
    
    override func canBecomeFirstResponder() -> Bool {
        return true
    }
    
    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        return false
    }
    
//    - (void)copy:(id)sender {
//    [[UIPasteboard generalPasteboard] setString:self.text];
//    }
    
    // MARK: -
    // MARK: Bind
    
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
    
}

