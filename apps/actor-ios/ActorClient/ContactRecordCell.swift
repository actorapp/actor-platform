//
//  ContactRecordCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 17.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class ContactRecordCell: UITableViewCell {

    @IBOutlet weak var icon: UIImageView!
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var value: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func bind(phone: AMUserPhone) {
        icon.image = UIImage(named: "ic_profile_phone")?.tintImage(Resources.TintColor)
        
        title.text = phone.getTitle();
        title.textColor = Resources.TintColor
        
        value.text = "+\(phone.getPhone())";
    }
}
