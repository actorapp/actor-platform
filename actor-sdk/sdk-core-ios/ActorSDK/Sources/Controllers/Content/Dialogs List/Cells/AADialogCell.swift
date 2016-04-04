//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AADialogCell: AATableViewCell, AABindedCell {
    
    // Binding data type
    
    public typealias BindData = ACDialog
    
    // Hight of cell
    
    public static func bindedCellHeight(table: AAManagedTable, item: ACDialog) -> CGFloat {
        
        return 76
    }
    
    // Cached design
    
    private static let counterBgImage = Imaging
        .imageWithColor(ActorSDK.sharedActor().style.dialogCounterBgColor, size: CGSizeMake(18, 18))
        .roundImage(18)
        .resizableImageWithCapInsets(UIEdgeInsetsMake(9, 9, 9, 9))
    private lazy var dialogTextActiveColor = ActorSDK.sharedActor().style.dialogTextActiveColor
    private lazy var dialogTextColor = ActorSDK.sharedActor().style.dialogTextColor
    private lazy var dialogStatusSending = ActorSDK.sharedActor().style.dialogStatusSending
    private lazy var dialogStatusRead = ActorSDK.sharedActor().style.dialogStatusRead
    private lazy var dialogStatusReceived = ActorSDK.sharedActor().style.dialogStatusReceived
    private lazy var dialogStatusSent = ActorSDK.sharedActor().style.dialogStatusSent
    private lazy var dialogStatusError = ActorSDK.sharedActor().style.dialogStatusError
    private lazy var chatIconClock = ActorSDK.sharedActor().style.chatIconClock
    private lazy var chatIconCheck2 = ActorSDK.sharedActor().style.chatIconCheck2
    private lazy var chatIconCheck1 = ActorSDK.sharedActor().style.chatIconCheck1
    private lazy var chatIconError = ActorSDK.sharedActor().style.chatIconError
    
    // Views

    private var cellRenderer: AABackgroundCellRenderer<AADialogCellConfig, AADialogCellLayout>!
    
    public let avatarView = AAAvatarView()
    public let titleView = YYLabel()
    public let messageView = YYLabel()
    
    public let dateView = YYLabel()
    public let statusView = UIImageView()
    public let counterView = YYLabel()
    public let counterViewBg = UIImageView()
    
    private var isEditing = false
    
    // Binding Data
    
    private var bindedItem: ACDialog?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    
        cellRenderer = AABackgroundCellRenderer<AADialogCellConfig, AADialogCellLayout>(renderer: cellRender, receiver: cellApply)
        
        titleView.displaysAsynchronously = true
        titleView.ignoreCommonProperties = true
//        titleView.fadeOnAsynchronouslyDisplay = true
        titleView.clearContentsBeforeAsynchronouslyDisplay = true
    
        messageView.displaysAsynchronously = true
        messageView.ignoreCommonProperties = true
//        messageView.fadeOnAsynchronouslyDisplay = true
        messageView.clearContentsBeforeAsynchronouslyDisplay = true
        
        dateView.displaysAsynchronously = true
        dateView.ignoreCommonProperties = true
//        dateView.fadeOnAsynchronouslyDisplay = true
        dateView.clearContentsBeforeAsynchronouslyDisplay = true
        
        counterView.displaysAsynchronously = true
        counterView.ignoreCommonProperties = true
