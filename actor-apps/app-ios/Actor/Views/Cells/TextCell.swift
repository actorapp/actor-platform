//
//  TextCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 05.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation


class TextCell: BasicCell {

    private var titleLabel: UILabel = UILabel()
    private var contentLabel: UILabel = UILabel()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFontOfSize(14.0)
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.textColor = MainAppTheme.list.actionColor
        contentView.addSubview(titleLabel)
        
        contentLabel.font = UIFont.systemFontOfSize(17.0)
        contentLabel.text = " "
        contentLabel.textColor = MainAppTheme.list.textColor
        contentLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        contentLabel.numberOfLines = 0
        contentLabel.sizeToFit()
        contentView.addSubview(contentLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setTitle(title: String, content: String) {
        titleLabel.text = title
        contentLabel.text = content
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: separatorInset.left, y: 7, width: contentView.bounds.width - separatorInset.left - 10, height: titleLabel.bounds.height)
        contentLabel.frame = CGRect(x: separatorInset.left, y: 27, width: contentView.bounds.width - separatorInset.left - 10, height: 500)
        contentLabel.sizeToFit()
    }
    
    class func measure(text: String) -> Double {
        
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        var rect = text.boundingRectWithSize(CGSize(width: 320 - 120, height: 10000),
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: UIFont.systemFontOfSize(17.0), NSParagraphStyleAttributeName: style],
            context: nil);
        
        return Double(round(rect.height) + 16)
    }
}