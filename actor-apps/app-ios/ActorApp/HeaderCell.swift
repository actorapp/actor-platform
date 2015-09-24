//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class HeaderCell: UATableViewCell {
    
    var titleView = UILabel()
    var iconView = UIImageView()
    
    init(reuseIdentifier: String?) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = MainAppTheme.list.backyardColor
        selectionStyle = UITableViewCellSelectionStyle.None
        
        titleView.textColor = MainAppTheme.list.sectionColor
        titleView.font = UIFont.systemFontOfSize(13)
        contentView.addSubview(titleView)
        
        iconView.contentMode = UIViewContentMode.ScaleAspectFill
        
        let tapRecognizer = UITapGestureRecognizer(target: self, action: "iconDidTap")
        iconView.addGestureRecognizer(tapRecognizer)
        iconView.userInteractionEnabled = true
        
        contentView.addSubview(iconView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let height = self.contentView.bounds.height
        let width = self.contentView.bounds.width
        
        titleView.frame = CGRectMake(15, height - 24, width - 48, 24)
        iconView.frame = CGRectMake(width - 18 - 15, height - 18 - 4, 18, 18)
    }
    
    func iconDidTap() {
        UIAlertView(title: nil, message: "Tap", delegate: nil, cancelButtonTitle: nil).show()
    }
}

class UAHeaderRegion: UARegion {
    
    private var height: Double = 40.0
    private var title: String?
    private var icon: UIImage?
    
    func setHeight(height: Double) -> UAHeaderRegion {
        self.height = height
        return self
    }
    
    func setTitle(title: String?) -> UAHeaderRegion {
        self.title = title
        return self
    }
    
    func setIcon(icon: UIImage?) -> UAHeaderRegion {
        self.icon = icon
        return self
    }
    
    override func itemsCount() -> Int {
        return 1
    }
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return CGFloat(height)
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        let res = HeaderCell(reuseIdentifier: "_HeaderCell")
        
        if title == nil {
            res.titleView.hidden = true
        } else {
            res.titleView.hidden = false
            res.titleView.text = title
        }
        
        if icon == nil {
            res.iconView.hidden = true
        } else {
            res.iconView.hidden = false
            res.iconView.image = icon!.tintImage(MainAppTheme.list.sectionColor)
        }
        
        return res
    }
    
    override func canSelect(index: Int) -> Bool {
        return false
    }    
}

extension UASection {
    func addHeaderCell() -> UAHeaderRegion {
        let res = UAHeaderRegion(section: self)
        regions.append(res)
        self.autoSeparatorTopOffset = 1
        return res
    }
}