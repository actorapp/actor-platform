//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AABigPlaceholderView: UIView {
    
    fileprivate var contentView: UIView!
    fileprivate var bgView: UIView!
    fileprivate var imageView: UIImageView!
    fileprivate var titleLabel: UILabel!
    fileprivate var subtitleLabel: UILabel!
    fileprivate var actionButton: UIButton!
    fileprivate var topOffset: CGFloat!
    fileprivate var subtitle2Label: UILabel!
    fileprivate var action2Button: UIButton!
    
    //
    // Constructors
    //
    
    init(topOffset: CGFloat!) {
        super.init(frame: CGRect.zero)
        
        self.topOffset = topOffset
        
        backgroundColor = UIColor.white
        
        contentView = UIView()
        contentView.backgroundColor = UIColor.white
        addSubview(contentView)
        
        imageView = UIImageView()
        imageView.isHidden = true
        
        bgView = UIView()
        bgView.backgroundColor = ActorSDK.sharedActor().style.placeholderBgColor
        contentView.addSubview(bgView)
        contentView.addSubview(imageView)
        
        titleLabel = UILabel()
        titleLabel.textColor = ActorSDK.sharedActor().style.placeholderTitleColor
        titleLabel.font = UIFont.systemFont(ofSize: 22)
        titleLabel.textAlignment = NSTextAlignment.center
        titleLabel.text = " "
        titleLabel.sizeToFit()
        contentView.addSubview(titleLabel)
        
        subtitleLabel = UILabel()
        subtitleLabel.textColor = ActorSDK.sharedActor().style.placeholderHintColor
        subtitleLabel.font = UIFont.systemFont(ofSize: 16.0)
        subtitleLabel.textAlignment = NSTextAlignment.center
        subtitleLabel.numberOfLines = 0
        contentView.addSubview(subtitleLabel)
        
        actionButton = UIButton(type: .system)
        actionButton.titleLabel!.font = UIFont.mediumSystemFontOfSize(21)
        contentView.addSubview(actionButton)
        
        subtitle2Label = UILabel()
        subtitle2Label.textColor = ActorSDK.sharedActor().style.placeholderHintColor
        subtitle2Label.font = UIFont.systemFont(ofSize: 16.0)
        subtitle2Label.textAlignment = NSTextAlignment.center
        subtitle2Label.numberOfLines = 0
        contentView.addSubview(subtitle2Label)
        
        action2Button = UIButton(type: .system)
        action2Button.titleLabel!.font = UIFont.mediumSystemFontOfSize(21)
        contentView.addSubview(action2Button)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    //
    // Setting image
    //
    
    func setImage(_ image: UIImage?, title: String?, subtitle: String?) {
        setImage(image, title: title, subtitle: subtitle, actionTitle: nil,  subtitle2: nil, actionTarget: nil, actionSelector: nil, action2title: nil, action2Selector: nil)
    }
    
    func setImage(_ image: UIImage?, title: String?, subtitle: String?, actionTitle: String?, subtitle2: String?, actionTarget: AnyObject?, actionSelector: Selector?, action2title: String?, action2Selector: Selector?) {
        
        if image != nil {
            imageView.image = image!
            imageView.isHidden = false
        } else {
            imageView.isHidden = true
        }
        
        if title != nil {
            titleLabel.text = title
            titleLabel.isHidden = false
        } else {
            titleLabel.isHidden = true
        }
        
        if subtitle != nil {
            
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.lineHeightMultiple = 1.11
            paragraphStyle.alignment = NSTextAlignment.center
            
            let attrString = NSMutableAttributedString(string: subtitle!)
            attrString.addAttribute(NSParagraphStyleAttributeName, value:paragraphStyle, range:NSMakeRange(0, attrString.length))

            subtitleLabel.attributedText = attrString
            
            subtitleLabel.isHidden = false
        } else {
            subtitleLabel.isHidden = true
        }

        if actionTitle != nil && actionTarget != nil && actionSelector != nil {
            actionButton.removeTarget(nil, action: nil, for: UIControlEvents.allEvents)
            actionButton.addTarget(actionTarget!, action: actionSelector!, for: UIControlEvents.touchUpInside)
            actionButton.setTitle(actionTitle, for: UIControlState())
            actionButton.isHidden = false
        } else {
            actionButton.isHidden = true
        }
        
        if (subtitle2 != nil) {
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.lineHeightMultiple = 1.11
            paragraphStyle.alignment = NSTextAlignment.center
            
            let attrString = NSMutableAttributedString(string: subtitle2!)
            attrString.addAttribute(NSParagraphStyleAttributeName, value:paragraphStyle, range:NSMakeRange(0, attrString.length))
            
            subtitle2Label.attributedText = attrString
            
            subtitle2Label.isHidden = false
        } else {
            subtitle2Label.isHidden = true
        }
        
        if action2title != nil && actionTarget != nil && actionSelector != nil {
            action2Button.removeTarget(nil, action: nil, for: UIControlEvents.allEvents)
            action2Button.addTarget(actionTarget!, action: action2Selector!, for: UIControlEvents.touchUpInside)
            action2Button.setTitle(action2title, for: UIControlState())
            action2Button.isHidden = false
        } else {
            action2Button.isHidden = true
        }
        
        setNeedsLayout()
    }
    
    //
    // Layouting
    //

    override func layoutSubviews() {
        super.layoutSubviews()
        
        var contentHeight: CGFloat = 0
        let maxContentWidth = bounds.size.width - 40
        
        if imageView.isHidden == false {
            imageView.frame = CGRect(x: 20 + (maxContentWidth - imageView.image!.size.width) / 2.0, y: topOffset, width: imageView.image!.size.width, height: imageView.image!.size.height)
            
            contentHeight += imageView.image!.size.height + topOffset
        }
        
        bgView.frame = CGRect(x: 0, y: 0, width: bounds.size.width, height: imageView.frame.height * 0.75 + topOffset)
        
        if titleLabel.isHidden == false {
            if contentHeight > 0 {
                contentHeight += 10
            }
            
            titleLabel.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: titleLabel.bounds.size.height)
            contentHeight += titleLabel.bounds.size.height
        }
        
        if subtitleLabel.isHidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let subtitleLabelSize = subtitleLabel.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.greatestFiniteMagnitude))
            subtitleLabel.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: subtitleLabelSize.height)
            contentHeight += subtitleLabelSize.height
        }
        
        if actionButton.isHidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let actionButtonTitleLabelSize = actionButton.titleLabel!.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.greatestFiniteMagnitude))
            actionButton.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: actionButtonTitleLabelSize.height)
            contentHeight += actionButtonTitleLabelSize.height
        }
        
        if subtitle2Label.isHidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let subtitleLabelSize = subtitle2Label.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.greatestFiniteMagnitude))
            subtitle2Label.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: subtitleLabelSize.height)
            contentHeight += subtitleLabelSize.height
        }
        
        if action2Button.isHidden == false {
            if contentHeight > 0 {
                contentHeight += 14
            }
            
            let actionButtonTitleLabelSize = action2Button.titleLabel!.sizeThatFits(CGSize(width: maxContentWidth, height: CGFloat.greatestFiniteMagnitude))
            action2Button.frame = CGRect(x: 20, y: contentHeight, width: maxContentWidth, height: actionButtonTitleLabelSize.height)
            contentHeight += actionButtonTitleLabelSize.height
        }
        
        contentView.frame = CGRect(x: 0, y: 0, width: maxContentWidth, height: contentHeight)
    }

}
