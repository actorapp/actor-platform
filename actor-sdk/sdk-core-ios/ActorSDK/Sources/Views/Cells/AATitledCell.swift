//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AATitledCell: AATableViewCell {
    
    fileprivate var isAction: Bool = false
    open let titleLabel: UILabel = UILabel()
    open let contentLabel: UILabel = UILabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.textColor = appStyle.cellTintColor
        contentView.addSubview(titleLabel)
        contentView.addSubview(contentLabel)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func setContent(_ title: String, content: String, isAction: Bool) {
        titleLabel.text = title
        contentLabel.text = content
        if isAction {
            contentLabel.textColor = UIColor.lightGray
        } else {
            contentLabel.textColor = appStyle.cellTextColor
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: separatorInset.left, y: 7, width: contentView.bounds.width - separatorInset.left - 10, height: 19)
        contentLabel.frame = CGRect(x: separatorInset.left, y: 27, width: contentView.bounds.width - separatorInset.left - 10, height: 22)
    }
}
