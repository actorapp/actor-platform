//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAHeaderCell: AATableViewCell {
    
    open var titleView = UILabel()
    open var iconView = UIImageView()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = appStyle.vcBackyardColor
        selectionStyle = UITableViewCellSelectionStyle.none
        
        titleView.textColor = appStyle.cellHeaderColor
        titleView.font = UIFont.systemFont(ofSize: 14)
        contentView.addSubview(titleView)
        
        iconView.contentMode = UIViewContentMode.scaleAspectFill
        contentView.addSubview(iconView)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        let height = self.contentView.bounds.height
        let width = self.contentView.bounds.width
        
        titleView.frame = CGRect(x: 15, y: height - 28, width: width - 48, height: 24)
        iconView.frame = CGRect(x: width - 18 - 15, y: height - 18 - 4, width: 18, height: 18)
    }
}
