//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

enum CommonCellStyle {
    case Normal
    case DestructiveCentered
    case Destructive
    case Switch
    case Action
    case ActionCentered
    case Navigation
    case Hint
}

class CommonCell: UATableViewCell {
    
    private var switcher: UISwitch?
    private var titleLabel = UILabel()
    private var hintLabel = UILabel()
    
    var style: CommonCellStyle = .Normal { didSet { updateCellStyle() } }
    var switchBlock: ((Bool) -> ())?
    var contentInset: CGFloat = 15
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFontOfSize(17.0)
        contentView.addSubview(titleLabel)
        
        hintLabel.font = UIFont.systemFontOfSize(17.0)
        hintLabel.textColor = MainAppTheme.list.hintColor
        contentView.addSubview(hintLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Setting text content
    
    func setContent(content: String?) {
        titleLabel.text = content
    }
    
    func setHint(hint: String?) {
        if hint == nil {
            hintLabel.hidden = true
        } else {
            hintLabel.text = hint
            hintLabel.hidden = false
        }
    }
    
    // Setting switcher content
    
    func setSwitcherOn(on: Bool) {
        setSwitcherOn(on, animated: false)
    }
    
    func setSwitcherOn(on: Bool, animated: Bool) {
        switcher?.setOn(on, animated: animated)
    }
    
    func setSwitcherEnabled(enabled: Bool) {
        switcher?.enabled = enabled
    }
    
    // Private methods
    
    private func updateCellStyle() {
        switch (style) {
        case .Normal:
            titleLabel.textColor = MainAppTheme.list.textColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Hint:
            titleLabel.textColor = MainAppTheme.list.hintColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .DestructiveCentered:
            titleLabel.textColor = UIColor.redColor()
            titleLabel.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Destructive:
            titleLabel.textColor = UIColor.redColor()
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Switch:
            titleLabel.textColor = MainAppTheme.list.textColor
            titleLabel.textAlignment = NSTextAlignment.Left
            setupSwitchIfNeeded()
            switcher?.hidden = false
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Action:
            titleLabel.textColor = MainAppTheme.list.actionColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .ActionCentered:
            titleLabel.textColor = MainAppTheme.list.actionColor
            titleLabel.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Navigation:
            titleLabel.textColor = MainAppTheme.list.textColor
            titleLabel.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        }
    }
    
    private func setupSwitchIfNeeded() {
        if switcher == nil {
            switcher = UISwitch()
            switcher!.addTarget(self, action: Selector("switcherSwitched"), forControlEvents: UIControlEvents.ValueChanged)
            contentView.addSubview(switcher!)
        }
    }
    
    func switcherSwitched() {
        if switchBlock != nil {
            switchBlock!(switcher!.on)
        }
    }
    
    override func layoutSubviews() {
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