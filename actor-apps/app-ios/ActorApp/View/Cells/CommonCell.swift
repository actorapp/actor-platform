//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

enum CommonCellStyle {
    case Normal
    case DestructiveCentered
    case Destructive
    case Switch
    case Blue
    case Green
    case Navigation
    case Hint
}

class CommonCell: UATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var switcher: UISwitch?
    
    // MARK: -
    // MARK: Public vars
    
    var style: CommonCellStyle = .Normal {
        didSet {
            updateCellStyle()
        }
    }
    
    var switchBlock: ((Bool) -> ())?
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        textLabel!.font = UIFont.systemFontOfSize(17.0)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    private func updateCellStyle() {
        switch (style) {
        case .Normal:
            textLabel!.textColor = MainAppTheme.list.textColor
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Hint:
            textLabel!.textColor = MainAppTheme.list.hintColor
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .DestructiveCentered:
            textLabel!.textColor = UIColor.redColor()
            textLabel!.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Destructive:
            textLabel!.textColor = UIColor.redColor()
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Switch:
            textLabel!.textColor = MainAppTheme.list.textColor
            setupSwitchIfNeeded()
            switcher?.hidden = false
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Blue: // TODO: Maybe rename?
//            textLabel!.textColor = UIColor.RGB(0x007ee5)
            textLabel!.textColor = MainAppTheme.list.actionColor
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Green:
            textLabel!.textColor = UIColor(red: 76/255.0, green: 216/255.0, blue: 100/255.0, alpha: 1.0) // TODO: Change color
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            accessoryType = UITableViewCellAccessoryType.None
            break
        case .Navigation:
            textLabel!.textColor = MainAppTheme.list.textColor
            textLabel!.textAlignment = NSTextAlignment.Left
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
    
    // MARK: -
    // MARK: Setters
    
    func setContent(content: String) {
        textLabel!.text = content
    }
    
    func setSwitcherOn(on: Bool) {
        setSwitcherOn(on, animated: false)
    }
    
    func setSwitcherOn(on: Bool, animated: Bool) {
        switcher?.setOn(on, animated: animated)
    }
    
    func setSwitcherEnabled(enabled: Bool) {
        switcher?.enabled = enabled
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if switcher != nil {
            let switcherSize = switcher!.bounds.size
            switcher!.frame = CGRect(x: contentView.bounds.width - switcherSize.width - 15, y: (contentView.bounds.height - switcherSize.height) / 2, width: switcherSize.width, height: switcherSize.height)
        }
    }
}
