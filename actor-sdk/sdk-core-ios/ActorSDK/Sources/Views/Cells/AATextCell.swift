//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AATextCell: AATableViewCell {

    open var titleLabel: UILabel = UILabel()
    open var contentLabel: UILabel = UILabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFont(ofSize: 14.0)
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.textColor = appStyle.cellTintColor
        contentView.addSubview(titleLabel)
        
        contentLabel.font = UIFont.systemFont(ofSize: 17.0)
        contentLabel.text = " "
        contentLabel.textColor = appStyle.cellTextColor
        contentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping
        contentLabel.numberOfLines = 0
        contentLabel.sizeToFit()
        contentView.addSubview(contentLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func setContent(_ title: String?, content: String?, isAction: Bool) {
        titleLabel.text = title
        titleLabel.isHidden = title == nil
        contentLabel.text = content
        
        if isAction {
            contentLabel.textColor = appStyle.cellTintColor
        } else {
            contentLabel.textColor = appStyle.cellTextColor
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        if titleLabel.isHidden {
            contentLabel.frame = CGRect(x: 15, y: 8, width: contentView.bounds.width - 30, height: contentView.height - 16)
        } else {
            titleLabel.frame = CGRect(x: 15, y: 7, width: contentView.bounds.width - 30, height: titleLabel.bounds.height)
            contentLabel.frame = CGRect(x: 15, y: 27, width: contentView.bounds.width - 30, height: 10000)
            contentLabel.sizeToFit()
        }
    }
    
    open class func measure(_ title: String?, text: String, width: CGFloat, enableNavigation: Bool) -> CGFloat {
        let size = UIViewMeasure.measureText(text, width: width - 30 - (enableNavigation ? 30 : 0), fontSize: 17)
        
        if title != nil {
            return CGFloat(size.height + 36)
        } else {
            return CGFloat(max(size.height + 16, 44))
        }
    }
}
