
//
//  BubbleTextCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

// Using padding for proper date align.
// One space + 16 non-breakable spases for out messages
private let stringOutPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";

// One space + 6 non-breakable spaces for in messages
private let stringInPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";

//private let bubbleFont = UIFont(name: "Roboto", size: 16)!
private let bubbleFont = UIFont(name: "HelveticaNeue", size: 16)!

private let maxTextWidth = 240

private func measureText(message: String, isOut: Bool) -> CGRect {
    var style = NSMutableParagraphStyle();
    style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
    
    var text = (message + (isOut ? stringOutPadding : stringInPadding)) as NSString;
    
    var size = CGSize(width: maxTextWidth, height: 0);
    var rect = text.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: bubbleFont, NSParagraphStyleAttributeName: style], context: nil);
    return CGRectMake(0, 0, round(rect.width), round(rect.height))
}

class BubbleTextCell : BubbleCell {
    
    class func measureTextHeight(message: AMMessage) -> CGFloat {
        var content = message.getContent() as! AMTextContent!;
        return round(measureText(content.getText(), message.getSenderId() == MSG.myUid()).height) + 14
    }
    
    let textPaddingStart:CGFloat = 10.0;
    let textPaddingEnd:CGFloat = 8.0;
    let datePaddingOut:CGFloat = 66.0;
    let datePaddingIn:CGFloat = 20.0;
    
//    let dateColorOut = UIColor(red: 45/255.0, green: 163/255.0, blue: 47/255.0, alpha: 1.0);
    let dateColorOut = UIColor(red: 0/255.0, green: 0/255.0, blue: 0/255.0, alpha: 0.27);
    
    let dateColorIn = UIColor(red: 151/255.0, green: 151/255.0, blue: 151/255.0, alpha: 1.0);
    let messageTextColor = UIColor(red: 20/255.0, green: 22/255.0, blue: 23/255.0, alpha: 1.0);
    
    let statusActive = UIColor(red: 52/255.0, green: 151/255.0, blue: 249/255.0, alpha: 1.0);
    
    let statusPassive = UIColor(red: 0/255.0, green: 0/255.0, blue: 0/255.0, alpha: 0.27);
    
    let bubble = UIImageView();
    let messageText = UILabel();
    let dateText = UILabel();
    let statusView = UIImageView();
    var isOut:Bool = false;
    var messageState: UInt = AMMessageState.UNKNOWN.rawValue;
    var needRelayout = true
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        messageText.font = bubbleFont;
        messageText.lineBreakMode = .ByWordWrapping;
        messageText.numberOfLines = 0;
        messageText.textColor = messageTextColor;
        
        dateText.font = UIFont(name: "HelveticaNeue-Italic", size: 11);
        dateText.lineBreakMode = .ByClipping;
        dateText.numberOfLines = 1;
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.textAlignment = NSTextAlignment.Right;
        
        statusView.contentMode = UIViewContentMode.Center;
        
        contentView.addSubview(bubble);
        contentView.addSubview(messageText);
        contentView.addSubview(dateText);
        contentView.addSubview(statusView);
        
        self.backgroundColor = UIColor.clearColor();
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool) {
        if (!reuse) {
            needRelayout = true
            messageText.text = (message.getContent() as! AMTextContent).getText();
            isOut = message.getSenderId() == MSG.myUid();
            if (isOut) {
                bubble.image =  UIImage(named: "BubbleOutgoingFull");
            } else {
                bubble.image =  UIImage(named: "BubbleIncomingFull");
            }
        }
        
        // Always update date and state
        dateText.text = formatDate(message.getDate());
        messageState = UInt(message.getMessageState().ordinal());
    }
    
    override func layoutSubviews() {
        super.layoutSubviews();

        UIView.performWithoutAnimation { () -> Void in
            
            var realRect = measureText(self.messageText.text!, self.isOut);
            
            self.messageText.frame = realRect;
            self.messageText.sizeToFit()
            
            var w = round(realRect.width);
            var h = round(realRect.height);
            
            if (self.isOut) {
                self.messageText.frame.origin = CGPoint(x: self.frame.width - w - self.textPaddingEnd - self.bubblePadding, y: 8);
                self.dateText.textColor = self.dateColorOut;
            } else {
                self.messageText.frame.origin = CGPoint(x: self.bubblePadding + self.textPaddingStart, y: 8)
                self.dateText.textColor = self.dateColorIn;
            }
            
            let x = round(self.messageText.frame.origin.x);
            let y = round(self.messageText.frame.origin.y);
            
            if (self.isOut) {
                self.bubble.frame = CGRectMake(x - self.textPaddingEnd, y - 4, w + self.textPaddingStart+self.textPaddingEnd, h + 8);
                self.dateText.frame = CGRectMake(x + w - 68, self.bubble.frame.maxY - 24, 46, 26);
            } else {
                self.bubble.frame = CGRectMake(x - self.textPaddingStart, y - 4, w + self.textPaddingStart+self.textPaddingEnd + self.datePaddingIn, h + 8);
                self.dateText.frame = CGRectMake(x + w - 32, self.bubble.frame.maxY - 24, 46, 26);
            }
            
            
            if (self.isOut) {
                self.statusView.frame = CGRectMake(x + w - 22, y + h - 20, 20, 26);
                self.statusView.hidden = false;
                
                switch(self.messageState) {
                    case AMMessageState.UNKNOWN.rawValue:
                        self.statusView.image = Resources.iconClock;
                        self.statusView.tintColor = self.statusPassive;
                    case AMMessageState.PENDING.rawValue:
                        self.statusView.image = Resources.iconClock;
                        self.statusView.tintColor = self.statusPassive;
                        break;
                    case AMMessageState.SENT.rawValue:
                        self.statusView.image = Resources.iconCheck1;
                        self.statusView.tintColor = self.statusPassive;
                        break;
                    case AMMessageState.RECEIVED.rawValue:
                        self.statusView.image = Resources.iconCheck2;
                        self.statusView.tintColor = self.statusPassive;
                        break;
                    case AMMessageState.READ.rawValue:
                        self.statusView.image = Resources.iconCheck2;
                        self.statusView.tintColor = self.statusActive;
                        break;
                    default:
                        self.statusView.image = Resources.iconClock;
                        self.statusView.tintColor = self.statusPassive;
                        break;
                }
            } else {
                self.statusView.hidden = true;
            }
        }
    }
}