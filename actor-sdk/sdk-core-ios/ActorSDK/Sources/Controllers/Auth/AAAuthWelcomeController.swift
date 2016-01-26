//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AAWelcomeController: UIViewController {
    
    var backgroundImage : UIImageView!
    
    var logoView : UIImageView!
    var appNameLabel : UILabel!
    var someInfoLabel: UILabel!
    
    var doneButton : UIButton!
    
    
    override func loadView() {
        super.loadView()
        
        self.backgroundImage = UIImageView()
        
        self.logoView = UIImageView()
        self.appNameLabel = UILabel()
        self.someInfoLabel  = UILabel()
        
        self.doneButton = UIButton(type: UIButtonType.System)
        
        //////////////////////////////////////////
        
        let screenWidth = UIScreen.mainScreen().bounds.size.width
        let screenHeight = UIScreen.mainScreen().bounds.size.height
        
        self.backgroundImage.frame = CGRectMake(0, 0, screenWidth, screenHeight)
        self.backgroundImage.backgroundColor = UIColor.whiteColor()
        
        if ActorSDK.sharedActor().useBackgroundOnWelcomeScreen == true {
            
            self.backgroundImage.image = UIImage.bundled("bg_2.jpg")
            
        }
        
        self.logoView.frame = CGRectMake(screenWidth/2-screenWidth/4, 80, screenWidth/2, screenWidth/2)
        
        self.logoView.image = UIImage.bundled("main_logo")
        
        ///
        
        self.appNameLabel.text = ActorSDK.sharedActor().appNameInLocStrings
        self.appNameLabel.textAlignment = .Center
        self.appNameLabel.backgroundColor = UIColor.clearColor()
        self.appNameLabel.font = UIFont(name: "HelveticaNeue-Light", size: 35)
        self.appNameLabel.textColor = ActorSDK.sharedActor().style.vcStarInfoTextColor
        
        self.appNameLabel.frame =  CGRectMake(screenWidth/2-screenWidth/4, self.logoView.frame.maxY + 10, screenWidth/2, 40)
        
        ///
        
        self.someInfoLabel.text = AALocalized("AuthMoreInfo")
        self.someInfoLabel.textAlignment = .Center
        self.someInfoLabel.backgroundColor = UIColor.clearColor()
        self.someInfoLabel.font = UIFont(name: "HelveticaNeue-Light", size: 17)
        self.someInfoLabel.numberOfLines = 0
        self.someInfoLabel.textColor = ActorSDK.sharedActor().style.vcStarInfoTextColor
        
        self.someInfoLabel.frame =  CGRectMake(screenWidth/2-screenWidth/4-45, self.appNameLabel.frame.maxY + 5, screenWidth/2+90, 60)
        
        ///
        self.doneButton.setTitle(AALocalized("AuthStarButton"), forState: UIControlState.Normal)
        self.doneButton.setTitleColor(ActorSDK.sharedActor().style.vcStarButton, forState: UIControlState.Normal)
        self.doneButton.tintColor = ActorSDK.sharedActor().style.vcStarButton
        self.doneButton.frame = CGRectMake(screenWidth/2-screenWidth/4-20, screenHeight-50, screenWidth/2, 30)
        
        self.doneButton.titleLabel!.font = UIFont(name: "HelveticaNeue-Light", size: 17)
        
        self.doneButton.setImage(UIImage.bundled("OnboardNext"), forState: UIControlState.Normal)
        self.doneButton.imageEdgeInsets = UIEdgeInsetsMake(5, screenWidth/2 + 20, 0, 0)
        
        self.doneButton.addTarget(self, action: "doneButtonAction", forControlEvents: UIControlEvents.TouchUpInside)
        
        
        //////////////////////////////////////////
        
        self.view.addSubview(self.backgroundImage)
        
        self.view.addSubview(self.logoView)
        self.view.addSubview(self.appNameLabel)
        self.view.addSubview(self.someInfoLabel)
        
        self.view.addSubview(self.doneButton)
        
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func doneButtonAction() {
        
        self.navigationController?.pushViewController(AAAuthPhoneViewController(), animated: true)
        
    }
    

}
