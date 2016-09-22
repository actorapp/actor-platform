//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AAWallpapersCell: AATableViewCell {
    
    fileprivate let wallpapper1 = UIImageView()
    fileprivate let wallpapper2 = UIImageView()
    fileprivate let wallpapper3 = UIImageView()
    
    open var wallpapperDidTap: ((_ name: String) -> ())?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        wallpapper1.clipsToBounds = true
        wallpapper1.contentMode = .scaleAspectFill
        wallpapper1.image = UIImage.bundled("bg_1_preview.jpg")!
        wallpapper1.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_1.jpg") }
        
        wallpapper2.clipsToBounds = true
        wallpapper2.contentMode = .scaleAspectFill
        wallpapper2.image = UIImage.bundled("bg_2_preview.jpg")!
        wallpapper2.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_2.jpg") }
        
        wallpapper3.clipsToBounds = true
        wallpapper3.contentMode = .scaleAspectFill
        wallpapper3.image = UIImage.bundled("bg_3_preview.jpg")!
        wallpapper3.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_3.jpg") }
        
        self.contentView.addSubview(wallpapper1)
        self.contentView.addSubview(wallpapper2)
        self.contentView.addSubview(wallpapper3)
        
        selectionStyle = .none
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = contentView.width
        let height = contentView.height
        let padding: CGFloat = 15
        let wPadding: CGFloat = 15
        let wWidth = (width - padding * 4) / 3
        let wHeight = height - padding - 15
        
        wallpapper1.frame = CGRect(x: padding, y: wPadding, width: wWidth, height: wHeight)
        wallpapper2.frame = CGRect(x: padding * 2 + wWidth, y: wPadding, width: wWidth, height: wHeight)
        wallpapper3.frame = CGRect(x: padding * 3 + wWidth * 2, y: wPadding, width: wWidth, height: wHeight)
        
    }
    

}
