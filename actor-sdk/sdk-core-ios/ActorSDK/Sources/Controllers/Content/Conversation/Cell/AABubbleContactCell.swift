//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AABubbleContactCell: AABubbleCell {
    
    private let avatar = AAAvatarView(frameSize: 44)
    private let name = UILabel()
    private let contact = UILabel()
    private var bindedRecords = [String]()
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        name.font = UIFont.mediumSystemFontOfSize(17)
        contact.font = UIFont.systemFontOfSize(15)
        
        contentView.addSubview(avatar)
        contentView.addSubview(name)
        contentView.addSubview(contact)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
 
    public override func bind(message: ACMessage, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        let contactLayout = cellLayout as! AAContactCellLayout
        
        // Always update bubble insets
        if (isOut) {
            bindBubbleType(.TextOut, isCompact: false)
            
            bubbleInsets = UIEdgeInsets(
                top: AABubbleCell.bubbleTop,
                left: 0 + (AADevice.isiPad ? 16 : 0),
                bottom: AABubbleCell.bubbleBottom,
                right: 4 + (AADevice.isiPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: AABubbleCell.bubbleContentTop,
                left: 6,
                bottom: AABubbleCell.bubbleContentBottom,
                right: 10)
            
            name.textColor = ActorSDK.sharedActor().style.chatTextOutColor
        } else {
            bindBubbleType(.TextIn, isCompact: false)
            
            bubbleInsets = UIEdgeInsets(
                top: AABubbleCell.bubbleTop,
                left: 4 + (AADevice.isiPad ? 16 : 0),
                bottom: AABubbleCell.bubbleBottom,
                right: 0 + (AADevice.isiPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: (isGroup ? 18 : 0) + AABubbleCell.bubbleContentTop,
                left: 13,
                bottom: AABubbleCell.bubbleContentBottom,
                right: 10)
            
            name.textColor = ActorSDK.sharedActor().style.chatTextInColor
        }
        name.text = contactLayout.name
        
        var s = ""
        for i in contactLayout.records {
            if (s != ""){
                s += "\n"
            }
            s += i
        }
        contact.text = s
        contact.numberOfLines = contactLayout.records.count
        bindedRecords = contactLayout.records
        
        avatar.bind(contactLayout.name, id: 0, avatar: nil)
    }
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        // Convenience
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        let height = max(44, bindedRecords.count * 18 + 22)
        layoutBubble(200, contentHeight: CGFloat(height))
        
        if (isOut) {
            avatar.frame = CGRectMake(contentWidth - insets.right - 200, insets.top, 44, 44)
        } else {
            avatar.frame = CGRectMake(insets.left, insets.top, 44, 44)
        }
        name.frame = CGRectMake(avatar.right + 6, insets.top, 200 - 58, 22)
        contact.frame = CGRectMake(avatar.right + 6, insets.top + 22, 200 - 58, 200)
        contact.sizeToFit()
    }
}

public class AAContactCellLayout: AACellLayout {

    let name: String
    let records: [String]
    
    init(name: String, records: [String], date: Int64) {
        self.name = name
        self.records = records
        let height = max(44, records.count * 18 + 22) + 12
        super.init(height: CGFloat(height), date: date, key: "location")
    }
}

public class AABubbleContactCellLayouter: AABubbleLayouter {
    public func isSuitable(message: ACMessage) -> Bool {
        if (!ActorSDK.sharedActor().enableExperimentalFeatures) {
            return false
        }
        
        if (message.content is ACContactContent) {
            return true
        }
        
        return false
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleContactCell.self
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        let content = message.content as! ACContactContent
        var records = [String]()
        for i in 0..<content.getPhones().size() {
            records.append(content.getPhones().getWithInt(i) as! String)
        }
        for i in 0..<content.getEmails().size() {
            records.append(content.getEmails().getWithInt(i) as! String)
        }
        return AAContactCellLayout(name: content.getName(), records: records, date: Int64(message.date))
    }
}