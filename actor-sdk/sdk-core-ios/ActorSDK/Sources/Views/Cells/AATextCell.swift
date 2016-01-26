//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AATextCell: AATableViewCell {

    public var titleLabel: UILabel = UILabel()
    public var contentLabel: UILabel = UILabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFontOfSize(14.0)
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.textColor = appStyle.cellTintColor
        contentView.addSubview(titleLabel)
        
        contentLabel.font = UIFont.systemFontOfSize(17.0)
        contentLabel.text = " "
        contentLabel.textColor = appStyle.cellTextColor
        contentLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        contentLabel.numberOfLines = 0
        contentLabel.sizeToFit()
        contentView.addSubview(contentLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func setContent(title: String?, content: String?, isAction: Bool) {
        titleLabel.text = title
        contentLabel.text = content
        if isAction {
            contentLabel.textColor = appStyle.cellTintColor
        } else {
            contentLabel.textColor = appStyle.cellTextColor
        }
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: 15, y: 7, width: contentView.bounds.width - 30, height: titleLabel.bounds.height)
        contentLabel.frame = CGRect(x: 15, y: 27, width: contentView.bounds.width - 30, height: 10000)
        contentLabel.sizeToFit()
    }
    
    public class func measure(text: String, width: CGFloat, enableNavigation: Bool) -> CGFloat {
        let size = UIViewMeasure.measureText(text, width: width - 30 - (enableNavigation ? 30 : 0), fontSize: 17)
        return CGFloat(size.height + 36)
    }
}