//        counterView.fadeOnAsynchronouslyDisplay = true
        counterView.clearContentsBeforeAsynchronouslyDisplay = true

        counterViewBg.image = AADialogCell.counterBgImage
        
        statusView.contentMode = .Center
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(messageView)
        self.contentView.addSubview(dateView)
        self.contentView.addSubview(statusView)
        self.contentView.addSubview(counterViewBg)
        self.contentView.addSubview(counterView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(item: ACDialog, table: AAManagedTable, index: Int, totalCount: Int) {
        
        //
        // Checking dialog rebinding
        //
        
        // Nothing changed
        if bindedItem == item {
            return
        }
        
        var isRebind: Bool = false
        if let b = bindedItem {
            if b.peer.isEqual(item.peer).boolValue {
                isRebind = true
            }
        }
        self.bindedItem = item
        
        //
        // Avatar View
        //
        avatarView.bind(item.dialogTitle, id: Int(item.peer.peerId), avatar: item.dialogAvatar)
        
        
        // Forcing Async Rendering.
        // This flag can became false when cell was resized
        if !titleView.displaysAsynchronously {
            titleView.displaysAsynchronously = true
        }
        
        if !messageView.displaysAsynchronously {
            messageView.displaysAsynchronously = true
        }

        
        // Reseting Text Layout on new peer binding
        if !isRebind {
            avatarView.alpha = 0
            titleView.alpha = 0
            messageView.alpha = 0
            statusView.alpha = 0
            dateView.alpha = 0
            counterView.alpha = 0
            counterViewBg.alpha = 0
        } else {
            titleView.clearContentsBeforeAsynchronouslyDisplay = false
            messageView.clearContentsBeforeAsynchronouslyDisplay = false
            dateView.clearContentsBeforeAsynchronouslyDisplay = false            
            counterView.clearContentsBeforeAsynchronouslyDisplay = false
        }
        
        
        //
        // Message State
        //
        
        if item.senderId != Actor.myUid() {
            self.statusView.hidden = true
        } else {
            if item.isRead() {
                self.statusView.tintColor = dialogStatusRead
                self.statusView.image = chatIconCheck2
                self.statusView.hidden = false
            } else if item.isReceived() {
                self.statusView.tintColor = dialogStatusReceived
                self.statusView.image = chatIconCheck2
                self.statusView.hidden = false
            } else {
                self.statusView.tintColor = dialogStatusSent
                self.statusView.image = chatIconCheck1
                self.statusView.hidden = false
            }
        }
        
        // Cancelling Renderer and forcing layouting to start new rendering
        cellRenderer.cancelRender()

        setNeedsLayout()
    }
    
    public override func willTransitionToState(state: UITableViewCellStateMask) {
        super.willTransitionToState(state)
        
        if state.contains(UITableViewCellStateMask.ShowingEditControlMask) {
            isEditing = true
        } else {
            isEditing = false
        }
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        // We expect height == 76;
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        //
        // Avatar View
        //
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48)
        
        
        //
        // Title
        //
        
        let titleFrame = CGRectMake(leftPadding, 16, width - leftPadding - /*paddingRight*/(padding + 50), 21)
        UIView.performWithoutAnimation {
            self.titleView.frame = titleFrame
        }
        
        
        //
        // Status Icon
        //

        if (!self.statusView.hidden) {
            statusView.frame = CGRectMake(leftPadding, 44, 20, 18)
        }
        
        
        //
        // Rest of Elements are layouted on the last phase
        //
        
        if bindedItem != nil {
            let config = AADialogCellConfig(
                item: bindedItem!,
                isStatusVisible: !statusView.hidden,
                titleWidth: titleFrame.width,
                contentWidth: width)
            
            if cellRenderer.requestRender(config) {
                
                // Disable async rendering on frame resize to avoid blinking on resize
                titleView.displaysAsynchronously = false
                titleView.clearContentsBeforeAsynchronouslyDisplay = false
                messageView.displaysAsynchronously = false
                messageView.clearContentsBeforeAsynchronouslyDisplay = false
                dateView.displaysAsynchronously = false
                dateView.clearContentsBeforeAsynchronouslyDisplay = false
                counterView.displaysAsynchronously = false
                counterView.clearContentsBeforeAsynchronouslyDisplay = false
            }
        }
    }
    
    private func cellRender(config: AADialogCellConfig) -> AADialogCellLayout! {
        
        //
        // Title Layouting
        //
        
        let title = NSMutableAttributedString(string: config.item.dialogTitle)
        title.yy_font = UIFont.mediumSystemFontOfSize(17)
        title.yy_color = appStyle.dialogTitleColor
        let titleContainer = YYTextContainer(size: CGSize(width: config.titleWidth, height: 1000))
        titleContainer.maximumNumberOfRows = 1
        titleContainer.truncationType = .End
        let titleLayout = YYTextLayout(container: titleContainer, text: title)!
        
        
        //
        // Message Status
        //
        
        var messagePadding: CGFloat = 0
        if config.isStatusVisible {
            messagePadding = 22
        }
        
        
        //
        // Counter
        //
        
        var unreadPadding: CGFloat = 0
        let counterLayout: YYTextLayout?
        if config.item.unreadCount > 0 {
            let counter = NSMutableAttributedString(string: "\(config.item.unreadCount)")
            counter.yy_font = UIFont.systemFontOfSize(14)
            counter.yy_color = appStyle.dialogCounterColor
            counterLayout = YYTextLayout(containerSize: CGSize(width: 1000, height: 1000), text: counter)!
            unreadPadding = max(counterLayout!.textBoundingSize.width + 8, 18)
        } else {
            counterLayout = nil
        }
        
        
        //
        // Message
        //
        
        let message = NSMutableAttributedString(string: Actor.getFormatter().formatDialogText(config.item))
        message.yy_font = UIFont.systemFontOfSize(16)
        if config.item.messageType.ordinal() != ACContentType.TEXT().ordinal() {
            message.yy_color = dialogTextActiveColor
        } else {
            message.yy_color = dialogTextColor
        }
        let messageWidth = config.contentWidth - 76 - 14 - messagePadding - unreadPadding
        let messageContainer = YYTextContainer(size: CGSize(width: messageWidth, height: 1000))
        messageContainer.maximumNumberOfRows = 1
        messageContainer.truncationType = .End
        let messageLayout = YYTextLayout(container: messageContainer, text: message)!
        
        
        //
        // Date
        //
        
        let dateStr: String
        if config.item.date > 0 {
            dateStr = Actor.getFormatter().formatShortDate(config.item.date)
        } else {
            dateStr = ""
        }
        let dateAtrStr = NSMutableAttributedString(string: dateStr)
        dateAtrStr.yy_color = appStyle.dialogDateColor
        dateAtrStr.yy_font = UIFont.systemFontOfSize(14)
        let dateContainer = YYTextContainer(size: CGSize(width: 60, height: 1000))
        let dateLayout = YYTextLayout(container: dateContainer, text: dateAtrStr)!
        
        return AADialogCellLayout(titleLayout: titleLayout, messageLayout: messageLayout, messageWidth: messageWidth, counterLayout: counterLayout, dateLayout: dateLayout)
    }
    
    private func cellApply(render: AADialogCellLayout!) {
        
        //
        // Avatar
        //
        
        presentView(avatarView)
        
        
        //
        // Title
        //
        self.titleView.textLayout = render.titleLayout
        presentView(titleView)
        
        
        let leftPadding: CGFloat
        if isEditing {
            leftPadding = 8
        } else {
            leftPadding = 14
        }
        
        
        //
        // Date
        //
        
        dateView.textLayout = render.dateLayout
        let dateWidth = render.dateLayout.textBoundingSize.width
        dateView.frame = CGRectMake(contentView.width - dateWidth - leftPadding, 18, dateWidth, 18)
        presentView(dateView)
        
        
        //
        // Message
        //
        
        var padding: CGFloat = 76
        if !statusView.hidden {
            padding += 22
        }
        let messageViewFrame = CGRectMake(padding, 44, render.messageWidth, 18)
        UIView.performWithoutAnimation {
            self.messageView.frame = messageViewFrame
        }
        messageView.textLayout = render.messageLayout
        presentView(messageView)
        
        
        //
        // Message State
        //
        if !self.statusView.hidden {
            presentView(self.statusView)
        }
        
        
        //
        // Counter
        //
        
        if render.counterLayout != nil {
            self.counterView.textLayout = render.counterLayout
            
            let textW = render.counterLayout!.textBoundingSize.width
            let unreadW = max(textW + 8, 18)
            
            counterView.frame = CGRectMake(contentView.width - leftPadding - unreadW + (unreadW - textW) / 2, 44, textW, 18)
            counterViewBg.frame = CGRectMake(contentView.width - leftPadding - unreadW, 44, unreadW, 18)
            
            presentView(counterView)
            presentView(counterViewBg)
        } else {
            
            dismissView(counterView)
            dismissView(counterViewBg)
        }
    }
    
    private func presentView(view: UIView) {
        view.alpha = 1
    }
    
    private func dismissView(view: UIView) {
        view.alpha = 0
    }
}

