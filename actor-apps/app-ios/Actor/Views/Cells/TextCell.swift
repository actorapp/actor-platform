//
//  TextCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 05.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation


class TextCell: UATableViewCell {

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
    
    func setAction(isAction: Bool) {
        if isAction {
            contentLabel.textColor = MainAppTheme.list.actionColor
        } else {
            contentLabel.textColor = MainAppTheme.list.textColor
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: 15, y: 7, width: contentView.bounds.width - 30, height: titleLabel.bounds.height)
        contentLabel.frame = CGRect(x: 15, y: 27, width: contentView.bounds.width - 30, height: 10000)
        contentLabel.sizeToFit()
    }
    
    class func measure(text: String, width: CGFloat, enableNavigation: Bool) -> CGFloat {
        
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        var rect = text.boundingRectWithSize(CGSize(width: width - 30 - (enableNavigation ? 30 : 0), height: 10000),
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: UIFont.systemFontOfSize(17.0), NSParagraphStyleAttributeName: style],
            context: nil);
        
        return CGFloat(round(rect.height) + 36)
    }
}