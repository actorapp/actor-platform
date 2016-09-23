//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AAAuthCountryCell: UITableViewCell {
    
    // MARK: - 
    // MARK: Private vars

    fileprivate var titleLabel: UILabel!
    fileprivate var codeLabel: UILabel!
    
    // MARK: -
    // MARK: Public vars
    
    open var searchMode: Bool!
    
    // MARK: -
    // MARK: Constructors
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel = UILabel()
        titleLabel.autoresizingMask = UIViewAutoresizing.flexibleWidth
        titleLabel.font = UIFont.systemFont(ofSize: 17.0)
        titleLabel.textColor = UIColor.black
        titleLabel.backgroundColor = UIColor.white
        contentView.addSubview(titleLabel)
        
        codeLabel = UILabel()
        codeLabel.font = UIFont.boldSystemFont(ofSize: 17)
        codeLabel.backgroundColor = UIColor.white
        codeLabel.textColor = UIColor.black
        codeLabel.autoresizingMask = UIViewAutoresizing.flexibleLeftMargin
        codeLabel.contentMode = UIViewContentMode.right
        codeLabel.textAlignment = NSTextAlignment.right
        contentView.addSubview(codeLabel)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    open func setTitle(_ title: String) {
        titleLabel.text = title
    }
    
    open func setCode(_ code: String) {
        codeLabel.text = code
    }
    
    open func setSearchMode(_ searchMode: Bool) {
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
