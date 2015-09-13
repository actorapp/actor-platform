//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class GroupCreateViewController: AAViewController, UITextFieldDelegate {

    private var addPhotoButton = UIButton()
    private var avatarImageView = UIImageView()
    private var hint = UILabel()
    
    private var groupName = UITextField()
    private var groupNameFieldSeparator = UIView()
    
    private var image: UIImage?

    override init(){
        super.init(nibName: nil, bundle: nil)
        self.navigationItem.title = NSLocalizedString("CreateGroupTitle", comment: "Compose Title")
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized("NavigationNext"), style: UIBarButtonItemStyle.Plain, target: self, action: "doNext")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = MainAppTheme.list.bgColor
        view.addSubview(addPhotoButton)
        view.addSubview(avatarImageView)
        view.addSubview(hint)
        view.addSubview(groupName)
        view.addSubview(groupNameFieldSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        let context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor);
        CGContextFillEllipseInRect(context, CGRectMake(0.0, 0.0, 110.0, 110.0));
        CGContextSetStrokeColorWithColor(context, UIColor.RGB(0xd9d9d9).CGColor);
        CGContextSetLineWidth(context, 1.0);
        CGContextStrokeEllipseInRect(context, CGRectMake(0.5, 0.5, 109.0, 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton.exclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, forState: UIControlState.Normal)
        addPhotoButton.addTarget(self, action: "photoTap", forControlEvents: UIControlEvents.TouchUpInside)
        
        let addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = NSLocalizedString("AuthProfileAddPhoto1", comment: "Title")
        addPhotoLabelFirst.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clearColor()
        addPhotoLabelFirst.textColor = UIColor.RGB(0xd9d9d9)
        addPhotoLabelFirst.sizeToFit()
        
        let addPhotoLabelSecond = UILabel()
        addPhotoLabelSecond.text = NSLocalizedString("AuthProfileAddPhoto2", comment: "Title")
        addPhotoLabelSecond.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelSecond.backgroundColor = UIColor.clearColor()
        addPhotoLabelSecond.textColor = UIColor.RGB(0xd9d9d9)
        addPhotoLabelSecond.sizeToFit()
        
        addPhotoButton.addSubview(addPhotoLabelFirst)
        addPhotoButton.addSubview(addPhotoLabelSecond)
        
        addPhotoLabelFirst.frame = CGRectIntegral(CGRectMake((80 - addPhotoLabelFirst.frame.size.width) / 2, 22, addPhotoLabelFirst.frame.size.width, addPhotoLabelFirst.frame.size.height));
        addPhotoLabelSecond.frame = CGRectIntegral(CGRectMake((80 - addPhotoLabelSecond.frame.size.width) / 2, 22 + 22, addPhotoLabelSecond.frame.size.width, addPhotoLabelSecond.frame.size.height));
        
//        groupName.backgroundColor = UIColor.whiteColor()
        groupName.backgroundColor = MainAppTheme.list.bgColor
        groupName.textColor = MainAppTheme.list.textColor
        groupName.font = UIFont.systemFontOfSize(20)
        groupName.keyboardType = UIKeyboardType.Default
        groupName.returnKeyType = UIReturnKeyType.Next
        groupName.attributedPlaceholder = NSAttributedString(string: NSLocalizedString("CreateGroupNamePlaceholder", comment: "Enter group title"), attributes: [NSForegroundColorAttributeName: MainAppTheme.list.hintColor])
        groupName.delegate = self
        groupName.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        groupName.autocapitalizationType = UITextAutocapitalizationType.Words
        
        groupNameFieldSeparator.backgroundColor = MainAppTheme.list.separatorColor
        
        hint.text = localized("CreateGroupHint")
        hint.font = UIFont.systemFontOfSize(15)
        hint.lineBreakMode = .ByWordWrapping
        hint.numberOfLines = 0
        hint.textColor = MainAppTheme.list.hintColor
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        
        avatarImageView.frame = CGRectMake(20, 20 + 66, 80, 80)
        addPhotoButton.frame = avatarImageView.frame
        hint.frame = CGRectMake(120, 20 + 66, screenSize.width - 140, 80)
        
        groupName.frame = CGRectMake(20, 106 + 66, screenSize.width - 20, 56.0)
        groupNameFieldSeparator.frame = CGRectMake(20, 156 + 66, screenSize.width - 20, 1)
    }
    
    func photoTap() {
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
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        groupName.becomeFirstResponder()
    }
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        doNext()
        return false
    }
    
    func doNext() {
        let title = groupName.text!.trim()
        if (title.length == 0) {
            shakeView(groupName, originalX: groupName.frame.origin.x)
            return
        }
        
        navigateNext(GroupMembersController(title: title, image: image), removeCurrent: true)
    }
}