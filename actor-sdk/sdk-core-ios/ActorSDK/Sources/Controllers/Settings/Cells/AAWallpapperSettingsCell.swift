//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAWallpapperSettingsCell: AATableViewCell {
    
    private let wallpapper1 = UIImageView()
    private let wallpapper2 = UIImageView()
    private let wallpapper3 = UIImageView()
    private let label = UILabel()
    private let disclose = UIImageView()
    
    public var wallpapperDidTap: ((name: String) -> ())?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        wallpapper1.clipsToBounds = true
        wallpapper1.contentMode = .ScaleAspectFill
        wallpapper1.image = UIImage.bundled("bg_1_preview.jpg")!
        wallpapper1.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_1.jpg") }

        wallpapper2.clipsToBounds = true
        wallpapper2.contentMode = .ScaleAspectFill
        wallpapper2.image = UIImage.bundled("bg_2_preview.jpg")!
        wallpapper2.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_2.jpg") }

        wallpapper3.clipsToBounds = true
        wallpapper3.contentMode = .ScaleAspectFill
        wallpapper3.image = UIImage.bundled("bg_3_preview.jpg")!
        wallpapper3.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_3.jpg") }

        label.font = UIFont.systemFontOfSize(17)
        label.textColor = appStyle.cellTextColor
        label.text = AALocalized("SettingsWallpapers")
        disclose.image = UIImage.bundled("ios_disclose")
        
        self.contentView.addSubview(wallpapper1)
        self.contentView.addSubview(wallpapper2)
        self.contentView.addSubview(wallpapper3)
        self.contentView.addSubview(label)
        self.contentView.addSubview(disclose)
        
        disclose.hidden = false
        //selectionStyle = .None
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = contentView.width
        let height = contentView.height
        let padding: CGFloat = 15
        let wPadding: CGFloat = 44
        let wWidth = (width - padding * 4) / 3
        let wHeight = height - padding - 44
        
        wallpapper1.frame = CGRectMake(padding, wPadding, wWidth, wHeight)
        wallpapper2.frame = CGRectMake(padding * 2 + wWidth, wPadding, wWidth, wHeight)
        wallpapper3.frame = CGRectMake(padding * 3 + wWidth * 2, wPadding, wWidth, wHeight)
        
        label.frame = CGRectMake(padding, 0, width - padding * 2, 44)
        
        disclose.frame = CGRectMake(width - 13 - 10, 15, 13, 14)
    }
}