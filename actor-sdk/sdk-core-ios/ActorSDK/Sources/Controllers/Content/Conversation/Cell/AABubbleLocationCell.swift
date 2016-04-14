//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

private let mapWidth: CGFloat = 200
private let mapHeight: CGFloat = 160

public class AABubbleLocationCell: AABubbleCell {
    
    private let map = AAMapFastView(mapWidth: mapWidth, mapHeight: mapHeight)
    
    private let pin = UIImageView()
    private let timeBg = UIImageView()
    private let timeLabel = UILabel()
    private let statusView = UIImageView()
    
    private var bindedLat: Double? = nil
    private var bindedLon: Double? = nil
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = ActorSDK.sharedActor().style.statusBackgroundImage
        
        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.Center

        pin.image = UIImage.bundled("LocationPin")
        
        contentView.addSubview(map)
        map.addSubview(pin)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)

        map.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleLocationCell.mapDidTap)))
        map.userInteractionEnabled = true
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func mapDidTap() {
        let url = "http://maps.apple.com/?q=\(bindedLat!),\(bindedLon!)"
        // print("url: \(url)")
        UIApplication.sharedApplication().openURL(NSURL(string: url)!)
    }
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        let layout = cellLayout as! AALocationCellLayout
        
        bindedLat = layout.latitude
        bindedLon = layout.longitude
        
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
        }
        
        map.bind(layout.latitude, longitude: layout.longitude)
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusMediaSent
                }
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusMediaError
                break
            case .PENDING:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            }
        } else {
            statusView.hidden = true
        }
    }
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        layoutBubble(mapWidth, contentHeight: mapHeight)
        
        if isOut {
            map.frame = CGRectMake(contentWidth - insets.right - mapWidth , insets.top, mapWidth, mapHeight)
        } else {
            map.frame = CGRectMake(insets.left, insets.top, mapWidth, mapHeight)
        }
        
        timeLabel.frame = CGRectMake(0, 0, 1000, 1000)
        timeLabel.sizeToFit()
        
        let timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        let timeHeight: CGFloat = 20
        
        timeLabel.frame = CGRectMake(map.frame.maxX - timeWidth - 18, map.frame.maxY - timeHeight - 6, timeLabel.frame.width, timeHeight)
        
        if (isOut) {
            statusView.frame = CGRectMake(timeLabel.frame.maxX, timeLabel.frame.minY, 23, timeHeight)
        }
        
        pin.frame = CGRectMake((map.width - pin.image!.size.width)/2, (map.height / 2 - pin.image!.size.height),
            pin.image!.size.width, pin.image!.size.height)
        
        timeBg.frame = CGRectMake(timeLabel.frame.minX - 4, timeLabel.frame.minY - 1, timeWidth + 8, timeHeight + 2)
        
    }
}

public class AALocationCellLayout: AACellLayout {
    
    let latitude: Double
    let longitude: Double
    
    init(latitude: Double, longitude: Double, date: Int64, layouter: AABubbleLayouter) {
        self.latitude = latitude
        self.longitude = longitude
        super.init(height: mapHeight + 2, date: date, key: "location", layouter: layouter)
    }
}

public class AABubbleLocationCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if (message.content is ACLocationContent) {
            return true
        }
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        let content = message.content as! ACLocationContent
        return AALocationCellLayout(latitude: Double(content.getLatitude()), longitude: Double(content.getLongitude()), date: Int64(message.date), layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleLocationCell.self
    }
}