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
    
    private let titleLabel = UILabel()
    private let sizeLabel = UILabel()
    
    private let dateLabel = UILabel()
    private let statusView = UIImageView()
    
    private var isOut: Bool = false
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue
    
    // MARK: -
    // MARK: Constructors
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        dateLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        dateLabel.lineBreakMode = .ByClipping
        dateLabel.numberOfLines = 1
        dateLabel.contentMode = UIViewContentMode.TopLeft
        dateLabel.textAlignment = NSTextAlignment.Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        titleLabel.font = UIFont.systemFontOfSize(16.0)
        titleLabel.textColor = UIColor.RGB(0x3faa3c)
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.ByTruncatingMiddle
        
        sizeLabel.font = UIFont.systemFontOfSize(13.0)
        sizeLabel.textColor = UIColor.RGB(0x74b56e)
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        contentView.addSubview(bubble)
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(sizeLabel)
        
        contentView.addSubview(dateLabel)
        contentView.addSubview(statusView)
        
        backgroundColor = UIColor.clearColor()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    override func bind(message: AMMessage, reuse: Bool) {
        let document = message.getContent() as! AMDocumentContent
        
        if (!reuse) {
            isOut = message.getSenderId() == MSG.myUid()
            if (isOut) {
                bubble.image =  UIImage(named: "BubbleOutgoingFull")
            } else {
                bubble.image =  UIImage(named: "BubbleIncomingFull")
            }
            
            titleLabel.text = document.getName()
            
            let source = document.getSource()
            let kb = Int(source.getSize()) / 1024
            if kb >= 1024 {
                let mb = kb / 1024
                if mb >= 1024 {
                    let gb = mb / 1024
                    sizeLabel.text = "\(gb)Gb"
                } else {
                    sizeLabel.text = "\(mb)Mb"
                }
            } else {
                sizeLabel.text = "\(kb)Kb"
            }
        }
        
        // Always update date and state
        dateLabel.text = formatDate(message.getDate())
        messageState = UInt(message.getMessageState().ordinal())
    }
    
    // MARK: -
    // MARK: Methods
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage) -> CGFloat {
        return 81
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
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
            var bubbleWidth = CGFloat(201)
            
            if (self.isOut) {
                self.bubble.frame = CGRectMake(contentWidth - bubbleWidth - self.bubblePadding, bubbleTopPadding, bubbleWidth, bubbleHeight)
                
                self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
                self.dateLabel.textColor = self.dateColorOut
                
                self.titleLabel.frame = CGRect(x: self.bubble.frame.minX + 76.0, y: 25.0, width: bubbleWidth - 76.0 - 8.0 - 6.0, height: self.titleLabel.bounds.height)
                self.sizeLabel.frame = CGRect(x: self.bubble.frame.minX + 76.0, y: 47.0, width: self.titleLabel.bounds.width, height: self.sizeLabel.bounds.height)
            } else {
                self.bubble.frame = CGRectMake(self.bubblePadding, bubbleTopPadding, bubbleWidth, bubbleHeight)
                
                self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
                self.dateLabel.textColor = self.dateColorIn
                
                self.titleLabel.frame = CGRect(x: self.bubble.frame.minX + 82.0, y: 25.0, width: bubbleWidth - 82.0 - 8.0, height: self.titleLabel.bounds.height)
                self.sizeLabel.frame = CGRect(x: self.bubble.frame.minX + 82.0, y: 47.0, width: self.titleLabel.bounds.width, height: self.sizeLabel.bounds.height)
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
