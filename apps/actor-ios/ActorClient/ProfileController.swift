//
//  ProfileController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 22.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class ProfileController: UIViewController, UIAlertViewDelegate {

    let uid: Int
    var userVm: AMUserVM?
    var binder = Binder()
    
    @IBOutlet weak var contentScroll: UIScrollView!
    @IBOutlet weak var avatarView: AvatarView!
    @IBOutlet weak var nameView: UILabel!
    @IBOutlet weak var settingsLabel: UILabel!
    @IBOutlet weak var notificationsIcon: UIImageView!
    @IBOutlet weak var onlineLabel: UILabel!
    @IBOutlet weak var notificationsSwitch: UISwitch!
    @IBOutlet weak var addToContactButton: UIButtonTable!
    @IBOutlet weak var writeMessageButton: UIButtonTable!
    @IBOutlet weak var footerView: UIView!
    @IBOutlet weak var contactsView: UIView!
    
    init(uid:Int) {
        self.uid = uid
        super.init(nibName: "ProfileController", bundle: nil)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Navigation bar
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Edit, target: self, action: "editName")
        
        // Name
        self.nameView.textColor = Resources.TextPrimaryColor
        self.onlineLabel.textColor = Resources.TextSecondaryColor
        
        // Actions
        self.addToContactButton.setTitleColor(Resources.TintColor, forState: UIControlState.Normal)
        self.addToContactButton.setImage(UIImage(named: "ic_profile_notification")?.tintImage(Resources.SecondaryTint), forState: UIControlState.Normal)
        self.addToContactButton.addTarget(self, action: "addContact", forControlEvents: UIControlEvents.TouchUpInside)
        
        self.writeMessageButton.setTitleColor(Resources.TintColor, forState: UIControlState.Normal)
        self.writeMessageButton.setImage(UIImage(named: "ic_profile_notification")?.tintImage(Resources.SecondaryTint), forState: UIControlState.Normal)
        self.writeMessageButton.addTarget(self, action: "compose", forControlEvents: UIControlEvents.TouchUpInside)
        
        // Settings
        self.settingsLabel.textColor = Resources.TintColor
        self.notificationsIcon.image = UIImage(named: "ic_profile_notification")?.tintImage(Resources.SecondaryTint)
        self.notificationsSwitch.onTintColor = Resources.TintColor
        
        self.userVm = MSG.getUsers().getWithLong(jlong(uid)) as! AMUserVM
        
        binder.bind(userVm!.getName()!, closure: { (value: NSString?) -> () in
            self.navigationItem.title = value! as String
            self.nameView.text = value! as String
        })
        binder.bind(userVm!.getAvatar(), closure: { (value: AMAvatar?)-> () in
            self.avatarView.bind(self.userVm!.getName().get() as! String, id: jint(self.uid), avatar: value)
        })
        
        binder.bind(userVm!.isContact(), closure: { (value:JavaLangBoolean?) -> () in
            if (value!.booleanValue()) {
                self.addToContactButton.setTitle("Remove from Contacts", forState: UIControlState.Normal)
            } else {
                self.addToContactButton.setTitle("Add to Contacts", forState: UIControlState.Normal)
            }
        })
        
        binder.bind(userVm!.getPresence(), closure: { (presence: AMUserPresence?) -> () in
            var stateText = MSG.getFormatter().formatPresenceWithAMUserPresence(presence, withAMSexEnum: self.userVm!.getSex())
            self.onlineLabel.text = stateText;
            var state = UInt(presence!.getState().ordinal())
            if (state == AMUserPresence_State.ONLINE.rawValue) {
                self.onlineLabel.textColor = Resources.TintColor
            } else {
                self.onlineLabel.textColor = Resources.TextSecondaryColor
            }
        })
        
        binder.bind(userVm!.getPhones(), closure: { (value:JavaUtilArrayList?) -> () in
            self.updateLayout(value!);
        })
        
        contentScroll.contentSize = CGSize(width: 0, height: contentScroll.frame.height + 1.0)
    }
    
    func updateLayout(phones: JavaUtilArrayList) {
        while(contactsView.subviews.count > 0){
            (contactsView.subviews[0] as! UIView).removeFromSuperview()
        }
        
        for i in 0..<phones.size() {
            var phone = phones.getWithInt(i) as! AMUserPhone;
            if (i == 0) {
                var iconView = UIImageView(frame: CGRectMake(0, CGFloat(i*48), 66, 48))
                iconView.image = UIImage(named: "ic_profile_phone")?.tintImage(Resources.TintColor)
                iconView.contentMode = UIViewContentMode.Center
                contactsView.addSubview(iconView)
            }
            
            var phoneView = UILabel(frame: CGRectMake(66, CGFloat(i*48 + 22), 320 - 66, 22))
            phoneView.textColor = Resources.TextPrimaryColor
            phoneView.font = UIFont.systemFontOfSize(18)
            phoneView.text = "+\(phone.getPhone())";
            
            var titleView = UILabel(frame: CGRectMake(66, CGFloat(i*48), 320 - 66, 22))
            titleView.textColor = Resources.TintColor
            titleView.font = UIFont.systemFontOfSize(14)
            titleView.text = phone.getTitle();
            
            contactsView.addSubview(titleView)
            contactsView.addSubview(phoneView)
        }
    }
    
    func addContact() {
        if ((self.userVm!.isContact().get() as! JavaLangBoolean).booleanValue()) {
            execute(MSG.removeContactWithInt(jint(uid)))
        } else {
            execute(MSG.addContactWithInt(jint(uid)))
        }
    }
    
    func compose() {
        self.navigationController?.pushViewController(MessagesViewController(peer: AMPeer.userWithInt(jint(self.uid))), animated: true);
    }
    
    func editName() {
        var alertView = UIAlertView(title: "Edit Name", message: "Contact Name", delegate: self, cancelButtonTitle: "Cancel")
        alertView.addButtonWithTitle("Edit")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)?.text =  self.userVm!.getName().get() as! String;
        alertView.textFieldAtIndex(0)?.autocapitalizationType = UITextAutocapitalizationType.Sentences
        alertView.show()
    }
    
    func alertView(alertView: UIAlertView, willDismissWithButtonIndex buttonIndex: Int) {
        if (buttonIndex == 1) {
            execute(MSG.editNameWithInt(jint(self.uid), withNSString: alertView.textFieldAtIndex(0)!.text!))
        }
    }
}
