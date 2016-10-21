//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import Photos

class AAThumbnailCollectionCell: UICollectionViewCell {
    let imgThumbnails       : UIImageView!
    let imgSelected         : UIImageView!
    
    var animated            : Bool!
    var indexPath           : IndexPath!
    var isCheckSelected     : Bool!
    weak var bindedThumbView : AAThumbnailView!
    weak var bindedPhotoModel : PHAsset!
    
    override init(frame: CGRect) {
        
        self.imgThumbnails = UIImageView()
        self.imgThumbnails.backgroundColor = UIColor.clear
        
        self.imgSelected = UIImageView()
        self.imgSelected.backgroundColor = UIColor.clear
        self.imgSelected.isUserInteractionEnabled = true
        self.imgSelected.contentMode = UIViewContentMode.scaleAspectFill
        
        self.isCheckSelected = false
        self.animated        = false
        
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
            
            self.imgSelected.transform = CGAffineTransform(scaleX: 0.5,y: 0.5)
            UIView.animate(withDuration: 0.3, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 0.5, options: UIViewAnimationOptions(), animations: { () -> Void in
                
                self.imgSelected.transform = CGAffineTransform.identity
                
                }, completion: nil)
            
            self.isCheckSelected = true
            print(animated)
            self.bindedThumbView.addSelectedModel(self.bindedPhotoModel,animated:self.animated)
            
        } else {
            
            self.imgSelected.image = UIImage.bundled("ImageSelectedOff")
            
            self.imgSelected.transform = CGAffineTransform(scaleX: 0.5,y: 0.5)
            UIView.animate(withDuration: 0.3, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 0.5, options: UIViewAnimationOptions(), animations: { () -> Void in
                
                self.imgSelected.transform = CGAffineTransform.identity
                
                }, completion: nil)
            
            self.isCheckSelected = false
            
            self.bindedThumbView.removeSelectedModel(self.bindedPhotoModel,animated:self.animated)
            
        }
        
    }
    
    func configUI() {
        
        self.addSubview(self.imgThumbnails)
        self.addSubview(self.imgSelected)
        
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.imgThumbnails.frame = CGRect(x: 0, y: 0, width: 90, height: 90)
        
        self.imgSelected.frame = CGRect(x: self.contentView.frame.size.width-30, y: 4, width: 26, height: 26)
        
        
        
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
