//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAContactActionCell: AATableViewCell {
    
    public let titleView = UILabel()
    public let iconView = UIImageView()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFontOfSize(18)
        titleView.textColor = ActorSDK.sharedActor().style.cellTintColor
        iconView.contentMode = UIViewContentMode.Center
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(iconView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(icon: String, actionTitle: String) {
        titleView.text = actionTitle
        iconView.image = UIImage.bundled(icon)?.tintImage(ActorSDK.sharedActor().style.cellTintColor)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        iconView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}