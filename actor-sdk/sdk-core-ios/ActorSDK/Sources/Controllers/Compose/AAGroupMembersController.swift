//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class GroupMembersController: AAContactsListContentController, AAContactsListContentControllerDelegate, CLTokenInputViewDelegate {

    fileprivate var groupTitle: String!
    fileprivate var groupImage: UIImage?
    
    fileprivate var tokenView = CLTokenInputView()
    fileprivate var tokenViewHeight: CGFloat = 48

    fileprivate var selected = [TokenRef]()

    public init(title: String, image: UIImage?) {
        super.init()

        self.searchEnabled = false
        self.delegate = self
        self.groupTitle = title
        self.groupImage = image
        
        navigationItem.title = AALocalized("CreateGroupMembersTitle")
        
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.plain, target: self, action: #selector(self.dismissController))
        }
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.done, target: self, action: #selector(GroupMembersController.doNext))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        super.tableDidLoad()
        
        tokenView.delegate = self
        tokenView.fieldColor = appStyle.vcTokenFieldTextColor
        tokenView.fieldTextColor = appStyle.vcTokenFieldTextColor        
        tokenView.backgroundColor = appStyle.vcTokenFieldBgColor
        tokenView.tintColor = appStyle.vcTokenTintColor
        tokenView.fieldName = ""
        
        let placeholder = AALocalized("CreateGroupMembersPlaceholders")
        let attributedPlaceholder = NSMutableAttributedString(string: placeholder)
        attributedPlaceholder.addAttribute(NSForegroundColorAttributeName, value: appStyle.vcHintColor, range: NSRange(location: 0, length: placeholder.length))
        tokenView.placeholderAttributedText = attributedPlaceholder
        
        self.view.addSubview(tokenView)
        
        tableView.keyboardDismissMode = UIScrollViewKeyboardDismissMode.onDrag
    }
    
    open func contactDidTap(_ controller: AAContactsListContentController, contact: ACContact) -> Bool {
        
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.contact.uid == contact.uid) {
                selected.remove(at: i)
                tokenView.remove(n.token)
                return true
            }
        }
        
        let token = CLToken(displayText: contact.name, context: nil)
        tokenView.add(token)
        selected.append(TokenRef(contact: contact, token: token))
        
        contactRows.filter("")

        return true
    }
    
    open func doNext() {
        let res = IOSIntArray(length: UInt(selected.count))
        for i in 0..<selected.count {
            res?.replaceInt(at: UInt(i), with: selected[i].contact.uid)
        }
        
        executePromise(Actor.createGroup(withTitle: groupTitle, withAvatar: nil, withUids: res)).then { (res: JavaLangInteger!) in
            let gid = res.int32Value
            if self.groupImage != nil {
                Actor.changeGroupAvatar(gid, image: self.groupImage!)
            }
            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.group(with: gid)) {
                self.navigateDetail(customController)
            } else {
                self.navigateDetail(ConversationViewController(peer: ACPeer.group(with: gid)))
            }
            self.dismissController()
        }
    }
    
    // Handling token input updates
    
    open func tokenInputView(_ view: CLTokenInputView, didChangeText text: String?) {
        contactRows.filter(text!)
    }
    
    open func tokenInputView(_ view: CLTokenInputView, didChangeHeightTo height: CGFloat) {
        tokenViewHeight = height
        
        self.view.setNeedsLayout()
    }
    
    open func tokenInputView(_ view: CLTokenInputView, didRemove token: CLToken) {
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.token == token) {
                selected.remove(at: i)
                return
            }
        }
    }

    // Hacking layout 
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if AADevice.isiPad {
            tokenView.frame = CGRect(x: 0, y: 44, width: view.frame.width, height: tokenViewHeight)
            tableView.frame = CGRect(x: 0, y: tokenViewHeight, width: view.frame.width, height: view.frame.height - tokenViewHeight)
        } else {
            tokenView.frame = CGRect(x: 0, y: 64, width: view.frame.width, height: tokenViewHeight)
            tableView.frame = CGRect(x: 0, y: tokenViewHeight, width: view.frame.width, height: view.frame.height - tokenViewHeight)
        }
    }
}

private class TokenRef {
    var contact: ACContact
    var token: CLToken
    
    init(contact: ACContact, token: CLToken) {
        self.contact = contact
        self.token = token
    }
}
