//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class GroupCreateController: UIViewController {
    private var addPhotoButton: UIButton = UIButton()
    private var avatarImageView: UIImageView = UIImageView()
    private var groupName: UITextField = UITextField()
    
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
        view.addSubview(avatarImageView)
        view.addSubview(addPhotoButton)
        view.addSubview(groupName)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        avatarImageView.frame = CGRectMake(10, 10, 64, 64)
        addPhotoButton.frame = avatarImageView.frame
    }
}