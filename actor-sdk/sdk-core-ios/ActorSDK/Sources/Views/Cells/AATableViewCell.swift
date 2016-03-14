//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AATableViewCell: UITableViewCell {
    
    private static let separatorColor = ActorSDK.sharedActor().style.vcSeparatorColor
    
    private var topSeparator: UIView = UIView()
    private var bottomSeparator: UIView = UIView()
    
    var appStyle: ActorStyle {
        get {
            return ActorSDK.sharedActor().style
        }
    }
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        bottomSeparator.backgroundColor = AATableViewCell.separatorColor
        topSeparator.backgroundColor = AATableViewCell.separatorColor
        
        backgroundColor = appStyle.cellBgColor
        
        let bgView = UIView()
        bgView.backgroundColor = appStyle.cellBgSelectedColor
        selectedBackgroundView = bgView
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    public var topSeparatorLeftInset: CGFloat = 0.0 {
        didSet {
            setNeedsLayout()
        }
    }
    public var bottomSeparatorLeftInset: CGFloat = 0.0 {
        didSet {
            setNeedsLayout()
        }
    }
    
    public var topSeparatorVisible: Bool = false {
        didSet {
            if topSeparatorVisible == oldValue {
                return
            }
            
            if topSeparatorVisible {
                contentView.addSubview(topSeparator)
                contentView.bringSubviewToFront(topSeparator)
            } else {
                topSeparator.removeFromSuperview()
            }
            
            setNeedsLayout()
        }
    }
    
    public var bottomSeparatorVisible: Bool = false {
        didSet {
            if bottomSeparatorVisible == oldValue {
                return
            }
            
            if bottomSeparatorVisible {
                contentView.addSubview(bottomSeparator)
            } else {
                bottomSeparator.removeFromSuperview()
            }
            
            setNeedsLayout()
            
        }
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        if topSeparatorVisible {
            topSeparator.frame = CGRect(x: topSeparatorLeftInset, y: 0, width: bounds.width - topSeparatorLeftInset, height: 0.5)
            contentView.bringSubviewToFront(topSeparator)
        }
        
        if bottomSeparatorVisible {
            bottomSeparator.frame = CGRect(x: bottomSeparatorLeftInset, y: contentView.bounds.height - 0.5, width: bounds.width - bottomSeparatorLeftInset, height: 0.5)
            contentView.bringSubviewToFront(bottomSeparator)
        }
    }
    
    public override func setHighlighted(highlighted: Bool, animated: Bool) {
        if self.highlighted != highlighted {
            super.setHighlighted(highlighted, animated: animated)
        }
        
        if !highlighted {
            if topSeparator.backgroundColor != AATableViewCell.separatorColor {
                topSeparator.backgroundColor = AATableViewCell.separatorColor
            }
            if bottomSeparator.backgroundColor != AATableViewCell.separatorColor {
                bottomSeparator.backgroundColor = AATableViewCell.separatorColor
            }
        }
    }
    
    public override func setSelected(selected: Bool, animated: Bool) {
        if self.selected != selected {
            super.setSelected(selected, animated: animated)
        }
        
        if !selected {
            if topSeparator.backgroundColor != AATableViewCell.separatorColor {
                topSeparator.backgroundColor = AATableViewCell.separatorColor
            }
            if bottomSeparator.backgroundColor != AATableViewCell.separatorColor {
                bottomSeparator.backgroundColor = AATableViewCell.separatorColor
            }
        }
    }
}