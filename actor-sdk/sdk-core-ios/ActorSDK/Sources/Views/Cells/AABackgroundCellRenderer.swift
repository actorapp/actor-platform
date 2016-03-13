//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation


public class AABackgroundCellRenderer<P, T where T: AnyObject, P: AnyObject, P: Equatable> {
    
    private var generation = 0
    private var wasPresented: Bool = false
    private var requestedConfig: P? = nil
    private let renderer: (config: P)-> T!
    private let receiver: (T!) -> ()
    
    public init(renderer: (config: P) -> T!, receiver: (T!) -> ()) {
        self.renderer = renderer
        self.receiver = receiver
    }

    func requestRender(config: P) -> Bool {
        // Ignore if not resized
        if requestedConfig == config {
            return false
        }
        let wasConfig = requestedConfig != nil
        
        if wasPresented {
            // Do Sync rendering when is just resized
            render(config)
        } else {
            requestedConfig = config
            generation++
            let curGen = generation
            dispatchBackground {
                if curGen != self.generation {
                    return
                }
                let res = self.renderer(config: config)
                if curGen != self.generation {
                    return
                }
                dispatchOnUi {
                    if curGen == self.generation {
                        self.wasPresented = true
                        self.receiver(res)
                    }
                }
            }
        }
        
        return wasConfig
    }
    
    func render(config: P) {
        // Ignore if not resized
        if requestedConfig == config {
            return
        }
        
        requestedConfig = config
        generation++
        wasPresented = true
        receiver(renderer(config: config))
    }
    
    func cancelRender(wasPresented: Bool = false) {
        generation++
        requestedConfig = nil
        self.wasPresented = wasPresented
    }
}