//
// Rendering
//

private class AADialogCellConfig {
    
    let item: ACDialog
    let titleWidth: CGFloat
    let isStatusVisible: Bool
    let contentWidth: CGFloat
    
    init(item: ACDialog, isStatusVisible: Bool, titleWidth: CGFloat, contentWidth: CGFloat) {
        self.item = item
        self.titleWidth = titleWidth
        self.contentWidth = contentWidth
        self.isStatusVisible = isStatusVisible
    }
}

extension AADialogCellConfig: Equatable {
    
}

private func ==(lhs: AADialogCellConfig, rhs: AADialogCellConfig) -> Bool {
    return rhs.titleWidth == lhs.titleWidth && rhs.contentWidth == lhs.contentWidth
}

private class AADialogCellLayout {
    
    let titleLayout: YYTextLayout
    let counterLayout: YYTextLayout?
    let messageLayout: YYTextLayout
    let messageWidth: CGFloat
    let dateLayout: YYTextLayout
    
    init(
        titleLayout: YYTextLayout,
        messageLayout: YYTextLayout,
        messageWidth: CGFloat,
        counterLayout: YYTextLayout?,
        dateLayout: YYTextLayout) {
            self.titleLayout = titleLayout
            self.counterLayout = counterLayout
            self.messageLayout = messageLayout
            self.messageWidth = messageWidth
            self.dateLayout = dateLayout
    }
}



