//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAAuthCountryCell: UITableViewCell {
    
    // MARK: - 
    // MARK: Private vars

    private var titleLabel: UILabel!
    private var codeLabel: UILabel!
    
    // MARK: -
    // MARK: Public vars
    
    public var searchMode: Bool!
    
    // MARK: -
    // MARK: Constructors
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel = UILabel()
        titleLabel.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        titleLabel.font = UIFont.systemFontOfSize(17.0)
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.backgroundColor = UIColor.whiteColor()
        contentView.addSubview(titleLabel)
        
        codeLabel = UILabel()
        codeLabel.font = UIFont.boldSystemFontOfSize(17)
        codeLabel.backgroundColor = UIColor.whiteColor()
        codeLabel.textColor = UIColor.blackColor()
        codeLabel.autoresizingMask = UIViewAutoresizing.FlexibleLeftMargin
        codeLabel.contentMode = UIViewContentMode.Right
        codeLabel.textAlignment = NSTextAlignment.Right
        contentView.addSubview(codeLabel)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    public func setTitle(title: String) {
        titleLabel.text = title
    }
    
    public func setCode(code: String) {
        codeLabel.text = code
    }
    
    public func setSearchMode(searchMode: Bool) {
        self.searchMode = searchMode
        
        let codeLabelWidth: CGFloat = 50.0
        let labelHeight: CGFloat = 20.0
        if searchMode {
            titleLabel.frame = CGRect(x: 9.0, y: 12.0, width: contentView.bounds.size.width - codeLabelWidth - codeLabelWidth - 9 - 5, height: labelHeight)
            codeLabel.frame = CGRect(x: contentView.bounds.size.width - codeLabelWidth - 9, y: 12.0, width: codeLabelWidth, height: labelHeight)
        } else {
            titleLabel.frame = CGRect(x: 15, y: 12.0, width: contentView.bounds.size.width - codeLabelWidth - 15 - 5, height: labelHeight)
            codeLabel.frame = CGRect(x: contentView.bounds.size.width - codeLabelWidth - 15, y: 12.0, width: codeLabelWidth, height: labelHeight)
        }
    }

}
