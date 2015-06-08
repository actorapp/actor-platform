//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class GroupCreateController: AAViewController, UITextFieldDelegate {

    private var addPhotoButton = UIButton()
    private var avatarImageView = UIImageView()
    private var groupName = UITextField()
    private var groupNameFieldSeparator = UIView()

    override init(){
        super.init(nibName: nil, bundle: nil)
        self.navigationItem.title = NSLocalizedString("CreateGroupTitle", comment: "Compose Title")
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationNext", comment: "Next"), style: UIBarButtonItemStyle.Plain, target: self, action: "doNext")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = MainAppTheme.list.bgColor
        view.addSubview(addPhotoButton)
        view.addSubview(avatarImageView)
        view.addSubview(groupName)
        view.addSubview(groupNameFieldSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        var context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor);
        CGContextFillEllipseInRect(context, CGRectMake(0.0, 0.0, 110.0, 110.0));
        CGContextSetStrokeColorWithColor(context, UIColor.RGB(0xd9d9d9).CGColor);
        CGContextSetLineWidth(context, 1.0);
        CGContextStrokeEllipseInRect(context, CGRectMake(0.5, 0.5, 109.0, 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton.exclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, forState: UIControlState.Normal)
        
        var addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = NSLocalizedString("AuthProfileAddPhoto1", comment: "Title")
        addPhotoLabelFirst.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clearColor()
        addPhotoLabelFirst.textColor = UIColor.RGB(0xd9d9d9)
        addPhotoLabelFirst.sizeToFit()
        
        var addPhotoLabelSecond = UILabel()
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
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        
        avatarImageView.frame = CGRectMake((screenSize.width - 80)/2, 24  + 66, 80, 80)
        addPhotoButton.frame = avatarImageView.frame
        
        groupName.frame = CGRectMake(24, 126 + 66, screenSize.width - 48, 56.0)
        groupNameFieldSeparator.frame = CGRectMake(24, 182 + 66, screenSize.width - 48, retinaPixel)
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
        var title = groupName.text.trim()
        if (title.size() == 0) {
            shakeView(groupName, originalX: groupName.frame.origin.x)
            return
        }
        
        navigateNext(GroupMembersController(title: title), removeCurrent: true)
    }
}