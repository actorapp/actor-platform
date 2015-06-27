//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices

class AAViewController: UIViewController {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholder = AAPlaceholderView(topOffset: 0)
    var pendingPickClosure: ((image: UIImage) -> ())?
    
    var avatarHeight: CGFloat = DeviceType.IS_IPHONE_6P ? 336.0 : 256.0
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment: "Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    // MARK: -
    // MARK: Placeholder
    
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
    
    // MARK: -
    // MARK: Methods
    
    func shakeView(view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        var originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
        
        UIView.animateWithDuration(0.05, delay: 0.0, options: UIViewAnimationOptions.Autoreverse, animations: { () -> Void in
            view.frame = rFirst
            }) { (finished) -> Void in
                if (finished) {
                    UIView.animateWithDuration(0.05, delay: 0.0, options: (UIViewAnimationOptions.Repeat | UIViewAnimationOptions.Autoreverse), animations: { () -> Void in
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
        var maxOffset = tableView.frame.width - avatarHeight
        var offset = min(tableView.contentOffset.y, avatarHeight)
        
        if let userCell = cell as? AAUserInfoCell {
            userCell.userAvatarView.frame = CGRectMake(0, offset, tableView.frame.width, avatarHeight - offset)
        } else if let groupCell = cell as? AAConversationGroupInfoCell {
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
        
        var pickerController = AAImagePickerController()
        pickerController.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
        pickerController.mediaTypes = [kUTTypeImage]
        pickerController.view.backgroundColor = MainAppTheme.list.bgColor
        pickerController.navigationBar.tintColor = MainAppTheme.navigation.barColor
        pickerController.delegate = self
        pickerController.navigationBar.tintColor = MainAppTheme.navigation.titleColor
        pickerController.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: MainAppTheme.navigation.titleColor]
        self.navigationController!.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        placeholder.frame = CGRectMake(0, 64, view.bounds.width, view.bounds.height - 64)
    }
    
    // MARK: -
    // MARK: Navigation
    
    func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }

}

extension AAViewController: UIImagePickerControllerDelegate, PECropViewControllerDelegate, UINavigationControllerDelegate {
    
    func cropImage(image: UIImage) {
        var cropController = PECropViewController()
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
    
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        MainAppTheme.navigation.applyStatusBar()
        navigationController!.dismissViewControllerAnimated(true, completion: { () -> Void in
            self.cropImage(image)
        })
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
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

extension AAViewController: UINavigationControllerDelegate {
    
}

