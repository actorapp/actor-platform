//
//  AvatarCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 17.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AvatarCell: UITableViewCell {

    @IBOutlet weak var avatarView: AvatarView!
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var subTitle: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    func bind(user: AMUserVM) {
        avatarView.bind(user.getName().get() as! String, id: user.getId(), avatar: user.getAvatar().get() as? AMAvatar)
        title.text = user.getName().get() as! String
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
