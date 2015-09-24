//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class PhotoPreviewController: NYTPhotosViewController, NYTPhotosViewControllerDelegate {
    
    // let binder = Binder()
    let photos: [PreviewImage]
    let controllerPhotos: [AAPhoto]
    var bind = [Int: CocoaDownloadCallback]()
    let fromView: UIView?
    var startStatusBarStyle: UIStatusBarStyle!
    var statusBarHidden: Bool?
    
    convenience init(photos: [PreviewImage], fromView: UIView?) {
        self.init(photos: photos, initialPhoto: 0, fromView: fromView)
    }
    
    convenience init(photo: PreviewImage, fromView: UIView?) {
        self.init(photos: [photo], fromView: fromView)
    }
    
    convenience init(file: ACFileReference, fromView: UIView?) {
        self.init(photos: [PreviewImage(file: file)], fromView: fromView)
    }
    
    init(photos: [PreviewImage], initialPhoto: Int, fromView: UIView?) {
        
        var converted = [AAPhoto]()
        for p in photos {
            if p.image != nil {
                converted.append(AAPhoto(image: p.image))
                continue
            } else if p.file != nil  {
                let desc = Actor.getDownloadedDescriptorWithFileId(p.file!.getFileId())
                if desc != nil {
                    let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(desc))
                    if img != nil {
                        converted.append(AAPhoto(image: img))
                        continue
                    }
                }
            }
            
            converted.append(AAPhoto(image: p.preview))
        }
        
        self.photos = photos
        self.controllerPhotos = converted
        
        self.fromView = fromView
        
        super.init(photos: converted, initialPhoto: converted[initialPhoto])
        
        self.delegate = self
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Binding files
        for i in 0..<controllerPhotos.count {
            let cp = controllerPhotos[i]
            let p = photos[i]
            
            if cp.image == nil {
                let callback = CocoaDownloadCallback(notDownloaded: { () -> () in
                    
                    }, onDownloading: { (progress) -> () in
                        
                    }, onDownloaded: { (reference) -> () in
                        let image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))
                        dispatchOnUi({ () -> Void in
                            cp.image = image
                            self.updateImageForPhoto(cp)
                        })
                })
                
                Actor.bindRawFileWithReference(p.file!, autoStart: true, withCallback: callback)
                bind[i] = callback
            }
        }
        
        // Save Status Bar style
        startStatusBarStyle = UIApplication.sharedApplication().statusBarStyle
        statusBarHidden = UIApplication.sharedApplication().statusBarHidden
        
        // Changing to black
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
        
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        UIApplication.sharedApplication().setStatusBarHidden(true, withAnimation: .Fade)
    }
    
    func photosViewController(photosViewController: NYTPhotosViewController!, referenceViewForPhoto photo: NYTPhoto!) -> UIView! {
        return self.fromView
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Unbind all
        for i in bind {
            Actor.unbindRawFileWithFileId(photos[i.0].file!.getFileId(), autoCancel: true, withCallback: i.1)
        }
        bind.removeAll()
        
        // Restoring status bar
        UIApplication.sharedApplication().setStatusBarStyle(startStatusBarStyle, animated: true)
        UIApplication.sharedApplication().setStatusBarHidden(statusBarHidden!, withAnimation: .None)
    }
}

class PreviewImage {
    
    let preview: UIImage?
    var image: UIImage?
    let file: ACFileReference?
    
    init(file: ACFileReference, preview: UIImage? = nil) {
        self.file = file
        self.preview = preview
        self.image = nil
    }
    
    init(image: UIImage) {
        self.file = nil
        self.preview = nil
        self.image = image
    }
}