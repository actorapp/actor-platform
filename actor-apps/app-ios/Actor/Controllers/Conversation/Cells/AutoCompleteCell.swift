//
//  AutoCompleteCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 05.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class AutoCompleteCell: UITableViewCell {
    
    var avatarView = AvatarView(frameSize: 32)
    var nickView = TTTAttributedLabel(frame: CGRectZero)
    var nameView = TTTAttributedLabel(frame: CGRectZero)
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        avatarView.enableAnimation = false
        nickView.font = UIFont.systemFontOfSize(14)
        nickView.textColor = MainAppTheme.list.textColor
        
        nameView.font = UIFont.systemFontOfSize(14)
        nameView.textColor = MainAppTheme.list.hintColor
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(nickView)
        self.contentView.addSubview(nameView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func bindData(user: AMUserVM, highlightWord: String) {
        avatarView.bind(user.getNameModel().get(), id: user.getId(), avatar: user.getAvatarModel().get(), clearPrev: true)
        
        var nickText: String
        var nameText: String
        
        if user.getNickModel().get() == nil {
            if user.getLocalNameModel().get() == nil {
                nickText = user.getNameModel().get()
                nameText = ""
            } else {
                nickText = user.getServerNameModel().get()
                nameText = " \u{2022} \(user.getLocalNameModel().get())"
            }
        } else {
            nickText = "@\(user.getNickModel().get())"
            nameText = " \u{2022} \(user.getNameModel().get())"
        }
        
        var nickAttrs = NSMutableAttributedString(string: nickText)
        var nameAttrs = NSMutableAttributedString(string: nameText)
        
        nickAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nickText.size()))
        nameAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nameText.size()))
        
        for range in nickText.rangesOfString(highlightWord) {
            let start = distance(nickText.startIndex, range.startIndex)
            let length = distance(range.startIndex, range.endIndex)
            let nsRange = NSMakeRange(start, length)
            nickAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
        }
        
        for range in nameText.rangesOfString(highlightWord) {
            let start = distance(nameText.startIndex, range.startIndex)
            let length = distance(range.startIndex, range.endIndex)
            let nsRange = NSMakeRange(start, length)
            nameAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
        }
        
        nickView.setText(nickAttrs)
        nameView.setText(nameText)
        
//        [label setText:text afterInheritingLabelAttributesAndConfiguringWithBlock:^ NSMutableAttributedString *(NSMutableAttributedString *mutableAttributedString) {
//            NSRange boldRange = [[mutableAttributedString string] rangeOfString:@"ipsum dolor" options:NSCaseInsensitiveSearch];
//            NSRange strikeRange = [[mutableAttributedString string] rangeOfString:@"sit amet" options:NSCaseInsensitiveSearch];
//            
//            // Core Text APIs use C functions without a direct bridge to UIFont. See Apple's "Core Text Programming Guide" to learn how to configure string attributes.
//            UIFont *boldSystemFont = [UIFont boldSystemFontOfSize:14];
//            CTFontRef font = CTFontCreateWithName((__bridge CFStringRef)boldSystemFont.fontName, boldSystemFont.pointSize, NULL);
//            if (font) {
//            [mutableAttributedString addAttribute:(NSString *)kCTFontAttributeName value:(__bridge id)font range:boldRange];
//            [mutableAttributedString addAttribute:kTTTStrikeOutAttributeName value:@YES range:strikeRange];
//            CFRelease(font);
//            }
//            
//            return mutableAttributedString;
//            }];
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRectMake(6, 6, 32, 32)
        nickView.frame = CGRectMake(44, 6, 100, 32)
        nickView.sizeToFit()
        nickView.frame = CGRectMake(44, 6, nickView.frame.width, 32)
        
        var left = 44 + nickView.frame.width
        nameView.frame = CGRectMake(left, 6, self.contentView.frame.width - left, 32)
    }
}