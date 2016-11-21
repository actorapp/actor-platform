//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYImage

open class AAWallpapperSettingsCell: AATableViewCell {
    
    fileprivate let wallpapper1 = UIImageView()
    fileprivate let wallpapper1Icon = UIImageView()
    fileprivate let wallpapper2 = UIImageView()
    fileprivate let wallpapper2Icon = UIImageView()
    fileprivate let wallpapper3 = UIImageView()
    fileprivate let wallpapper3Icon = UIImageView()
    fileprivate let label = UILabel()
    fileprivate let disclose = UIImageView()
    
    open var wallpapperDidTap: ((_ name: String) -> ())?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        wallpapper1.clipsToBounds = true
        wallpapper1.contentMode = .scaleAspectFill
        wallpapper1.image = UIImage.bundled("bg_1_preview.jpg")!
        wallpapper1.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_1.jpg") }
        wallpapper1Icon.image = UIImage.bundled("ImageSelectedOn")

        wallpapper2.clipsToBounds = true
        wallpapper2.contentMode = .scaleAspectFill
        wallpapper2.image = UIImage.bundled("bg_2_preview.jpg")!
        wallpapper2.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_2.jpg") }
        wallpapper2Icon.image = UIImage.bundled("ImageSelectedOn")

        wallpapper3.clipsToBounds = true
        wallpapper3.contentMode = .scaleAspectFill
        wallpapper3.image = UIImage.bundled("bg_3_preview.jpg")!
        wallpapper3.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?("bg_3.jpg") }
        wallpapper3Icon.image = UIImage.bundled("ImageSelectedOn")

        label.font = UIFont.systemFont(ofSize: 17)
        label.textColor = appStyle.cellTextColor
        label.text = AALocalized("SettingsWallpapers")
        disclose.image = UIImage.bundled("ios_disclose")
        
        self.contentView.addSubview(wallpapper1)
        self.wallpapper1.addSubview(wallpapper1Icon)
        self.contentView.addSubview(wallpapper2)
        self.wallpapper2.addSubview(wallpapper2Icon)
        self.contentView.addSubview(wallpapper3)
        self.wallpapper3.addSubview(wallpapper3Icon)
        self.contentView.addSubview(label)
        self.contentView.addSubview(disclose)
        
        disclose.isHidden = false
        //selectionStyle = .None
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = contentView.width
        let height = contentView.height
        let padding: CGFloat = 15
        let wPadding: CGFloat = 44
        let wWidth = (width - padding * 4) / 3
        let wHeight = height - padding - 44
        
        wallpapper1.frame = CGRect(x: padding, y: wPadding, width: wWidth, height: wHeight)
        wallpapper2.frame = CGRect(x: padding * 2 + wWidth, y: wPadding, width: wWidth, height: wHeight)
        wallpapper3.frame = CGRect(x: padding * 3 + wWidth * 2, y: wPadding, width: wWidth, height: wHeight)
        
        wallpapper1Icon.frame = CGRect(x: wWidth - 32, y: wHeight - 32, width: 26, height: 26)
        wallpapper2Icon.frame = CGRect(x: wWidth - 32, y: wHeight - 32, width: 26, height: 26)
        wallpapper3Icon.frame = CGRect(x: wWidth - 32, y: wHeight - 32, width: 26, height: 26)
        
        label.frame = CGRect(x: padding, y: 0, width: width - padding * 2, height: 44)
        
        disclose.frame = CGRect(x: width - 13 - 10, y: 15, width: 13, height: 14)
    }
    
    open func bind() {
        wallpapper1Icon.isHidden = ActorSDK.sharedActor().messenger.getSelectedWallpaper() != "local:bg_1.jpg"
        wallpapper2Icon.isHidden = ActorSDK.sharedActor().messenger.getSelectedWallpaper() != "local:bg_2.jpg"
        wallpapper3Icon.isHidden = ActorSDK.sharedActor().messenger.getSelectedWallpaper() != "local:bg_3.jpg"
    }
}
