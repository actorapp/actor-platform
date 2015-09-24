//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class BigPlaceholderView: UIView {
    
    // MARK: -
    // MARK: Private vars
    
    private var contentView: UIView!
    private var bgView: UIView!
    private var imageView: UIImageView!
    private var titleLabel: UILabel!
    private var subtitleLabel: UILabel!
    private var actionButton: UIButton!
    private var topOffset: CGFloat!
    private var subtitle2Label: UILabel!
    private var action2Button: UIButton!
    
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
        imageView.hidden = true
        
        bgView = UIView()
        bgView.backgroundColor = MainAppTheme.navigation.barSolidColor
        contentView.addSubview(bgView)
        contentView.addSubview(imageView)
        
        titleLabel = UILabel()
        titleLabel.textColor = MainAppTheme.placeholder.textTitle
        titleLabel.font = UIFont.systemFontOfSize(22)
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.text = " "
        titleLabel.sizeToFit()
        contentView.addSubview(titleLabel)
        
        subtitleLabel = UILabel()
        subtitleLabel.textColor = MainAppTheme.placeholder.textHint
        subtitleLabel.font = UIFont.systemFontOfSize(16.0)
        subtitleLabel.textAlignment = NSTextAlignment.Center
        subtitleLabel.numberOfLines = 0
        contentView.addSubview(subtitleLabel)
        
        actionButton = UIButton(type: .System)
        actionButton.titleLabel!.font = UIFont.mediumSystemFontOfSize(21)
        contentView.addSubview(actionButton)
        
        subtitle2Label = UILabel()
        subtitle2Label.textColor = MainAppTheme.placeholder.textHint
        subtitle2Label.font = UIFont.systemFontOfSize(16.0)
        subtitle2Label.textAlignment = NSTextAlignment.Center
        subtitle2Label.numberOfLines = 0
        contentView.addSubview(subtitle2Label)
        
        action2Button = UIButton(type: .System)
        action2Button.titleLabel!.font = UIFont.mediumSystemFontOfSize(21)
        contentView.addSubview(action2Button)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setImage(image: UIImage?, title: String?, subtitle: String?) {
        setImage(image, title: title, subtitle: subtitle, actionTitle: nil,  subtitle2: nil, actionTarget: nil, actionSelector: nil, action2title: nil, action2Selector: nil)
    }
    
    func setImage(image: UIImage?, title: String?, subtitle: String?, actionTitle: String?, subtitle2: String?, actionTarget: AnyObject?, actionSelector: Selector?, action2title: String?, action2Selector: Selector?) {
        
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
            
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.lineHeightMultiple = 1.11
            paragraphStyle.alignment = NSTextAlignment.Center
            
            let attrString = NSMutableAttributedString(string: subtitle!)
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
        
        if (subtitle2 != nil) {
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.lineHeightMultiple = 1.11
            paragraphStyle.alignment = NSTextAlignment.Center
            
            let attrString = NSMutableAttributedString(string: subtitle2!)
            attrString.addAttribute(NSParagraphStyleAttributeName, value:paragraphStyle, range:NSMakeRange(0, attrString.length))
            
            subtitle2Label.attributedText = attrString
            
            subtitle2Label.hidden = false
        } else {
            subtitle2Label.hidden = true
        }
        
        if action2title != nil && actionTarget != nil && actionSelector != nil {
            action2Button.removeTarget(nil, action: nil, forControlEvents: UIControlEvents.AllEvents)
            action2Button.addTarget(actionTarget!, action: action2Selector!, forControlEvents: UIControlEvents.TouchUpInside)
            action2Button.setTitle(action2title, forState: UIControlState.Normal)
            action2Button.hidden = false
        } else {
            action2Button.hidden = true
        }
        
        setNeedsLayout()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var contentHeight: CGFloat = 0
        let maxContentWidth = bounds.size.width - 40
        
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
        
        if subtitle2Label.hidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let subtitleLabelSize = subtitle2Label.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            subtitle2Label.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: subtitleLabelSize.height)
            contentHeight += subtitleLabelSize.height
        }
        
        if action2Button.hidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let actionButtonTitleLabelSize = action2Button.titleLabel!.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.max))
            action2Button.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: actionButtonTitleLabelSize.height)
            contentHeight += actionButtonTitleLabelSize.height
        }
        
//        contentView.frame = CGRect(x: (bounds.size.width - maxContentWidth) / 2.0, y: (bounds.size.height - contentHeight) / 2.0, width: maxContentWidth, height: contentHeight)

        contentView.frame = CGRect(x: 0, y: 0, width: maxContentWidth, height: contentHeight)
    }

}
