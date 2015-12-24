//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton

public class AABubbleVoiceCell: AABubbleBaseFileCell,AAModernConversationAudioPlayerDelegate {

    // Views
    
    let progress = AAProgressView(size: CGSizeMake(48, 48))
    let timeLabel = UILabel()
    let voiceTimeLabel = UILabel()
    let playPauseButton = UIButton()
    let soundProgress = UIProgressView()
    let statusView = UIImageView()
    

    private let sizeLabel = UILabel()
    private let titleLabel = UILabel()
    
    // Binded data
    
    var bindedLayout: VoiceMessageCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        
        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = appStyle.chatTextDateOutColor
        
        statusView.contentMode = UIViewContentMode.Center
        
        voiceTimeLabel.font = UIFont.italicSystemFontOfSize(11)
        voiceTimeLabel.textColor = appStyle.chatTextDateOutColor

        sizeLabel.font = UIFont.systemFontOfSize(13.0)
        sizeLabel.textColor = appStyle.chatTextOutColor
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        titleLabel.font = UIFont.systemFontOfSize(16.0)
        titleLabel.textColor = appStyle.chatTextOutColor
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(sizeLabel)
        
        
        contentView.addSubview(progress)
        
        contentView.addSubview(playPauseButton)
        contentView.addSubview(soundProgress)
        contentView.addSubview(voiceTimeLabel)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
        playPauseButton.addTarget(self, action: "mediaDidTap", forControlEvents: UIControlEvents.TouchUpInside)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
        
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    public override func bind(message: ACMessage, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! VoiceMessageCellLayout
        
        let document = message.content as! ACVoiceContent
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.MediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.MediaIn, isCompact: false)
            }
            
            titleLabel.text = "Voice message"
            sizeLabel.text = bindedLayout.fileSize
            
            // Reset progress
            self.progress.hideButton()
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.alpha = 0
            })
            
            // Bind file
            fileBind(message, autoDownload: document.getSource().getSize() < 1024 * 1025 * 1024)
            
            
           
        }
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(UInt(message.messageState.ordinal())) {
            case ACMessageState.PENDING.rawValue:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            case ACMessageState.SENT.rawValue:
                self.statusView.image = appStyle.chatIconCheck1
                self.statusView.tintColor = appStyle.chatStatusSent
                break
            case ACMessageState.RECEIVED.rawValue:
                self.statusView.image = appStyle.chatIconCheck2
                self.statusView.tintColor = appStyle.chatStatusReceived
                break
            case ACMessageState.READ.rawValue:
                self.statusView.image = appStyle.chatIconCheck2
                self.statusView.tintColor = appStyle.chatStatusRead
                break
            case ACMessageState.ERROR.rawValue:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusError
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            }
        } else {
            statusView.hidden = true
        }
    }
    
    // File state binding
    
    public override func fileUploadPaused(reference: String, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.hideView()
            
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        
        runOnUiThread(selfGeneration) { () -> () in
            self.playPauseButton.showView()
            
            self.progress.setProgress(1)
            self.progress.hideView()
        }
    }
    
    
    // Media Action
    
    public func mediaDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    
                    
                    print("paaaaath ==== \(CocoaFiles.pathFromDescriptor(reference))")
                    self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
                    self.controller.playVoiceFromPath(CocoaFiles.pathFromDescriptor(reference))
                    
                    self.controller.voicePlayer.delegate = self
                    
                    
            }))
            
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    
                    
                    print("paaaaath2 ==== \(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))")
                    self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), forState: UIControlState.Normal)
                    self.controller.playVoiceFromPath(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))
                    
                    self.controller.voicePlayer.delegate = self
                    
            }))
            
        }
    }
    
    public func audioPlayerDidFinish() {
        playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), forState: UIControlState.Normal)
    }
    
    // Layouting
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        layoutBubble(200, contentHeight: 66)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        let top = insets.top - 2
        
        // Progress state
        let progressRect = CGRectMake(contentLeft + 8, 12 + top, 48, 48)
        self.progress.frame = progressRect
        self.playPauseButton.frame = progressRect
        
        timeLabel.frame = CGRectMake(0, 0, 1000, 1000)
        timeLabel.sizeToFit()
        
        // Content
        self.titleLabel.frame = CGRectMake(contentLeft + 62, 16 + top, 200 - 64, 22)
        self.sizeLabel.frame = CGRectMake(contentLeft + 62, 16 + 22 + top, 200 - 64, 22)
    
        
        // Message state
        if (self.isOut) {
            self.timeLabel.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
            self.statusView.hidden = false
        } else {
            self.timeLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
            self.statusView.hidden = true
        }
        
    }
    
}

/**
 Voice cell layout
 */
public class VoiceMessageCellLayout: AACellLayout {
    
    public let contentSize: CGSize
    public let screenSize: CGSize
    public let autoDownload: Bool
    
    public let fileName: String
    public let fileExt: String
    public let fileSize: String
    
    /**
     Creting layout for media bubble
     */
    public init(fileName: String, fileExt: String, fileSize: Int,id: Int64, date: Int64, autoDownload: Bool) {
        
        // Saving content size
        self.contentSize = CGSizeMake(200, 66)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        // Calculating bubble screen size
        self.screenSize = CGSize(width: 200, height: 66)
        
        self.fileName = fileName
        self.fileExt = fileExt.lowercaseString
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "voice")
    }
    
    /**
     Creating layout for voice content
     */
    
    public convenience init(id: Int64, voiceContent: ACVoiceContent, date: Int64) {
        
        
        self.init(fileName: voiceContent.getName(), fileExt: voiceContent.getExt(), fileSize: Int(voiceContent.getSource().getSize()),id: id, date: date, autoDownload: true)
    }
    
    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage) {
        if let content = message.content as? ACVoiceContent {
            self.init(id: Int64(message.rid), voiceContent: content, date: Int64(message.date))
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
 Layouter for voice bubbles
 */
public class AABubbleVoiceCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACVoiceContent {
            return true
        }
        
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return VoiceMessageCellLayout(message: message)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleVoiceCell.self
    }
}
