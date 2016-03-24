//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAGroupCreateViewController: AAViewController, UITextFieldDelegate {

    private var addPhotoButton = UIButton()
    private var avatarImageView = UIImageView()
    private var hint = UILabel()
    
    private var groupName = UITextField()
    private var groupNameFieldSeparator = UIView()
    
    private var image: UIImage?

    public override init(){
        super.init(nibName: nil, bundle: nil)
        self.navigationItem.title = AALocalized("CreateGroupTitle")
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: #selector(AAViewController.dismiss))
        }
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.Plain, target: self, action: #selector(AAGroupCreateViewController.doNext))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = appStyle.vcBgColor
        view.addSubview(addPhotoButton)
        view.addSubview(avatarImageView)
        view.addSubview(hint)
        view.addSubview(groupName)
        view.addSubview(groupNameFieldSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        let context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, appStyle.composeAvatarBgColor.CGColor);
        CGContextFillEllipseInRect(context, CGRectMake(0.0, 0.0, 110.0, 110.0));
        CGContextSetStrokeColorWithColor(context, appStyle.composeAvatarBorderColor.CGColor);
        CGContextSetLineWidth(context, 1.0);
        CGContextStrokeEllipseInRect(context, CGRectMake(0.5, 0.5, 109.0, 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton.exclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, forState: UIControlState.Normal)
        addPhotoButton.addTarget(self, action: #selector(AAGroupCreateViewController.photoTap), forControlEvents: UIControlEvents.TouchUpInside)
        
        let addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = AALocalized("ActionAddPhoto1")
        addPhotoLabelFirst.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clearColor()
        addPhotoLabelFirst.textColor = appStyle.composeAvatarTextColor
        addPhotoLabelFirst.sizeToFit()
        
        let addPhotoLabelSecond = UILabel()
        addPhotoLabelSecond.text = AALocalized("ActionAddPhoto2")
        addPhotoLabelSecond.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelSecond.backgroundColor = UIColor.clearColor()
        addPhotoLabelSecond.textColor = appStyle.composeAvatarTextColor
        addPhotoLabelSecond.sizeToFit()
        
        addPhotoButton.addSubview(addPhotoLabelFirst)
        addPhotoButton.addSubview(addPhotoLabelSecond)
        
        addPhotoLabelFirst.frame = CGRectIntegral(CGRectMake((80 - addPhotoLabelFirst.frame.size.width) / 2, 22, addPhotoLabelFirst.frame.size.width, addPhotoLabelFirst.frame.size.height));
        addPhotoLabelSecond.frame = CGRectIntegral(CGRectMake((80 - addPhotoLabelSecond.frame.size.width) / 2, 22 + 22, addPhotoLabelSecond.frame.size.width, addPhotoLabelSecond.frame.size.height));
        
        groupName.backgroundColor = appStyle.vcBgColor
        groupName.textColor = ActorSDK.sharedActor().style.cellTextColor
        groupName.font = UIFont.systemFontOfSize(20)
        groupName.keyboardType = UIKeyboardType.Default
        groupName.returnKeyType = UIReturnKeyType.Next
        groupName.attributedPlaceholder = NSAttributedString(string: AALocalized("CreateGroupNamePlaceholder"), attributes: [NSForegroundColorAttributeName: ActorSDK.sharedActor().style.vcHintColor])
        groupName.delegate = self
        groupName.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        groupName.autocapitalizationType = UITextAutocapitalizationType.Words
        groupName.keyboardAppearance = appStyle.isDarkApp ? .Dark : .Light
        
        groupNameFieldSeparator.backgroundColor = appStyle.vcSeparatorColor
        
        hint.text = AALocalized("CreateGroupHint")
        hint.font = UIFont.systemFontOfSize(15)
        hint.lineBreakMode = .ByWordWrapping
        hint.numberOfLines = 0
        hint.textColor = ActorSDK.sharedActor().style.vcHintColor
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        avatarImageView.frame = CGRectMake(20, 20 + 66, 80, 80)
        addPhotoButton.frame = avatarImageView.frame
        hint.frame = CGRectMake(120, 20 + 66, view.width - 140, 80)
        
        groupName.frame = CGRectMake(20, 106 + 66, view.width - 20, 56.0)
        groupNameFieldSeparator.frame = CGRectMake(20, 156 + 66, view.width - 20, 0.5)
    }
    
    public func photoTap() {
        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
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
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        groupName.becomeFirstResponder()
    }
    
//    public override func viewDidAppear(animated: Bool) {
//        super.viewDidAppear(animated)
//        
//        groupName.becomeFirstResponder()
//    }
    
    public func textFieldShouldReturn(textField: UITextField) -> Bool {
        doNext()
        return false
    }
    
    public func doNext() {
        let title = groupName.text!.trim()
        if (title.length == 0) {
            shakeView(groupName, originalX: groupName.frame.origin.x)
            return
        }
        
        navigateNext(GroupMembersController(title: title, image: image), removeCurrent: true)
    }
}