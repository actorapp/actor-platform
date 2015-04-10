//
//  AABubbleDocumentCell.swift
//  ActorApp
//
//  Created by Danil Gontovnik on 4/10/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AABubbleDocumentCell: BubbleCell {
    
    // MARK: -
    // MARK: Private vars
    
    private let bubble = UIImageView()
    private let dateText = UILabel()
    private let statusView = UIImageView()
    
    private var isOut: Bool = false
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue
    
    // MARK: -
    // MARK: Constructors
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        dateText.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        dateText.lineBreakMode = .ByClipping
        dateText.numberOfLines = 1
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.textAlignment = NSTextAlignment.Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        contentView.addSubview(bubble)
        contentView.addSubview(dateText)
        contentView.addSubview(statusView)
        
        backgroundColor = UIColor.clearColor()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    override func bind(message: AMMessage, reuse: Bool) {
        if (!reuse) {
            isOut = message.getSenderId() == MSG.myUid()
            if (isOut) {
                bubble.image =  UIImage(named: "BubbleOutgoingFull")
            } else {
                bubble.image =  UIImage(named: "BubbleIncomingFull")
            }
        }
        
        // Always update date and state
        dateText.text = formatDate(message.getDate())
        messageState = UInt(message.getMessageState().ordinal())
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage) -> CGFloat {
        return 91
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 3
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 3
    }
    
    // MARK: -
    // MARK: Layout
    
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        UIView.performWithoutAnimation { () -> Void in
            
            var bubbleTopPadding = AABubbleDocumentCell.bubbleTopPadding()
            var bubbleBottomPadding = AABubbleDocumentCell.bubbleBottomPadding()
            
            var contentWidth = self.contentView.frame.width
            var contentHeight = self.contentView.frame.height
            
            var bubbleHeight = contentHeight - bubbleTopPadding - bubbleBottomPadding
            var bubbleWidth = CGFloat(221)
            
            if (self.isOut) {
                self.bubble.frame = CGRectMake(contentWidth - bubbleWidth - self.bubblePadding, bubbleTopPadding, bubbleWidth, bubbleHeight)
                
                self.dateText.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
                self.dateText.textColor = self.dateColorOut
            } else {
                self.bubble.frame = CGRectMake(self.bubblePadding, bubbleTopPadding, bubbleWidth, bubbleHeight)
                
                self.dateText.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
                self.dateText.textColor = self.dateColorIn
            }
            
            
            if (self.isOut) {
                self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
                self.statusView.hidden = false
                
                switch(self.messageState) {
                case AMMessageState.UNKNOWN.rawValue:
                    self.statusView.image = Resources.iconClock
                    self.statusView.tintColor = self.statusPassive
                case AMMessageState.PENDING.rawValue:
                    self.statusView.image = Resources.iconClock
                    self.statusView.tintColor = self.statusPassive
                    break
                case AMMessageState.SENT.rawValue:
                    self.statusView.image = Resources.iconCheck1
                    self.statusView.tintColor = self.statusPassive
                    break
                case AMMessageState.RECEIVED.rawValue:
                    self.statusView.image = Resources.iconCheck2
                    self.statusView.tintColor = self.statusPassive
                    break
                case AMMessageState.READ.rawValue:
                    self.statusView.image = Resources.iconCheck2
                    self.statusView.tintColor = self.statusActive
                    break
                default:
                    self.statusView.image = Resources.iconClock
                    self.statusView.tintColor = self.statusPassive
                    break
                }
            } else {
                self.statusView.hidden = true
            }
        }
    }

}
