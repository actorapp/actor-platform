//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAGroupCreateViewController: AAViewController, UITextFieldDelegate {

    fileprivate let isChannel: Bool
    fileprivate var addPhotoButton = UIButton()
    fileprivate var avatarImageView = UIImageView()
    fileprivate var hint = UILabel()
    
    fileprivate var groupName = UITextField()
    fileprivate var groupNameFieldSeparator = UIView()
    
    fileprivate var image: UIImage?

    public init(isChannel: Bool) {
        self.isChannel = isChannel
        super.init(nibName: nil, bundle: nil)
        if isChannel {
            self.navigationItem.title = AALocalized("CreateChannelTitle")
        } else {
            self.navigationItem.title = AALocalized("CreateGroupTitle")
        }
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.plain, target: self, action: #selector(self.dismissController))
        }
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.done, target: self, action: #selector(AAGroupCreateViewController.doNext))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = appStyle.vcBgColor
        view.addSubview(addPhotoButton)
        view.addSubview(avatarImageView)
        view.addSubview(hint)
        view.addSubview(groupName)
        view.addSubview(groupNameFieldSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        let context = UIGraphicsGetCurrentContext();
        context?.setFillColor(appStyle.composeAvatarBgColor.cgColor);
        context?.fillEllipse(in: CGRect(x: 0.0, y: 0.0, width: 110.0, height: 110.0));
        context?.setStrokeColor(appStyle.composeAvatarBorderColor.cgColor);
        context?.setLineWidth(1.0);
        context?.strokeEllipse(in: CGRect(x: 0.5, y: 0.5, width: 109.0, height: 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton.isExclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, for: UIControlState())
        addPhotoButton.addTarget(self, action: #selector(AAGroupCreateViewController.photoTap), for: UIControlEvents.touchUpInside)
        
        let addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = AALocalized("ActionAddPhoto1")
        addPhotoLabelFirst.font = UIFont.systemFont(ofSize: 15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clear
        addPhotoLabelFirst.textColor = appStyle.composeAvatarTextColor
        addPhotoLabelFirst.sizeToFit()
        
        let addPhotoLabelSecond = UILabel()
        addPhotoLabelSecond.text = AALocalized("ActionAddPhoto2")
        addPhotoLabelSecond.font = UIFont.systemFont(ofSize: 15.0)
        addPhotoLabelSecond.backgroundColor = UIColor.clear
        addPhotoLabelSecond.textColor = appStyle.composeAvatarTextColor
        addPhotoLabelSecond.sizeToFit()
        
        addPhotoButton.addSubview(addPhotoLabelFirst)
        addPhotoButton.addSubview(addPhotoLabelSecond)
        
        addPhotoLabelFirst.frame = CGRect(x: (80 - addPhotoLabelFirst.frame.size.width) / 2, y: 22, width: addPhotoLabelFirst.frame.size.width, height: addPhotoLabelFirst.frame.size.height).integral;
        addPhotoLabelSecond.frame = CGRect(x: (80 - addPhotoLabelSecond.frame.size.width) / 2, y: 22 + 22, width: addPhotoLabelSecond.frame.size.width, height: addPhotoLabelSecond.frame.size.height).integral;
        
        groupName.backgroundColor = appStyle.vcBgColor
        groupName.textColor = ActorSDK.sharedActor().style.cellTextColor
        groupName.font = UIFont.systemFont(ofSize: 20)
        groupName.keyboardType = UIKeyboardType.default
        groupName.returnKeyType = UIReturnKeyType.next
        if isChannel {
            groupName.attributedPlaceholder = NSAttributedString(string: AALocalized("CreateChannelNamePlaceholder"), attributes: [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.vcHintColor])
        } else {
            groupName.attributedPlaceholder = NSAttributedString(string: AALocalized("CreateGroupNamePlaceholder"), attributes: [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.vcHintColor])
        }
        groupName.delegate = self
        groupName.contentVerticalAlignment = UIControlContentVerticalAlignment.center
        groupName.autocapitalizationType = UITextAutocapitalizationType.words
        groupName.keyboardAppearance = appStyle.isDarkApp ? .dark : .light
        
        groupNameFieldSeparator.backgroundColor = appStyle.vcSeparatorColor
        
        if isChannel {
            hint.text = AALocalized("CreateChannelHint")
        } else {
            hint.text = AALocalized("CreateGroupHint")
        }
        
        hint.font = UIFont.systemFont(ofSize: 15)
        hint.lineBreakMode = .byWordWrapping
        hint.numberOfLines = 0
        hint.textColor = ActorSDK.sharedActor().style.vcHintColor
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        avatarImageView.frame = CGRect(x: 20, y: 20 + 66, width: 80, height: 80)
        addPhotoButton.frame = avatarImageView.frame
        hint.frame = CGRect(x: 120, y: 20 + 66, width: view.width - 140, height: 80)
        
        groupName.frame = CGRect(x: 20, y: 106 + 66, width: view.width - 20, height: 56.0)
        groupNameFieldSeparator.frame = CGRect(x: 20, y: 156 + 66, width: view.width - 20, height: 0.5)
    }
    
    open func photoTap() {
        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera)
        self.showActionSheet(hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
            cancelButton: "AlertCancel",
            destructButton: self.avatarImageView.image != nil ? "PhotoRemove" : nil,
            sourceView: self.view,
            sourceRect: self.view.bounds,
            tapClosure: { (index) -> () in
                if index == -2 {
                    self.avatarImageView.image = nil
                    self.image = nil
                } else if index >= 0 {
                    let takePhoto: Bool = (index == 0) && hasCamera
                    self.pickAvatar(takePhoto, closure: { (image) -> () in
                        self.image = image
                        self.avatarImageView.image = image.roundImage(80)
                    })
                }
        })
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)   
        groupName.becomeFirstResponder()
    }

    open func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        doNext()
        return false
    }
    
    open func doNext() {
        let title = groupName.text!.trim()
        if (title.length == 0) {
            shakeView(groupName, originalX: groupName.frame.origin.x)
            return
        }
        
        groupName.resignFirstResponder()
        
        if isChannel {
            executePromise(Actor.createChannel(withTitle: title, withAvatar: nil)).then({ (gid: JavaLangInteger!) in
                self.navigateNext(AAGroupTypeViewController(gid: Int(gid.intValue()), isCreation: true), removeCurrent: true)
            })
        } else {
            navigateNext(GroupMembersController(title: title, image: image), removeCurrent: true)
        }
    }
}
