//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

class AAThumbnailCollectionCell: UICollectionViewCell {
    let imgThumbnails       : UIImageView!
    let imgSelected         : UIImageView!
    
    var indexPath           : NSIndexPath!
    var isCheckSelected     : Bool!
    weak var bindedThumbView : AAThumbnailView!
    weak var bindedPhotoModel : PHAsset!
    
    override init(frame: CGRect) {
        
        self.imgThumbnails = UIImageView()
        self.imgThumbnails.backgroundColor = UIColor.clearColor()
        
        self.imgSelected = UIImageView()
        self.imgSelected.backgroundColor = UIColor.clearColor()
        self.imgSelected.userInteractionEnabled = true
        self.imgSelected.contentMode = UIViewContentMode.ScaleAspectFill
        
        self.isCheckSelected = false
        
        super.init(frame: frame)
        
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(AAThumbnailCollectionCell.handleSingleTap))
        self.imgSelected.addGestureRecognizer(tapRecognizer)
        
        self.configUI()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    ///
    
    func handleSingleTap() {
        
        if (self.isCheckSelected == false) {
            
            self.imgSelected.image = UIImage.bundled("ImageSelectedOn")
            
            self.imgSelected.transform = CGAffineTransformMakeScale(0.5,0.5)
            UIView.animateWithDuration(0.3, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 0.5, options: UIViewAnimationOptions.CurveEaseInOut, animations: { () -> Void in
                
                self.imgSelected.transform = CGAffineTransformIdentity
                
                }, completion: nil)
            
            self.isCheckSelected = true
            
            self.bindedThumbView.addSelectedModel(self.bindedPhotoModel)
            
        } else {
            
            self.imgSelected.image = UIImage.bundled("ImageSelectedOff")
            
            self.imgSelected.transform = CGAffineTransformMakeScale(0.5,0.5)
            UIView.animateWithDuration(0.3, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 0.5, options: UIViewAnimationOptions.CurveEaseInOut, animations: { () -> Void in
                
                self.imgSelected.transform = CGAffineTransformIdentity
                
                }, completion: nil)
            
            self.isCheckSelected = false
            
            self.bindedThumbView.removeSelectedModel(self.bindedPhotoModel)
            
        }
        
    }
    
    func configUI() {
        
        self.addSubview(self.imgThumbnails)
        self.addSubview(self.imgSelected)
        
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.imgThumbnails.frame = CGRectMake(0, 0, 90, 90)
        
        self.imgSelected.frame = CGRectMake(self.contentView.frame.size.width-30, 4, 26, 26)
        
        
        
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        
        self.imgThumbnails.image = nil
        
    }
    
    //
    
    func cellReuseIdentifier() -> String {
        return "VMThumbnailCollectionCell"
    }
}
