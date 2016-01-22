//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAHeaderCell: AATableViewCell {
    
    public var titleView = UILabel()
    public var iconView = UIImageView()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = appStyle.vcBackyardColor
        selectionStyle = UITableViewCellSelectionStyle.None
        
        titleView.textColor = appStyle.cellHeaderColor
        titleView.font = UIFont.systemFontOfSize(14)
        contentView.addSubview(titleView)
        
        iconView.contentMode = UIViewContentMode.ScaleAspectFill
        contentView.addSubview(iconView)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        let height = self.contentView.bounds.height
        let width = self.contentView.bounds.width
        
        titleView.frame = CGRectMake(15, height - 28, width - 48, 24)
        iconView.frame = CGRectMake(width - 18 - 15, height - 18 - 4, 18, 18)
    }
}