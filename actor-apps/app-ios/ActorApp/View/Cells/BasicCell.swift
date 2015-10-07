//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class BasicCell : UITableViewCell {
    public let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor)
    public let separatorPadding: Int
    
    public init(reuseIdentifier:String, separatorPadding: Int) {
        self.separatorPadding = separatorPadding
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        self.contentView.addSubview(separatorView);
    }
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        self.separatorPadding = 0
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        self.contentView.addSubview(separatorView);
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func hideSeparator(hidden: Bool) {
        separatorView.hidden = hidden
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        let height = self.contentView.frame.height;

        separatorView.frame = CGRectMake(CGFloat(separatorPadding), CGFloat(height - 0.5), width - CGFloat(separatorPadding), CGFloat(0.5));
    }
}