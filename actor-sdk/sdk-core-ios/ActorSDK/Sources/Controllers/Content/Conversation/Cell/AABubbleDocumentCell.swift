//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton

public class AABubbleDocumentCell: AABubbleBaseFileCell, UIDocumentInteractionControllerDelegate {
    
    private let progress = AAProgressView(size: CGSizeMake(48, 48))
    private let fileIcon = UIImageView()
    
    private let titleLabel = UILabel()
    private let sizeLabel = UILabel()
    
    private let dateLabel = UILabel()
    private let statusView = UIImageView()
    
    private var bindedLayout: DocumentCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        dateLabel.font = UIFont.italicSystemFontOfSize(11)
        dateLabel.lineBreakMode = .ByClipping
        dateLabel.numberOfLines = 1
        dateLabel.contentMode = UIViewContentMode.TopLeft
        dateLabel.textAlignment = NSTextAlignment.Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        titleLabel.font = UIFont.systemFontOfSize(16.0)
        titleLabel.textColor = appStyle.chatTextOutColor
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        
        sizeLabel.font = UIFont.systemFontOfSize(13.0)
        sizeLabel.textColor = appStyle.chatTextOutColor
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(sizeLabel)
        
        contentView.addSubview(dateLabel)
        contentView.addSubview(statusView)
        
        contentView.addSubview(fileIcon)
        contentView.addSubview(progress)
        
        self.contentInsets = UIEdgeInsetsMake(0, 0, 0, 0)
        
        self.bubble.userInteractionEnabled = true
        self.bubble.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleDocumentCell.documentDidTap)))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {

        self.bindedLayout = cellLayout as! DocumentCellLayout
        
        let document = message.content as! ACDocumentContent
        
        self.bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.MediaOut, isCompact: false)
                dateLabel.textColor = appStyle.chatTextDateOutColor
                self.statusView.hidden = false
            } else {
                bindBubbleType(.MediaIn, isCompact: false)
                dateLabel.textColor = appStyle.chatTextDateInColor
                self.statusView.hidden = true
            }
            
            titleLabel.text = bindedLayout.fileName
            sizeLabel.text = bindedLayout.fileSize
            
            // Reset progress
            self.progress.hideButton()
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.hidden = true
                self.fileIcon.hidden = true
            })
            
            // Bind file
            fileBind(message, autoDownload: document.getSource().getSize() < 1024 * 1025 * 1024)
        }
        
        // Always update date and state
        dateLabel.text = cellLayout.date
        
        // Setting message status
        if (isOut) {
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusSent
                }
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusError
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            }
        }
    }
    
    public func documentDidTap() {
        
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    let docController = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(reference)))
                    docController.delegate = self
                    
                    if (docController.presentPreviewAnimated(true)) {
                        return
                    }
                   
                    if (content.getName().hasSuffix(".ogg")) {
                        
                        print("paaaaath ==== \(CocoaFiles.pathFromDescriptor(reference))")
                        //self.controller.playVoiceFromPath(CocoaFiles.pathFromDescriptor(reference))
                        
                        return
                    }
                    
            }))
            
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    let docController = UIDocumentInteractionController(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())))
                    docController.delegate = self
                    
                    if (docController.presentPreviewAnimated(true)) {
                        return
                    }
                    
                    
                    if (content.getName().hasSuffix(".ogg")) {
                        
                        print("paaaaath2 ==== \(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))")
                        //self.controller.playVoiceFromPath(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))
                        
                        return
                    }
                    
            }))
        }
    }
    
    public override func fileUploadPaused(reference: String, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.hideProgress()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.setProgress(progress)
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.hideProgress()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            self.fileIcon.hideView()
            
            self.progress.showView()
            self.progress.setProgress(progress)
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            
            self.fileIcon.image = self.bindedLayout.icon
            self.fileIcon.showView()
            
            self.progress.hideView()
            self.progress.setProgress(1)
        }
    }
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        
        let contentWidth = self.contentView.frame.width
        let top = insets.top - 2
//        let contentHeight = self.contentView.frame.height
        
        layoutBubble(200, contentHeight: 66)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        // Content
        self.titleLabel.frame = CGRectMake(contentLeft + 62, 16 + top, 200 - 64, 22)
        self.sizeLabel.frame = CGRectMake(contentLeft + 62, 16 + 22 + top, 200 - 64, 22)
        
        // Progress state
        let progressRect = CGRectMake(contentLeft + 8, 12 + top, 48, 48)
        self.progress.frame = progressRect
        self.fileIcon.frame = CGRectMake(contentLeft + 16, 20 + top, 32, 32)
        
        // Message state
        if (self.isOut) {
            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
            self.statusView.hidden = false
        } else {
            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.hidden = true
        }
    }

    public func documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController) -> UIViewController {
        return self.controller
    }
}

public class AABubbleDocumentCellLayout: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        
        return message.content is ACDocumentContent
        
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return DocumentCellLayout(message: message, layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleDocumentCell.self
    }
}

public class DocumentCellLayout: AACellLayout {
    
    public let fileName: String
    public let fileExt: String
    public let fileSize: String
    
    public let icon: UIImage
    public let fastThumb: NSData?
    
    public let autoDownload: Bool
    
    public init(fileName: String, fileExt: String, fileSize: Int, fastThumb: ACFastThumb?, date: Int64, autoDownload: Bool, layouter: AABubbleLayouter) {
        
        // File metadata
        self.fileName = fileName
        self.fileExt = fileExt.lowercaseString
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Auto download flag
        self.autoDownload = autoDownload
        
        // Fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // File icon
        var fileName = "file_unknown"
        if (AAFileTypes[self.fileExt] != nil) {
            switch(AAFileTypes[self.fileExt]!) {
            case AAFileType.Music:
                fileName = "file_music"
                break
            case AAFileType.Doc:
                fileName = "file_doc"
                break
            case AAFileType.Spreadsheet:
                fileName = "file_xls"
                break
            case AAFileType.Video:
                fileName = "file_video"
                break
            case AAFileType.Presentation:
                fileName = "file_ppt"
                break
            case AAFileType.PDF:
                fileName = "file_pdf"
                break
            case AAFileType.APK:
                fileName = "file_apk"
                break
            case AAFileType.RAR:
                fileName = "file_rar"
                break
            case AAFileType.ZIP:
                fileName = "file_zip"
                break
            case AAFileType.CSV:
                fileName = "file_csv"
                break
            case AAFileType.HTML:
                fileName = "file_html"
                break
            default:
                fileName = "file_unknown"
                break
            }
        }
        self.icon = UIImage.bundled(fileName)!
        
        super.init(height: 66, date: date, key: "document", layouter: layouter)
    }
    
    public convenience init(document: ACDocumentContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(fileName: document.getName(), fileExt: document.getExt(), fileSize: Int(document.getSource().getSize()), fastThumb: document.getFastThumb(), date: date, autoDownload: false, layouter: layouter)
    }
    
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        self.init(document: message.content as! ACDocumentContent, date: Int64(message.date), layouter: layouter)
    }
}