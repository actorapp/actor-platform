//
//  MenuItemCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 18.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class MenuItemCell: UITableViewCell {

    @IBOutlet weak var titleView: UILabel!
    @IBOutlet weak var iconView: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func setData(image: String, title: String){
        iconView.image = UIImage(named: image)!.tintImage(Resources.SecondaryTint)
        titleView.text = title
    }
}
