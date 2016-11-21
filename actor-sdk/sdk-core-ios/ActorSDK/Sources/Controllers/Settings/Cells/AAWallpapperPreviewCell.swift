//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAWallpapperPreviewCell: UICollectionViewCell {

    fileprivate let imageView = UIImageView()
    fileprivate let imageIcon = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        
        imageIcon.image = UIImage.bundled("ImageSelectedOn")
        
//        if self.selectedAssets.contains(photoModel) {
//            cell.isCheckSelected = true
//            cell.imgSelected.image = UIImage.bundled("ImageSelectedOn")
//            
//        } else {
//            cell.isCheckSelected = false
//            cell.imgSelected.image = UIImage.bundled("ImageSelectedOff")
//        }
        
        self.contentView.addSubview(imageView)
        self.contentView.addSubview(imageIcon)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(_ index: Int) {
        imageView.image = UIImage.bundled("bg_\(index + 1).jpg")
        imageIcon.isHidden = ActorSDK.sharedActor().messenger.getSelectedWallpaper() == "local:bg_\(index + 1).jpg"
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = contentView.bounds
        imageIcon.frame = CGRect(x: contentView.width - 32, y: contentView.height - 32, width: 26, height: 26)
    }
}
