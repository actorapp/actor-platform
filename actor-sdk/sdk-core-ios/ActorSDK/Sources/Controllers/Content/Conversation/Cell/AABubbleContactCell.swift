//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AddressBookUI
import MessageUI

public class AABubbleContactCell: AABubbleCell, ABNewPersonViewControllerDelegate, MFMailComposeViewControllerDelegate, UINavigationControllerDelegate {
    
    private let avatar = AAAvatarView()
    private let name = UILabel()
    private let contact = UILabel()
    private var bindedRecords = [String]()
    private let tapView = UIView()
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        name.font = UIFont.mediumSystemFontOfSize(17)
        contact.font = UIFont.systemFontOfSize(15)
        tapView.backgroundColor = UIColor.clearColor()
        
        contentView.addSubview(avatar)
        contentView.addSubview(name)
        contentView.addSubview(contact)
        contentView.addSubview(tapView)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
        
        tapView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleContactCell.contactDidTap)))
        tapView.userInteractionEnabled = true
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func contactDidTap() {
        if let m = bindedMessage {
            if let c = m.content as? ACContactContent {
                let menuBuilder = AAMenuBuilder()
                let phones = c.getPhones()
                for i in 0..<phones.size() {
                    let p = phones.getWithInt(i) as! String
                    menuBuilder.add(p, closure: { () -> () in
                        if let url = NSURL(string: "tel:\(p)") {
                            if !UIApplication.sharedApplication().openURL(url) {
                                self.controller.alertUser("ErrorUnableToCall")
                            }
                        } else {
                            self.controller.alertUser("ErrorUnableToCall")
                        }
                    })
                }
                let emails = c.getEmails()
                for i in 0..<emails.size() {
                    let e = emails.getWithInt(i) as! String
                    menuBuilder.add(e, closure: { () -> () in
                        let emailController = MFMailComposeViewController()
                        emailController.delegate = self
                        emailController.setToRecipients([e])
                        self.controller.presentViewController(emailController, animated: true, completion: nil)
                    })
                }
                menuBuilder.add(AALocalized("ProfileAddToContacts"), closure: { () -> () in
                    let add = ABNewPersonViewController()
                    add.newPersonViewDelegate = self
                    
                    let person: ABRecordRef = ABPersonCreate().takeRetainedValue()
                    let name = c.getName().trim()
                    let nameParts = name.componentsSeparatedByString(" ")
                    ABRecordSetValue(person, kABPersonFirstNameProperty, nameParts[0], nil)
                    if (nameParts.count >= 2) {
                        let lastName = name.substringFromIndex(nameParts[0].endIndex).trim()
                        ABRecordSetValue(person, kABPersonLastNameProperty, lastName, nil)
                    }
                    
                    if (phones.size() > 0) {
                        let phonesValues: ABMultiValueRef = ABMultiValueCreateMutable(UInt32(kABMultiStringPropertyType)).takeRetainedValue()
                        for i in 0..<phones.size() {
                            let p = phones.getWithInt(i) as! String
                            ABMultiValueAddValueAndLabel(phonesValues, p.replace(" ", dest: ""), kABPersonPhoneMainLabel, nil)
                        }
                        ABRecordSetValue(person, kABPersonPhoneProperty, phonesValues, nil)
                    }
                    
                    if (emails.size() > 0) {
                        let phonesValues: ABMultiValueRef = ABMultiValueCreateMutable(UInt32(kABMultiStringPropertyType)).takeRetainedValue()
                        for i in 0..<emails.size() {
                            let p = emails.getWithInt(i) as! String
                            ABMultiValueAddValueAndLabel(phonesValues, p.replace(" ", dest: ""), kABPersonPhoneMainLabel, nil)
                        }
                        ABRecordSetValue(person, kABPersonEmailProperty, phonesValues, nil)
                    }
                    
                    add.displayedPerson = person
                    self.controller.presentViewController(AANavigationController(rootViewController: add), animated: true, completion: nil)
                })
                
                controller.showActionSheet(menuBuilder.items, cancelButton: "AlertCancel", destructButton: nil, sourceView: tapView, sourceRect: tapView.bounds, tapClosure: menuBuilder.tapClosure)
            }
        }
    }
    
    public func newPersonViewController(newPersonView: ABNewPersonViewController, didCompleteWithNewPerson person: ABRecord?) {
        newPersonView.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
 
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
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
            tapView.frame = CGRectMake(contentWidth - insets.left - 200, insets.top, 200, CGFloat(height))
        } else {
            avatar.frame = CGRectMake(insets.left, insets.top, 44, 44)
            tapView.frame = CGRectMake(insets.left, insets.top, 200, CGFloat(height))
        }
        name.frame = CGRectMake(avatar.right + 6, insets.top, 200 - 58, 22)
        contact.frame = CGRectMake(avatar.right + 6, insets.top + 22, 200 - 58, 200)
        contact.sizeToFit()
    }
}

public class AAContactCellLayout: AACellLayout {

    let name: String
    let records: [String]
    
    init(name: String, records: [String], date: Int64, layouter: AABubbleLayouter) {
        self.name = name
        self.records = records
        let height = max(44, records.count * 18 + 22) + 12
        super.init(height: CGFloat(height), date: date, key: "location", layouter: layouter)
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
        return AAContactCellLayout(name: content.getName(), records: records, date: Int64(message.date), layouter: self)
    }
}