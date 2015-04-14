//
//  AAPlaceholderView.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/4/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAPlaceholderView: UIView {
    
    // MARK: -
    // MARK: Private vars
    
    private var contentView: UIView!
    private var bgView: UIView!
    private var imageView: UIImageView!
    private var titleLabel: UILabel!
    private var subtitleLabel: UILabel!
    private var actionButton: UIButton!
    private var topOffset: CGFloat!
    
    // MARK: -
    // MARK: Public vars

    // MARK: -
    // MARK: Constructors
    
    init(topOffset: CGFloat!) {
        super.init(frame: CGRectZero)
        
        self.topOffset = topOffset
        
        backgroundColor = UIColor.whiteColor()
        
        contentView = UIView()
        contentView.backgroundColor = UIColor.whiteColor()
        addSubview(contentView)
        
        imageView = UIImageView()
        bgView = UIView()
        bgView.backgroundColor = MainAppTheme.navigation.barColor// UIColor.RGB(0x5289c4)
        contentView.addSubview(bgView)
        contentView.addSubview(imageView)
        
        titleLabel = UILabel()
        titleLabel.textColor = MainAppTheme.navigation.barColor
        titleLabel.font = UIFont(name: "HelveticaNeue", size: 22.0);
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.text = " "
        titleLabel.sizeToFit()
        contentView.addSubview(titleLabel)
        
        subtitleLabel = UILabel()
        subtitleLabel.textColor = Resources.HintText
        subtitleLabel.font = UIFont.systemFontOfSize(16.0)
        subtitleLabel.textAlignment = NSTextAlignment.Center
        subtitleLabel.numberOfLines = 0
        
        contentView.addSubview(subtitleLabel)
        
        actionButton = UIButton.buttonWithType(UIButtonType.System) as! UIButton
        actionButton.titleLabel!.font = UIFont(name: "HelveticaNeue-Medium", size: 21.0)
        contentView.addSubview(actionButton)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setImage(image: UIImage?, title: String?, subtitle: String?) {
        setImage(image, title: title, subtitle: subtitle, actionTitle: nil, actionTarget: nil, actionSelector: nil)
    }
    
    func setImage(image: UIImage?, title: String?, subtitle: String?, actionTitle: String?, actionTarget: AnyObject?, actionSelector: Selector?) {
        
        if image != nil {
            imageView.image = image!
            imageView.hidden = false
        } else {
            imageView.hidden = true
        }
        
        if title != nil {
            titleLabel.text = title
            titleLabel.hidden = false
        } else {
            titleLabel.hidden = true
        }
        
        if subtitle != nil {
            
            var paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.lineHeightMultiple = 1.11
            paragraphStyle.alignment = NSTextAlignment.Center
            
            var attrString = NSMutableAttributedString(string: subtitle!)
            attrString.addAttribute(NSParagraphStyleAttributeName, value:paragraphStyle, range:NSMakeRange(0, attrString.length))

            subtitleLabel.attributedText = attrString
            
            subtitleLabel.hidden = false
        } else {
            subtitleLabel.hidden = true
        }

        if actionTitle != nil && actionTarget != nil && actionSelector != nil {
            actionButton.removeTarget(nil, action: nil, forControlEvents: UIControlEvents.AllEvents)
            actionButton.addTarget(actionTarget!, action: actionSelector!, forControlEvents: UIControlEvents.TouchUpInside)
            actionButton.setTitle(actionTitle, forState: UIControlState.Normal)
            actionButton.hidden = false
        } else {
            actionButton.hidden = true
        }
        
        setNeedsLayout()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var contentHeight: CGFloat = 0
        var maxContentWidth = bounds.size.width - 40
        var originY = 0
        
        if imageView.hidden == false {
            imageView.frame = CGRect(x: 20 + (maxContentWidth - imageView.image!.size.width) / 2.0, y: topOffset, width: imageView.image!.size.width, height: imageView.image!.size.height)
            
            contentHeight += imageView.image!.size.height + topOffset
        }
        
        bgView.frame = CGRect(x: 0, y: 0, width: bounds.size.width, height: imageView.frame.height * 0.75 + topOffset)
        
        if titleLabel.hidden == false {
            if contentHeight > 0 {
                contentHeight += 10
            }
            
            titleLabel.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: titleLabel.bounds.size.height)
            contentHeight += titleLabel.bounds.size.height
        }
        
        if subtitleLabel.hidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let subtitleLabelSize = subtitleLabel.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            subtitleLabel.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: subtitleLabelSize.height)
            contentHeight += subtitleLabelSize.height
        }
        
        if actionButton.hidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let actionButtonTitleLabelSize = actionButton.titleLabel!.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            actionButton.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: actionButtonTitleLabelSize.height)
            contentHeight += actionButtonTitleLabelSize.height
        }
        
//        contentView.frame = CGRect(x: (bounds.size.width - maxContentWidth) / 2.0, y: (bounds.size.height - contentHeight) / 2.0, width: maxContentWidth, height: contentHeight)

        contentView.frame = CGRect(x: 0, y: 0, width: maxContentWidth, height: contentHeight)
    }

}
