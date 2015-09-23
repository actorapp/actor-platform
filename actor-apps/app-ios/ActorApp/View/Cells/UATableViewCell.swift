//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class UATableViewCell: UITableViewCell {
    
    private var topSeparator: UIView = UIView()
    private var bottomSeparator: UIView = UIView()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        bottomSeparator.backgroundColor = MainAppTheme.list.separatorColor
        topSeparator.backgroundColor = MainAppTheme.list.separatorColor
        
        applyStyle("cell")
    }
    
    init(cellStyle: String, reuseIdentifier: String?) {
        
        let style = pickStyle(cellStyle)
        let st: UITableViewCellStyle = (style != nil && style!.cellStyle != nil) ? style!.cellStyle! : .Default
        
        super.init(style: st, reuseIdentifier: reuseIdentifier)
        
        if style != nil {
            applyStyle(style!)
        }
        
        bottomSeparator.backgroundColor = MainAppTheme.list.separatorColor
        topSeparator.backgroundColor = MainAppTheme.list.separatorColor
    }

    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    var topSeparatorLeftInset: CGFloat = 0.0 {
        didSet {
            setNeedsLayout()
        }
    }
    var bottomSeparatorLeftInset: CGFloat = 0.0 {
        didSet {
            setNeedsLayout()
        }
    }
    
    var topSeparatorVisible: Bool = false {
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
    
    var bottomSeparatorVisible: Bool = false {
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
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if topSeparatorVisible {
            topSeparator.frame = CGRect(x: topSeparatorLeftInset, y: 0, width: bounds.width - topSeparatorLeftInset, height: 0.5)
            contentView.bringSubviewToFront(topSeparator)
        }
        
        if bottomSeparatorVisible {
            bottomSeparator.frame = CGRect(x: bottomSeparatorLeftInset, y: contentView.bounds.height - Utils.retinaPixel(), width: bounds.width - bottomSeparatorLeftInset, height: 0.5)
            contentView.bringSubviewToFront(bottomSeparator)
        }
    }
    
    override func setHighlighted(highlighted: Bool, animated: Bool) {
        super.setHighlighted(highlighted, animated: animated)
        
        if !highlighted {
            topSeparator.backgroundColor = MainAppTheme.list.separatorColor
            bottomSeparator.backgroundColor = MainAppTheme.list.separatorColor
        }
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        if !selected {
            topSeparator.backgroundColor = MainAppTheme.list.separatorColor
            bottomSeparator.backgroundColor = MainAppTheme.list.separatorColor
        }
    }
}