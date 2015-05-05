//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//
import UIKit

class AADialogsListSearchCell: AATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private let avatarView: AAAvatarView = AAAvatarView(frameSize: 48, type: AAAvatarType.Rounded);
    private let titleView: UILabel = UILabel();
    private let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor);
    
    private var bindedFile: jlong? = nil;
    private var avatarCallback: CocoaDownloadCallback? = nil;
    
    // MARK: -
    // MARK: Constructors
    
    init(reuseIdentifier:String) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont(name: "Roboto-Medium", size: 19);
        titleView.textColor = Resources.TextPrimaryColor;
        
        contentView.addSubview(avatarView)
        contentView.addSubview(titleView)
        contentView.addSubview(separatorView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    func bindSearchEntity(searchEntity: AMSearchEntity, isLast: Bool) {
        avatarView.bind(searchEntity.getTitle(), id: searchEntity.getPeer().getPeerId(), avatar: searchEntity.getAvatar());
        titleView.text = searchEntity.getTitle();
        self.separatorView.hidden = isLast;
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews();
        
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48);
        titleView.frame = CGRectMake(leftPadding, 0, width - leftPadding - (padding + 50), contentView.bounds.size.height);
        separatorView.frame = CGRectMake(leftPadding, 75.5, width, 0.5);
    }

}
