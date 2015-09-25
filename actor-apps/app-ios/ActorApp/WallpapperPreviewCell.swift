//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class WallpapperPreviewCell: UICollectionViewCell {

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
        imageView.image = UIImage(named: "bg_\(index + 1).jpg")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        imageView.frame = contentView.bounds
    }
}