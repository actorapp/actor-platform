//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class GroupCreateController: UIViewController {
    private var addPhotoButton = UIButton()
    private var avatarImageView = UIImageView()
    private var groupName = UITextField()
    private var titleLabel = UILabel()
    
    init(){
        super.init(nibName: nil, bundle: nil)
        self.navigationItem.title = NSLocalizedString("CreateGroupTitle", comment: "Compose Title")
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
        view.addSubview(titleLabel)
        
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
        
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font = isIPad
            ? UIFont(name: "HelveticaNeue-Thin", size: 50.0)
            : UIFont(name: "HelveticaNeue-Light", size: 30.0)
        titleLabel.text = NSLocalizedString("AuthProfileTitle", comment: "Title")

    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        avatarImageView.frame = CGRectMake(24, 24, 64, 64)
        addPhotoButton.frame = avatarImageView.frame
        
        titleLabel.frame = CGRectMake(80, 24, 320, 64)
    }
}