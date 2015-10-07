//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices
import PEPhotoCropEditor

public class AAViewController: UIViewController, UINavigationControllerDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    public var placeholder = BigPlaceholderView(topOffset: 0)
    public var pendingPickClosure: ((image: UIImage) -> ())?
    
    public var avatarHeight: CGFloat = DeviceType.IS_IPHONE_6P ? 336.0 : 256.0
    
    public var popover: UIPopoverController?
    
    // Content type for view tracking
    
    public var content: ACPage?
    
    // Data for views
    
    public var autoTrack: Bool = false {
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
    
    public var uid: Int! {
        didSet {
            if self.uid != nil {
                self.user = Actor.getUserWithUid(jint(self.uid))
                self.isBot = user.isBot().boolValue
            }
        }
    }
    public var user: ACUserVM!
    public var isBot: Bool!
    
    public var gid: Int! {
        didSet {
            if self.gid != nil {
                self.group = Actor.getGroupWithGid(jint(self.gid))
            }
        }
    }
    public var group: ACGroupVM!
    
    public init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    public func showPlaceholderWithImage(image: UIImage?, title: String?, subtitle: String?) {
        placeholder.setImage(image, title: title, subtitle: subtitle)
        showPlaceholder()
    }
    
    public func showPlaceholder() {
        if placeholder.superview == nil {
            placeholder.frame = view.bounds
            view.addSubview(placeholder)
        }
    }
    
    public func hidePlaceholder() {
        if placeholder.superview != nil {
            placeholder.removeFromSuperview()
        }
    }
    
    public func shakeView(view: UIView, originalX: CGFloat) {
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
    
    public func applyScrollUi(tableView: UITableView, cell: UITableViewCell?) {
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
        
        // navigationController?.navigationBar.lt_setBackgroundColor(MainAppTheme.navigation.barSolidColor.alpha(fraction))
    }
    
    public func applyScrollUi(tableView: UITableView) {
        applyScrollUi(tableView, indexPath: NSIndexPath(forRow: 0, inSection: 0))
    }
    
    public func applyScrollUi(tableView: UITableView, indexPath: NSIndexPath) {
        applyScrollUi(tableView, cell: tableView.cellForRowAtIndexPath(indexPath))
    }
    
    public func pickAvatar(takePhoto:Bool, closure: (image: UIImage) -> ()) {
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
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        placeholder.frame = CGRectMake(0, 64, view.bounds.width, view.bounds.height - 64)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = content {
            Analytics.trackPageVisible(c)
        }
        if let u = uid {
            Actor.onProfileOpenWithUid(jint(u))
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = content {
            Analytics.trackPageHidden(c)
        }
        if let u = uid {
            Actor.onProfileClosedWithUid(jint(u))
        }
    }
    
    public func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func presentInNavigation(controller: UIViewController) {
        var navigation = AANavigationController()
        navigation.viewControllers = [controller]
        presentViewController(navigation, animated: true, completion: nil)
    }
}

extension AAViewController: UIImagePickerControllerDelegate, PECropViewControllerDelegate {
    
    public func cropImage(image: UIImage) {
        let cropController = PECropViewController()
        cropController.cropAspectRatio = 1.0
        cropController.keepingCropAspectRatio = true
        cropController.image = image
        cropController.delegate = self
        cropController.toolbarHidden = true
        navigationController!.presentViewController(UINavigationController(rootViewController: cropController), animated: true, completion: nil)
    }
    
    public func cropViewController(controller: PECropViewController!, didFinishCroppingImage croppedImage: UIImage!) {
        if (pendingPickClosure != nil){
            pendingPickClosure!(image: croppedImage)
        }
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public func cropViewControllerDidCancel(controller: PECropViewController!) {
        pendingPickClosure = nil
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        MainAppTheme.navigation.applyStatusBar()
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    public func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        MainAppTheme.navigation.applyStatusBar()
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    public func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        pendingPickClosure = nil
        MainAppTheme.navigation.applyStatusBar()
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

