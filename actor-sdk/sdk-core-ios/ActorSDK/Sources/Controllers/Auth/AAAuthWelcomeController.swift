//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYKit
import ElegantPresentations

public class AAWelcomeController: AAViewController {
    
//    var backgroundImage : UIImageView!
    
    let logoView : UIImageView = UIImageView()
    let appNameLabel : UILabel = UILabel()
    let someInfoLabel: UILabel = UILabel()
    let signupButton: UIButton = UIButton()
    let signinButton: UIButton = UIButton()
    
    public override init() {
        super.init(nibName: nil, bundle: nil)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override func loadView() {
        super.loadView()
        
        self.view.backgroundColor = UIColor(red: 94, green: 142, blue: 192)
        
        self.logoView.image = UIImage.bundled("logo_welcome")
        
        appNameLabel.text = "Welcome To Actor!"
        appNameLabel.textAlignment = .Center
        appNameLabel.backgroundColor = UIColor.clearColor()
        appNameLabel.font = UIFont.mediumSystemFontOfSize(24)
        appNameLabel.textColor = UIColor.whiteColor()
        
        someInfoLabel.text = "\"Open messaging that respects you\""
        someInfoLabel.textAlignment = .Center
        someInfoLabel.backgroundColor = UIColor.clearColor()
        someInfoLabel.font = UIFont.systemFontOfSize(16)
        someInfoLabel.numberOfLines = 1
        someInfoLabel.textColor = UIColor.whiteColor()
        
        signupButton.setTitle("Sign Up", forState: .Normal)
        signupButton.titleLabel?.font = UIFont.mediumSystemFontOfSize(17)
        signupButton.setTitleColor(UIColor(red: 94, green: 142, blue: 192), forState: .Normal)
        signupButton.setBackgroundImage(Imaging.roundedImage(UIColor.whiteColor(), radius: 22), forState: .Normal)
        signupButton.setBackgroundImage(Imaging.roundedImage(UIColor.whiteColor().alpha(0.7), radius: 22), forState: .Highlighted)
        signupButton.addTarget(self, action: "signupAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        signinButton.setTitle("Sign In", forState: .Normal)
        signinButton.titleLabel?.font = UIFont.systemFontOfSize(17)
        signinButton.setTitleColor(UIColor.whiteColor(), forState: .Normal)
        signinButton.setTitleColor(UIColor.whiteColor().alpha(0.7), forState: .Highlighted)
        signinButton.addTarget(self, action: "signInAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        self.view.addSubview(self.logoView)
        self.view.addSubview(self.appNameLabel)
        self.view.addSubview(self.someInfoLabel)
        self.view.addSubview(self.signupButton)
        self.view.addSubview(self.signinButton)
    }
    
    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if AADevice.isiPhone4 {
            logoView.frame = CGRectMake((view.width - 90) / 2, 90, 90, 90)
            appNameLabel.frame = CGRectMake((view.width - 300) / 2, logoView.bottom + 30, 300, 29)
            someInfoLabel.frame = CGRectMake((view.width - 300) / 2, appNameLabel.bottom + 8, 300, 46)
            
            signupButton.frame = CGRectMake((view.width - 136) / 2, view.height - 44 - 80, 136, 44)
            signinButton.frame = CGRectMake((view.width - 136) / 2, view.height - 44 - 25, 136, 44)
        } else {
            
            logoView.frame = CGRectMake((view.width - 90) / 2, 145, 90, 90)
            appNameLabel.frame = CGRectMake((view.width - 300) / 2, logoView.bottom + 35, 300, 29)
            someInfoLabel.frame = CGRectMake((view.width - 300) / 2, appNameLabel.bottom + 8, 300, 46)
        
            signupButton.frame = CGRectMake((view.width - 136) / 2, view.height - 44 - 90, 136, 44)
            signinButton.frame = CGRectMake((view.width - 136) / 2, view.height - 44 - 35, 136, 44)
        }
    }
    
    public func signupAction() {
        // TODO: Remove BG after auth?
        UIApplication.sharedApplication().keyWindow?.backgroundColor = UIColor(red: 94, green: 142, blue: 192)
        self.presentElegantViewController(AAAuthNavigationController(rootViewController: AAAuthAskName()))
    }
    
    public func signInAction() {
        
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
     
        // TODO: Fix after cancel?
        UIApplication.sharedApplication().setStatusBarStyle(.LightContent, animated: true)
    }
}
