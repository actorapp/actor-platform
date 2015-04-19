//
//  BubbleView.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit;

class AABubbleCell: UITableViewCell {
    
    // MARK: -
    // MARK: Private static vars
    
    // Cached bubble images
    private static var cacnedOutTextBg:UIImage? = nil;
    private static var cacnedOutTextBgBorder:UIImage? = nil;
    private static var cacnedInTextBg:UIImage? = nil;
    private static var cacnedInTextBgBorder:UIImage? = nil;
    
    // MARK: -
    // MARK: Public vars
    
    // Views
    let avatarView = AAAvatarView(frameSize: 39)
    let senderNameLabel = UILabel()
    let bubble = UIImageView()
    let bubbleBorder = UIImageView()
    
    // Layout
    let groupContentInsetY = 20.0
    let groupContentInsetX = 40.0
    var bubbleVerticalSpacing: CGFloat = 6.0
    let bubblePadding: CGFloat = 6;
    let bubbleMediaPadding: CGFloat = 10;
    
    // Binded data
    var bindedMessage: AMMessage? = nil
    var group: Bool = false
    
    // MARK: -
    // MARK: Constructors

    init(reuseId: String){
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseId);
        
        senderNameLabel.font = UIFont.systemFontOfSize(15.0)
        
        bubble.userInteractionEnabled = true
        
        contentView.addSubview(bubble);
        contentView.addSubview(bubbleBorder);
        
        backgroundColor = UIColor.clearColor();
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    
    // MARK: -
    // MARK: Getters

    class func measureHeight(message: AMMessage, group: Bool) -> CGFloat {
        var content = message.getContent()!;
        
        var height : CGFloat
        if (content is AMTextContent) {
            height = AABubbleTextCell.measureTextHeight(message) + AABubbleTextCell.bubbleTopPadding() + AABubbleTextCell.bubbleBottomPadding()
        } else if (content is AMPhotoContent) {
            height = AABubbleMediaCell.measureMediaHeight(message) + AABubbleMediaCell.bubbleTopPadding() + AABubbleMediaCell.bubbleBottomPadding()
        } else if (content is AMVideoContent) {
            height = AABubbleMediaCell.measureMediaHeight(message) + AABubbleMediaCell.bubbleTopPadding() + AABubbleMediaCell.bubbleBottomPadding()
        } else if (content is AMServiceContent) {
            height = AABubbleServiceCell.measureServiceHeight(message) + AABubbleServiceCell.bubbleTopPadding() + AABubbleServiceCell.bubbleBottomPadding()
        } else if (content is AMDocumentContent) {
            height = AABubbleDocumentCell.measureServiceHeight(message) + AABubbleDocumentCell.bubbleTopPadding() + AABubbleDocumentCell.bubbleBottomPadding()
        } else {
            height = AABubbleUnsupportedCell.measureUnsupportedHeight(message) + AABubbleUnsupportedCell.bubbleTopPadding() + AABubbleUnsupportedCell.bubbleBottomPadding()
        }
        
        let isIn = message.getSenderId() != MSG.myUid()
        if group && isIn && !(content is AMServiceContent) && !(content is AMPhotoContent) && !(content is AMVideoContent) {
            height += CGFloat(20.0)
        }
        
        return height
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
    
    func bindBubbleType(type: BubbleType, isCompact: Bool) {
        // TODO: Cache images
        switch(type) {
            case BubbleType.TextIn:
                if (AABubbleCell.cacnedInTextBg == nil) {
                    AABubbleCell.cacnedInTextBg = UIImage(named: "BubbleIncomingFull")?.tintImage(MainAppTheme.bubbles.textBgOut)
                }
                if (AABubbleCell.cacnedInTextBgBorder == nil) {
                    AABubbleCell.cacnedInTextBgBorder = UIImage(named: "BubbleIncomingFullBorder")?.tintImage(MainAppTheme.bubbles.textBgOutBorder)
                }
                
                bubble.image = AABubbleCell.cacnedInTextBg
                bubbleBorder.image = AABubbleCell.cacnedInTextBgBorder
            break
            case BubbleType.TextOut:
                if (AABubbleCell.cacnedOutTextBg == nil) {
                    AABubbleCell.cacnedOutTextBg = UIImage(named: "BubbleOutgoingFull")?.tintImage(MainAppTheme.bubbles.textBgOut)
                }
                if (AABubbleCell.cacnedOutTextBgBorder == nil) {
                    AABubbleCell.cacnedOutTextBgBorder = UIImage(named: "BubbleOutgoingFullBorder")?.tintImage(MainAppTheme.bubbles.textBgOutBorder)
                }
                
                bubble.image =  AABubbleCell.cacnedOutTextBg!
                bubbleBorder.image =  AABubbleCell.cacnedOutTextBgBorder!
            break
            case BubbleType.MediaIn:
            break
            case BubbleType.MediaIn:
            break
            default:
            break
        }
    }
    
    func layoutBubble(frame: CGRect) {
        bubble.frame = frame
        bubbleBorder.frame = frame
    }
    
    func bind(message: AMMessage, reuse: Bool) {
        fatalError("bind(message:) has not been implemented")
    }
    
}

enum BubbleType {
    case TextOut
    case TextIn
    case MediaOut
    case MediaIn
    case Service
}