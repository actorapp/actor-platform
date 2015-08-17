//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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

    func bindData(user: ACMentionFilterResult, highlightWord: String) {
        avatarView.bind(user.getMentionString(), id: user.getUid(), avatar: user.getAvatar(), clearPrev: true)
        
        var nickText: String
        var nameText: String
        
        nickText = user.getMentionString()
        if user.getOriginalString() != nil {
            nameText = " \u{2022} \(user.getOriginalString())";
        } else {
            nameText = ""
        }
        
        var nickAttrs = NSMutableAttributedString(string: nickText)
        var nameAttrs = NSMutableAttributedString(string: nameText)
        
        nickAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nickText.size))
        nickAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.list.textColor, range: NSMakeRange(0, nickText.size))
        
        nameAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nameText.size))
        nameAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.list.hintColor, range: NSMakeRange(0, nameText.size))
        
        for i in 0..<user.getMentionMatches().size() {
            var match = user.getMentionMatches().getWithInt(i) as! ACStringMatch
            let nsRange = NSMakeRange(Int(match.getStart()), Int(match.getLength()))
            nickAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
        }
        
        if user.getOriginalString() != nil {
            for i in 0..<user.getOriginalMatches().size() {
                var match = user.getOriginalMatches().getWithInt(i) as! ACStringMatch
                let nsRange = NSMakeRange(Int(match.getStart()) + 3, Int(match.getLength()))
                nameAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
            }
        }
        
        nickView.setText(nickAttrs)
        nameView.setText(nameAttrs)
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