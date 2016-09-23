//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAWallpapperPreviewController: AAViewController {
    
    fileprivate let imageView = UIImageView()
    fileprivate let cancelButton = UIButton()
    fileprivate let setButton = UIButton()
    
    fileprivate let imageName: String
    fileprivate let selectedImage: UIImage
    fileprivate var fromName: Bool
    
    public init(imageName: String) {
        self.imageName = imageName
        self.selectedImage = UIImage()
        self.fromName = true
        super.init()
        imageView.image = UIImage.bundled(imageName)!
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        cancelButton.backgroundColor = appStyle.vcPanelBgColor
        cancelButton.addTarget(self, action: #selector(AAWallpapperPreviewController.cancelDidTap), for: .touchUpInside)
        cancelButton.setTitle(AALocalized("AlertCancel"), for: UIControlState())
        cancelButton.setTitleColor(appStyle.tabUnselectedTextColor, for: UIControlState())
        setButton.backgroundColor = appStyle.vcPanelBgColor
        setButton.addTarget(self, action: #selector(AAWallpapperPreviewController.setDidTap), for: .touchUpInside)
        setButton.setTitle(AALocalized("AlertSet"), for: UIControlState())
        setButton.setTitleColor(appStyle.tabUnselectedTextColor, for: UIControlState())
    }
    
    public init(selectedImage: UIImage) {
        self.selectedImage = selectedImage
        self.imageName = ""
        self.fromName = false
        super.init()
        imageView.image = selectedImage
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        cancelButton.backgroundColor = appStyle.vcPanelBgColor
        cancelButton.addTarget(self, action: #selector(AAWallpapperPreviewController.cancelDidTap), for: .touchUpInside)
        cancelButton.setTitle(AALocalized("AlertCancel"), for: UIControlState())
        cancelButton.setTitleColor(appStyle.tabUnselectedTextColor, for: UIControlState())
        setButton.backgroundColor = appStyle.vcPanelBgColor
        setButton.addTarget(self, action: #selector(AAWallpapperPreviewController.setDidTap), for: .touchUpInside)
        setButton.setTitle(AALocalized("AlertSet"), for: UIControlState())
        setButton.setTitleColor(appStyle.tabUnselectedTextColor, for: UIControlState())
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.edgesForExtendedLayout = UIRectEdge.top
        
        view.addSubview(imageView)
        view.addSubview(cancelButton)
        view.addSubview(setButton)
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        imageView.frame = view.bounds
        
        cancelButton.frame = CGRect(x: 0, y: view.height - 55, width: view.width / 2, height: 55)
        setButton.frame = CGRect(x: view.width / 2, y: view.height - 55, width: view.width / 2, height: 55)
    }
    
    func cancelDidTap() {
        self.dismiss(animated: true, completion: nil)
    }
    
    func setDidTap() {
        self.dismiss(animated: true, completion: nil)
        
        if self.fromName == true {
            Actor.changeSelectedWallpaper("local:\(imageName)")
            
        } else {
            dispatchBackground({ () -> Void in
                
                let descriptor = "/tmp/customWallpaperImage"
                let path = CocoaFiles.pathFromDescriptor(descriptor)
                
                try? UIImageJPEGRepresentation(self.selectedImage, 1.00)!.write(to: URL(fileURLWithPath: path), options: [.atomic])
                
                Actor.changeSelectedWallpaper("file:\(descriptor)")
            })
            
        }

    }
}
