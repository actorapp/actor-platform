//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import SZTextView

public class AAGroupEditInfoController: AAViewController, UITextViewDelegate {
    
    private var isChannel = false
    private let scrollView = UIScrollView()
    private let bgContainer = UIView()
    private let topSeparator = UIView()
    private let bottomSeparator = UIView()
    
    private let avatarView = AAAvatarView()
    private let nameInput = UITextField()
    private let nameInputSeparator = UIView()
    private let descriptionView = SZTextView()
    private let descriptionSeparator = UIView()
    
    public init(gid: Int) {
        super.init()
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.CHANNEL()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = appStyle.vcBackyardColor
        
        scrollView.alwaysBounceVertical = true
        
        nameInputSeparator.backgroundColor = appStyle.vcSeparatorColor
        topSeparator.backgroundColor = appStyle.vcSeparatorColor
        bottomSeparator.backgroundColor = appStyle.vcSeparatorColor
        descriptionSeparator.backgroundColor = appStyle.vcSeparatorColor
        
        bgContainer.backgroundColor = appStyle.vcBgColor
        
        scrollView.addSubview(bgContainer)
        bgContainer.addSubview(avatarView)
        bgContainer.addSubview(nameInput)
        bgContainer.addSubview(nameInputSeparator)
        bgContainer.addSubview(descriptionView)
        bgContainer.addSubview(descriptionSeparator)
        bgContainer.addSubview(topSeparator)
        bgContainer.addSubview(bottomSeparator)
        view.addSubview(scrollView)
        
        avatarView.bind(group.name.get(), id: gid, avatar: group.avatar.get())
        avatarView.viewDidTap = {
            self.avatarDidTap()
        }
        
        nameInput.font = UIFont.systemFontOfSize(19)
        if isChannel {
            nameInput.placeholder = AALocalized("GroupEditNameChannel")
        } else {
            nameInput.placeholder = AALocalized("GroupEditName")
        }
        nameInput.text = group.name.get()
        
        descriptionView.delegate = self
        descriptionView.font = UIFont.systemFontOfSize(17)
        descriptionView.placeholder = AALocalized("GroupEditDescription")
        descriptionView.text = group.about.get()
        descriptionView.scrollEnabled = false
        
        if isChannel {
            navigationItem.title = AALocalized("GroupEditTitleChannel")
        } else {
            navigationItem.title = AALocalized("GroupEditTitle")
        }
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationSave"), style: .Done, target: self, action: #selector(saveDidPressed))
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Done, target: self, action: #selector(cancelEdit))
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        scrollView.frame = CGRectMake(0, 0, view.width, view.height)
        
        nameInput.frame = CGRectMake(94, 34, view.width - 94 - 10, 24)
        nameInputSeparator.frame = CGRectMake(94, 34 + 24, view.width - 94, 0.5)
        avatarView.frame = CGRectMake(14, 14, 66, 66)
        
        descriptionSeparator.frame = CGRectMake(0, 94, view.width, 0.5)
        topSeparator.frame = CGRectMake(0, 0, view.width, 0.5)
        
        layoutContainer()
    }
    
    public func avatarDidTap() {
        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)
        self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                              cancelButton: "AlertCancel",
                              destructButton: self.group.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                              sourceView: self.view,
                              sourceRect: self.view.bounds,
                              tapClosure: { (index) -> () in
                                if (index == -2) {
                                    self.confirmAlertUser("PhotoRemoveGroupMessage",
                                        action: "PhotoRemove",
                                        tapYes: { () -> () in
                                            Actor.removeGroupAvatarWithGid(jint(self.gid))
                                            self.avatarView.bind(self.group.name.get(), id: self.gid, avatar: nil)
                                        }, tapNo: nil)
                                } else if (index >= 0) {
                                    let takePhoto: Bool = (index == 0) && hasCamera
                                    self.pickAvatar(takePhoto, closure: { (image) -> () in
                                        let fileName = Actor.changeGroupAvatar(jint(self.gid), image: image)
                                        self.avatarView.bind(self.group.name.get(), id: self.gid, fileName: CocoaFiles.pathFromDescriptor(fileName))
                                    })
                                }
        })
    }
    
    public func saveDidPressed() {
        let text = nameInput.text!.trim()
        let about = self.descriptionView.text!.trim()
        nameInput.resignFirstResponder()
        descriptionView.resignFirstResponder()
        if text != group.name.get() {
            executePromise(Actor.editGroupTitleWithGid(jint(gid), withTitle: text).then({ (v: ARVoid!) in
                if about != self.group.about.get() {
                    self.executePromise(Actor.editGroupAboutWithGid(jint(self.gid), withAbout: about).then({ (v: ARVoid!) in
                        self.cancelEdit()
                    }))
                } else {
                    self.cancelEdit()
                }
            }))
        } else {
            if about != self.group.about.get() {
                self.executePromise(Actor.editGroupAboutWithGid(jint(self.gid), withAbout: about).then({ (v: ARVoid!) in
                    self.cancelEdit()
                }))
            } else {
                self.cancelEdit()
            }
        }
    }
    
    func cancelEdit() {
        nameInput.resignFirstResponder()
        descriptionView.resignFirstResponder()
        dismiss()
    }
    
    public func textViewDidChange(textView: UITextView) {
        layoutContainer()
    }
    
    private func layoutContainer() {
        let newSize = descriptionView.sizeThatFits(CGSize(width: view.width - 20, height: CGFloat.max))
        descriptionView.frame = CGRectMake(10, 102, view.width - 20, max(newSize.height, 33))
        bgContainer.frame = CGRectMake(0, 0, view.width, 100 + descriptionView.height + 8)
        bottomSeparator.frame = CGRectMake(0, bgContainer.height - 0.5, bgContainer.width, 0.5)
    }
}