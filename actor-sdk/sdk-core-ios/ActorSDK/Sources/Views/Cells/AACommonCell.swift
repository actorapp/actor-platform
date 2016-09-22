//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public enum AACommonCellStyle {
    case normal
    case destructiveCentered
    case destructive
    case `switch`
    case action
    case actionCentered
    case navigation
    case hint
    case checkmark
}

open class AACommonCell: AATableViewCell {
    
    fileprivate var switcher: UISwitch?
    fileprivate var titleLabel = UILabel()
    fileprivate var hintLabel = UILabel()
    
    open var style: AACommonCellStyle = .normal { didSet { updateCellStyle() } }
    open var switchBlock: ((Bool) -> ())?
    open var contentInset: CGFloat = 15
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFont(ofSize: 17.0)
        contentView.addSubview(titleLabel)
        
        hintLabel.font = UIFont.systemFont(ofSize: 17.0)
        hintLabel.textColor = appStyle.cellHintColor
        contentView.addSubview(hintLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Setting text content
    
    open func setContent(_ content: String?) {
        titleLabel.text = content
    }
    
    open func setHint(_ hint: String?) {
        if hint == nil {
            hintLabel.isHidden = true
        } else {
            hintLabel.text = hint
            hintLabel.isHidden = false
        }
        setNeedsLayout()
    }
    
    // Setting switcher content
    
    open func setSwitcherOn(_ on: Bool) {
        setSwitcherOn(on, animated: false)
    }
    
    open func setSwitcherOn(_ on: Bool, animated: Bool) {
        switcher?.setOn(on, animated: animated)
    }
    
    open func setSwitcherEnabled(_ enabled: Bool) {
        switcher?.isEnabled = enabled
    }
    
    // Private methods
    
    fileprivate func updateCellStyle() {
        switch (style) {
        case .normal:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .hint:
            titleLabel.textColor = appStyle.cellHintColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .destructiveCentered:
            titleLabel.textColor = appStyle.cellDestructiveColor
            titleLabel.textAlignment = NSTextAlignment.center
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .destructive:
            titleLabel.textColor = appStyle.cellDestructiveColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .switch:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.left
            setupSwitchIfNeeded()
            switcher?.isHidden = false
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .action:
            titleLabel.textColor = appStyle.cellTintColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .actionCentered:
            titleLabel.textColor = appStyle.cellTintColor
            titleLabel.textAlignment = NSTextAlignment.center
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.none
            break
        case .navigation:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.disclosureIndicator
            
        case .checkmark:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.left
            switcher?.isHidden = true
            accessoryType = UITableViewCellAccessoryType.checkmark
            break
        }
    }
    
    fileprivate func setupSwitchIfNeeded() {
        if switcher == nil {
            switcher = UISwitch()
            switcher!.addTarget(self, action: #selector(AACommonCell.switcherSwitched), for: UIControlEvents.valueChanged)
            switcher!.onTintColor = appStyle.vcSwitchOn
            switcher!.tintColor = appStyle.vcSwitchOff
            contentView.addSubview(switcher!)
        }
    }
    
    func switcherSwitched() {
        if switchBlock != nil {
            switchBlock!(switcher!.isOn)
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        if hintLabel.text != nil {
            hintLabel.frame = CGRect(x: 0, y: 0, width: 100, height: 44)
            hintLabel.sizeToFit()
            
            if accessoryType == UITableViewCellAccessoryType.none {
                hintLabel.frame = CGRect(x: contentView.bounds.width - hintLabel.width - 15, y: 0, width: hintLabel.width, height: 44)
                titleLabel.frame = CGRect(x: contentInset, y: 0, width: contentView.bounds.width - hintLabel.width - contentInset - 20, height: 44)
            } else {
                hintLabel.frame = CGRect(x: contentView.bounds.width - hintLabel.width, y: 0, width: hintLabel.width, height: 44)
                titleLabel.frame = CGRect(x: contentInset, y: 0, width: contentView.bounds.width - hintLabel.width - contentInset - 5, height: 44)
            }
        } else {
            titleLabel.frame = CGRect(x: contentInset, y: 0, width: contentView.bounds.width - contentInset - 5, height: 44)
        }
        
        if switcher != nil {
            let switcherSize = switcher!.bounds.size
            switcher!.frame = CGRect(x: contentView.bounds.width - switcherSize.width - 15, y: (contentView.bounds.height - switcherSize.height) / 2, width: switcherSize.width, height: switcherSize.height)
        }
    }
}
