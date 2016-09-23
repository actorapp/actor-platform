//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AddressBookUI
import MessageUI

open class AABubbleContactCell: AABubbleCell, ABNewPersonViewControllerDelegate, MFMailComposeViewControllerDelegate, UINavigationControllerDelegate {
    
    fileprivate let avatar = AAAvatarView()
    fileprivate let name = UILabel()
    fileprivate let contact = UILabel()
    fileprivate var bindedRecords = [String]()
    fileprivate let tapView = UIView()
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        name.font = UIFont.mediumSystemFontOfSize(17)
        contact.font = UIFont.systemFont(ofSize: 15)
        tapView.backgroundColor = UIColor.clear
        
        contentView.addSubview(avatar)
        contentView.addSubview(name)
        contentView.addSubview(contact)
        contentView.addSubview(tapView)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
        
        tapView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleContactCell.contactDidTap)))
        tapView.isUserInteractionEnabled = true
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func contactDidTap() {
        if let m = bindedMessage {
            if let c = m.content as? ACContactContent {
                let menuBuilder = AAMenuBuilder()
                let phones = c.getPhones()
                for i in 0..<phones!.size() {
                    let p = phones!.getWith(i) as! String
                    menuBuilder.add(p, closure: { () -> () in
                        if let url = URL(string: "tel:\(p)") {
                            if !UIApplication.shared.openURL(url) {
                                self.controller.alertUser("ErrorUnableToCall")
                            }
                        } else {
                            self.controller.alertUser("ErrorUnableToCall")
                        }
                    })
                }
                let emails = c.getEmails()
                for i in 0..<emails!.size() {
                    let e = emails!.getWith(i) as! String
                    menuBuilder.add(e, closure: { () -> () in
                        let emailController = MFMailComposeViewController()
                        emailController.delegate = self
                        emailController.setToRecipients([e])
                        self.controller.present(emailController, animated: true, completion: nil)
                    })
                }
                menuBuilder.add(AALocalized("ProfileAddToContacts"), closure: { () -> () in
                    let add = ABNewPersonViewController()
                    add.newPersonViewDelegate = self
                    
                    let person: ABRecord = ABPersonCreate().takeRetainedValue()
                    let name = c.getName().trim()
                    let nameParts = name.components(separatedBy: " ")
                    ABRecordSetValue(person, kABPersonFirstNameProperty, nameParts[0] as CFTypeRef!, nil)
                    if (nameParts.count >= 2) {
                        let lastName = name.substring(from: nameParts[0].endIndex).trim()
                        ABRecordSetValue(person, kABPersonLastNameProperty, lastName as CFTypeRef!, nil)
                    }
                    
                    if (phones!.size() > 0) {
                        let phonesValues: ABMultiValue = ABMultiValueCreateMutable(UInt32(kABMultiStringPropertyType)).takeRetainedValue()
                        for i in 0..<phones!.size() {
                            let p = phones!.getWith(i) as! String
                            ABMultiValueAddValueAndLabel(phonesValues, p.replace(" ", dest: "") as CFTypeRef!, kABPersonPhoneMainLabel, nil)
                        }
                        ABRecordSetValue(person, kABPersonPhoneProperty, phonesValues, nil)
                    }
                    
                    if (emails!.size() > 0) {
                        let phonesValues: ABMultiValue = ABMultiValueCreateMutable(UInt32(kABMultiStringPropertyType)).takeRetainedValue()
                        for i in 0..<emails!.size() {
                            let p = emails!.getWith(i) as! String
                            ABMultiValueAddValueAndLabel(phonesValues, p.replace(" ", dest: "") as CFTypeRef!, kABPersonPhoneMainLabel, nil)
                        }
                        ABRecordSetValue(person, kABPersonEmailProperty, phonesValues, nil)
                    }
                    
                    add.displayedPerson = person
                    self.controller.present(AANavigationController(rootViewController: add), animated: true, completion: nil)
                })
                
                controller.showActionSheet(menuBuilder.items, cancelButton: "AlertCancel", destructButton: nil, sourceView: tapView, sourceRect: tapView.bounds, tapClosure: menuBuilder.tapClosure)
            }
        }
    }
    
    open func newPersonViewController(_ newPersonView: ABNewPersonViewController, didCompleteWithNewPerson person: ABRecord?) {
        newPersonView.dismiss(animated: true, completion: nil)
    }
    
    open func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
 
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        let contactLayout = cellLayout as! AAContactCellLayout
        
        // Always update bubble insets
        if (isOut) {
            bindBubbleType(.textOut, isCompact: false)
            
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
            bindBubbleType(.textIn, isCompact: false)
            
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
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {
        // Convenience
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        let height = max(44, bindedRecords.count * 18 + 22)
        layoutBubble(200, contentHeight: CGFloat(height))
        
        if (isOut) {
            avatar.frame = CGRect(x: contentWidth - insets.right - 200, y: insets.top, width: 46, height: 46)
            tapView.frame = CGRect(x: contentWidth - insets.left - 200, y: insets.top, width: 200, height: CGFloat(height))
        } else {
            avatar.frame = CGRect(x: insets.left, y: insets.top, width: 44, height: 44)
            tapView.frame = CGRect(x: insets.left, y: insets.top, width: 200, height: CGFloat(height))
        }
        name.frame = CGRect(x: avatar.right + 6, y: insets.top, width: 200 - 58, height: 22)
        contact.frame = CGRect(x: avatar.right + 6, y: insets.top + 22, width: 200 - 58, height: 200)
        contact.sizeToFit()
    }
}

open class AAContactCellLayout: AACellLayout {

    let name: String
    let records: [String]
    
    init(name: String, records: [String], date: Int64, layouter: AABubbleLayouter) {
        self.name = name
        self.records = records
        let height = max(44, records.count * 18 + 22) + 12
        super.init(height: CGFloat(height), date: date, key: "location", layouter: layouter)
    }
}

open class AABubbleContactCellLayouter: AABubbleLayouter {
    open func isSuitable(_ message: ACMessage) -> Bool {
        if (message.content is ACContactContent) {
            return true
        }
        
        return false
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleContactCell.self
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        let content = message.content as! ACContactContent
        var records = [String]()
        for i in 0..<content.getPhones().size() {
            records.append(content.getPhones().getWith(i) as! String)
        }
        for i in 0..<content.getEmails().size() {
            records.append(content.getEmails().getWith(i) as! String)
        }
        return AAContactCellLayout(name: content.getName(), records: records, date: Int64(message.date), layouter: self)
    }
}
