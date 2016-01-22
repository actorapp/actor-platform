//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAWallpapperPreviewCell: UICollectionViewCell {

    private let imageView = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        imageView.contentMode = .ScaleAspectFill
        imageView.clipsToBounds = true
        
        self.contentView.addSubview(imageView)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(index: Int) {
        imageView.image = UIImage.bundled("bg_\(index + 1).jpg")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = contentView.bounds
    }
}