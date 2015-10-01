//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices

class AAViewController: UIViewController, UINavigationControllerDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholder = BigPlaceholderView(topOffset: 0)
    var pendingPickClosure: ((image: UIImage) -> ())?
    
    var avatarHeight: CGFloat = DeviceType.IS_IPHONE_6P ? 336.0 : 256.0
    
    var popover: UIPopoverController?
    
    // Content type for view tracking
    
    var content: ACPage?
    
    // Data for views
    
    var autoTrack: Bool = false {
        didSet {
            if self.autoTrack {
                if let u = self.uid {
                    content = ACAllEvents_Profile_viewWithInt_(jint(u))
                }
                if let g = self.gid {
                    content = ACAllEvents_Group_viewWithInt_(jint(g))
                }
            }
        }
    }
    
    var uid: Int! {
        didSet {
            if self.uid != nil {
                self.user = Actor.getUserWithUid(jint(self.uid))
                self.isBot = user.isBot().boolValue
            }
        }
    }
    var user: ACUserVM!
    var isBot: Bool!
    
    var gid: Int! {
        didSet {
            if self.gid != nil {
                self.group = Actor.getGroupWithGid(jint(self.gid))
            }
        }
    }
    var group: ACGroupVM!
    
    init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    func showPlaceholderWithImage(image: UIImage?, title: String?, subtitle: String?) {
        placeholder.setImage(image, title: title, subtitle: subtitle)
        showPlaceholder()
    }
    
    func showPlaceholder() {
        if placeholder.superview == nil {
            placeholder.frame = view.bounds
            view.addSubview(placeholder)
        }
    }
    
    func hidePlaceholder() {
        if placeholder.superview != nil {
            placeholder.removeFromSuperview()
        }
    }
    
    func shakeView(view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        let originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
        
        UIView.animateWithDuration(0.05, delay: 0.0, options: .Autoreverse, animations: { () -> Void in
            view.frame = rFirst
            }) { (finished) -> Void in
                if (finished) {
                    UIView.animateWithDuration(0.05, delay: 0.0, options: [.Repeat, .Autoreverse], animations: { () -> Void in
                        UIView.setAnimationRepeatCount(3)
                        view.frame = r
                        }, completion: { (finished) -> Void in
                            view.frame = originalFrame
                    })
                } else {
                    view.frame = originalFrame
                }
        }
    }
    
    func applyScrollUi(tableView: UITableView, cell: UITableViewCell?) {
        let offset = min(tableView.contentOffset.y, avatarHeight)
        
        if let userCell = cell as? UserPhotoCell {
            userCell.userAvatarView.frame = CGRectMake(0, offset, tableView.frame.width, avatarHeight - offset)
        } else if let groupCell = cell as? GroupPhotoCell {
            groupCell.groupAvatarView.frame = CGRectMake(0, offset, tableView.frame.width, avatarHeight - offset)
        }
        
        var fraction: Double = 0
        if (offset > 0) {
            if (offset > avatarHeight - 64) {
                fraction = 1
            } else {
                fraction = Double(offset) / (Double(avatarHeight) - 64)
            }
        }
        
        navigationController?.navigationBar.lt_setBackgroundColor(MainAppTheme.navigation.barSolidColor.alpha(fraction))
    }
    
    func applyScrollUi(tableView: UITableView) {
        applyScrollUi(tableView, indexPath: NSIndexPath(forRow: 0, inSection: 0))
    }
    
    func applyScrollUi(tableView: UITableView, indexPath: NSIndexPath) {
        applyScrollUi(tableView, cell: tableView.cellForRowAtIndexPath(indexPath))
    }
    
    func pickAvatar(takePhoto:Bool, closure: (image: UIImage) -> ()) {
        self.pendingPickClosure = closure
        
        let pickerController = AAImagePickerController()
        pickerController.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
        pickerController.mediaTypes = [kUTTypeImage as String]
        pickerController.view.backgroundColor = MainAppTheme.list.bgColor
        pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
        pickerController.delegate = self
        pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
        pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
        self.navigationController!.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        placeholder.frame = CGRectMake(0, 64, view.bounds.width, view.bounds.height - 64)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = content {
            Analytics.trackPageVisible(c)
        }
        if let u = uid {
            Actor.onProfileOpenWithUid(jint(u))
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = content {
            Analytics.trackPageHidden(c)
        }
        if let u = uid {
            Actor.onProfileClosedWithUid(jint(u))
        }
    }
    
    func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    func presentInNavigation(controller: UIViewController) {
        var navigation = AANavigationController()
        navigation.viewControllers = [controller]
        presentViewController(navigation, animated: true, completion: nil)
    }
}

extension AAViewController: UIImagePickerControllerDelegate, PECropViewControllerDelegate {
    
    func cropImage(image: UIImage) {
        let cropController = PECropViewController()
        cropController.cropAspectRatio = 1.0
        cropController.keepingCropAspectRatio = true
        cropController.image = image
        cropController.delegate = self
        cropController.toolbarHidden = true
        navigationController!.presentViewController(UINavigationController(rootViewController: cropController), animated: true, completion: nil)
    }
    
    func cropViewController(controller: PECropViewController!, didFinishCroppingImage croppedImage: UIImage!) {
        if (pendingPickClosure != nil){
            pendingPickClosure!(image: croppedImage)
        }
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func cropViewControllerDidCancel(controller: PECropViewController!) {
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        MainAppTheme.navigation.applyStatusBar()
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        pendingPickClosure = nil
        MainAppTheme.navigation.applyStatusBar()
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

