//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

enum AATableViewCellStyle {
    case Normal
    case DestructiveCentered
    case Destructive
    case Switch
    case Blue
    case Green
    case Navigation
    case Hint
}

class AATableViewCell: UITableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var switcher: UISwitch?
    private var topSeparator: UIView?
    private var bottomSeparator: UIView?
    
    private var topSeparatorLeftInset: CGFloat = 0.0
    private var bottomSeparatorLeftInset: CGFloat = 0.0
    
    // MARK: -
    // MARK: Public vars
    
    var style: AATableViewCellStyle = AATableViewCellStyle.Normal {
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
        backgroundColor = MainAppTheme.list.bgColor
        
        var selectedView = UIView()
        selectedView.backgroundColor = MainAppTheme.list.bgSelectedColor
        selectedBackgroundView = selectedView
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
        default:
            break
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
    
    func showTopSeparator() {
        if topSeparator == nil {
            topSeparator = UIView()
            topSeparator!.backgroundColor = MainAppTheme.list.separatorColor
        }
        
        if topSeparator!.superview == nil {
            contentView.addSubview(topSeparator!)
        }
    }
    
    func hideTopSeparator() {
        topSeparator?.removeFromSuperview()
    }
    
    func showBottomSeparator() {
        if bottomSeparator == nil {
            bottomSeparator = UIView()
            bottomSeparator!.backgroundColor = MainAppTheme.list.separatorColor
        }
        
        if bottomSeparator!.superview == nil {
            contentView.addSubview(bottomSeparator!)
        }
    }
    
    func hideBottomSeparator() {
        bottomSeparator?.removeFromSuperview()
    }
    
    // MARK: -
    // MARK: Setters
    
    func setContent(content: String) {
        textLabel!.text = content
    }
    
    func setLeftInset(leftInset: CGFloat) {
        separatorInset = UIEdgeInsets(top: 0.0, left: leftInset, bottom: 0.0, right: 0.0)
    }
    
    func setTopSeparatorLeftInset(leftInset: CGFloat) {
        topSeparatorLeftInset = leftInset
        setNeedsLayout()
    }
    
    func setBottomSeparatorLeftInset(leftInset: CGFloat) {
        bottomSeparatorLeftInset = leftInset
        setNeedsLayout()
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
    
    override func setHighlighted(highlighted: Bool, animated: Bool) {
        super.setHighlighted(highlighted, animated: animated)
        
        if !highlighted {
            if topSeparator != nil {
                topSeparator!.backgroundColor = MainAppTheme.list.separatorColor
            }
            
            if bottomSeparator != nil {
                bottomSeparator!.backgroundColor = MainAppTheme.list.separatorColor
            }
        }
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        if !selected {
            if topSeparator != nil {
                topSeparator!.backgroundColor = MainAppTheme.list.separatorColor
            }
            
            if bottomSeparator != nil {
                bottomSeparator!.backgroundColor = MainAppTheme.list.separatorColor
            }
        }
    }
    
    // MARK: -
    // MARK: Reuse
    
    override func prepareForReuse() {
        hideTopSeparator()
        hideBottomSeparator()
        super.prepareForReuse()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if switcher != nil {
            let switcherSize = switcher!.bounds.size
            switcher!.frame = CGRect(x: contentView.bounds.width - switcherSize.width - 15, y: (contentView.bounds.height - switcherSize.height) / 2, width: switcherSize.width, height: switcherSize.height)
        }
        
        if topSeparator != nil {
            topSeparator!.frame = CGRect(x: topSeparatorLeftInset, y: 0, width: bounds.width - topSeparatorLeftInset, height: Utils.retinaPixel())
        }
        
        if bottomSeparator != nil {
            bottomSeparator!.frame = CGRect(x: bottomSeparatorLeftInset, y: contentView.bounds.height - Utils.retinaPixel(), width: bounds.width - bottomSeparatorLeftInset, height: Utils.retinaPixel())
        }
    }

}
