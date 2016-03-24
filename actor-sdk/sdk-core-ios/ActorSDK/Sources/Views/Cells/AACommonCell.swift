//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public enum AACommonCellStyle {
    case Normal
    case DestructiveCentered
    case Destructive
    case Switch
    case Action
    case ActionCentered
    case Navigation
    case Hint
    case Checkmark
}

public class AACommonCell: AATableViewCell {
    
    private var switcher: UISwitch?
    private var titleLabel = UILabel()
    private var hintLabel = UILabel()
    
    public var style: AACommonCellStyle = .Normal { didSet { updateCellStyle() } }
    public var switchBlock: ((Bool) -> ())?
    public var contentInset: CGFloat = 15
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFontOfSize(17.0)
        contentView.addSubview(titleLabel)
        
        hintLabel.font = UIFont.systemFontOfSize(17.0)
        hintLabel.textColor = appStyle.cellHintColor
        contentView.addSubview(hintLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Setting text content
    
    public func setContent(content: String?) {
        titleLabel.text = content
    }
    
    public func setHint(hint: String?) {
        if hint == nil {
            hintLabel.hidden = true
        } else {
            hintLabel.text = hint
            hintLabel.hidden = false
        }
    }
    
    // Setting switcher content
    
    public func setSwitcherOn(on: Bool) {
        setSwitcherOn(on, animated: false)
    }
    
    public func setSwitcherOn(on: Bool, animated: Bool) {
        switcher?.setOn(on, animated: animated)
    }
    
    public func setSwitcherEnabled(enabled: Bool) {
        switcher?.enabled = enabled
    }
    
    // Private methods
    
    private func updateCellStyle() {
        switch (style) {
        case .Normal:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Hint:
            titleLabel.textColor = appStyle.cellHintColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .DestructiveCentered:
            titleLabel.textColor = appStyle.cellDestructiveColor
            titleLabel.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Destructive:
            titleLabel.textColor = appStyle.cellDestructiveColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Switch:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.Left
            setupSwitchIfNeeded()
            switcher?.hidden = false
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Action:
            titleLabel.textColor = appStyle.cellTintColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .ActionCentered:
            titleLabel.textColor = appStyle.cellTintColor
            titleLabel.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Navigation:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            
        case .Checkmark:
            titleLabel.textColor = appStyle.cellTextColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.Checkmark
            break
        }
    }
    
    private func setupSwitchIfNeeded() {
        if switcher == nil {
            switcher = UISwitch()
            switcher!.addTarget(self, action: #selector(AACommonCell.switcherSwitched), forControlEvents: UIControlEvents.ValueChanged)
            switcher!.onTintColor = appStyle.vcSwitchOn
            switcher!.tintColor = appStyle.vcSwitchOff
            contentView.addSubview(switcher!)
        }
    }
    
    func switcherSwitched() {
        if switchBlock != nil {
            switchBlock!(switcher!.on)
        }
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        if hintLabel.text != nil {
            hintLabel.frame = CGRectMake(0, 0, 100, 44)
            hintLabel.sizeToFit()
            
            hintLabel.frame = CGRectMake(contentView.bounds.width - hintLabel.width, 0, hintLabel.width, 44)
            titleLabel.frame = CGRectMake(contentInset, 0, contentView.bounds.width - hintLabel.width - contentInset - 5, 44)
        } else {
            titleLabel.frame = CGRectMake(contentInset, 0, contentView.bounds.width - contentInset - 5, 44)
        }
        
        if switcher != nil {
            let switcherSize = switcher!.bounds.size
            switcher!.frame = CGRect(x: contentView.bounds.width - switcherSize.width - 15, y: (contentView.bounds.height - switcherSize.height) / 2, width: switcherSize.width, height: switcherSize.height)
        }
    }
}