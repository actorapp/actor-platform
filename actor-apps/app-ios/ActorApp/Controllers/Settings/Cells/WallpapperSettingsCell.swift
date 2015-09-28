//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//


import Foundation

class WallpapperSettingsCell: UATableViewCell {
    
    private let wallpapper1 = UIImageView()
    private let wallpapper2 = UIImageView()
    private let wallpapper3 = UIImageView()
    private let label = UILabel()
    private let disclose = UIImageView()
    
    var wallpapperDidTap: ((name: String) -> ())?
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(cellStyle: "cell", reuseIdentifier: reuseIdentifier)
        
        wallpapper1.image = UIImage(named: "bg_1.jpg")!
        wallpapper1.clipsToBounds = true
        wallpapper1.contentMode = .ScaleAspectFill
        wallpapper1.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_1.jpg") }
        wallpapper2.image = UIImage(named: "bg_2.jpg")!
        wallpapper2.clipsToBounds = true
        wallpapper2.contentMode = .ScaleAspectFill
        wallpapper2.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_2.jpg") }
        wallpapper3.image = UIImage(named: "bg_3.jpg")!
        wallpapper3.clipsToBounds = true
        wallpapper3.contentMode = .ScaleAspectFill
        wallpapper3.viewDidTap = { [unowned self] () -> () in self.wallpapperDidTap?(name: "bg_3.jpg") }
        label.font = UIFont.systemFontOfSize(17)
        label.applyStyle("label")
        label.text = localized("SettingsWallpapers")
        disclose.image = UIImage(named: "ios_disclose")
        
        self.contentView.addSubview(wallpapper1)
        self.contentView.addSubview(wallpapper2)
        self.contentView.addSubview(wallpapper3)
        self.contentView.addSubview(label)
        self.contentView.addSubview(disclose)
        
        disclose.hidden = true
        selectionStyle = .None
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
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