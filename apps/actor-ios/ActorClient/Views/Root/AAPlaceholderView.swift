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
    private var imageView: UIImageView!
    private var titleLabel: UILabel!
    private var subtitleLabel: UILabel!
    private var actionButton: UIButton!
    
    // MARK: -
    // MARK: Public vars

    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(frame: CGRectZero)
        
        backgroundColor = UIColor.whiteColor()
        
        contentView = UIView()
        contentView.backgroundColor = UIColor.whiteColor()
        addSubview(contentView)
        
        imageView = UIImageView()
        contentView.addSubview(imageView)
        
        titleLabel = UILabel()
        titleLabel.textColor = Resources.PlaceholderText
        titleLabel.font = UIFont.systemFontOfSize(22.0)
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.text = " "
        titleLabel.sizeToFit()
        contentView.addSubview(titleLabel)
        
        subtitleLabel = UILabel()
        subtitleLabel.textColor = Resources.PlaceholderText
        subtitleLabel.font = UIFont.systemFontOfSize(14.0)
        subtitleLabel.textAlignment = NSTextAlignment.Center
        subtitleLabel.numberOfLines = 0
        contentView.addSubview(subtitleLabel)
        
        actionButton = UIButton.buttonWithType(UIButtonType.System) as! UIButton
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
            subtitleLabel.text = subtitle
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
            imageView.frame = CGRect(x: (maxContentWidth - imageView.image!.size.width) / 2.0, y: 0, width: imageView.image!.size.width, height: imageView.image!.size.height)
            
            contentHeight += imageView.image!.size.height
        }
        
        if titleLabel.hidden == false {
            if contentHeight > 0 {
                contentHeight += 10
            }
            
            titleLabel.frame = CGRect(x: 0, y: contentHeight, width: maxContentWidth, height: titleLabel.bounds.size.height)
            contentHeight += titleLabel.bounds.size.height
        }
        
        if subtitleLabel.hidden == false {
            if contentHeight > 0 {
                contentHeight += 10
            }
            
            let subtitleLabelSize = subtitleLabel.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            subtitleLabel.frame = CGRect(x: 0, y: contentHeight, width: maxContentWidth, height: subtitleLabelSize.height)
            contentHeight += subtitleLabelSize.height
        }
        
        if actionButton.hidden == false {
            if contentHeight > 0 {
                contentHeight += 10
            }
            
            let actionButtonTitleLabelSize = actionButton.titleLabel!.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            actionButton.frame = CGRect(x: 0, y: contentHeight, width: maxContentWidth, height: actionButtonTitleLabelSize.height)
            contentHeight += actionButtonTitleLabelSize.height
        }
        
        contentView.frame = CGRect(x: (bounds.size.width - maxContentWidth) / 2.0, y: (bounds.size.height - contentHeight) / 2.0, width: maxContentWidth, height: contentHeight)
    }

}
