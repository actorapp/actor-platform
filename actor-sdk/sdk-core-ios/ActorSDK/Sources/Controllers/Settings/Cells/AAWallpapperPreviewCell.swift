//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAWallpapperPreviewCell: UICollectionViewCell {

    private let imageView = UIImageView()
    private let imageIcon = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        imageView.contentMode = .ScaleAspectFill
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
    
    func bind(index: Int) {
        imageView.image = UIImage.bundled("bg_\(index + 1).jpg")
        imageIcon.hidden = ActorSDK.sharedActor().messenger.getSelectedWallpaper() == "local:bg_\(index + 1).jpg"
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = contentView.bounds
        imageIcon.frame = CGRectMake(contentView.width - 32, contentView.height - 32, 26, 26)
    }
}