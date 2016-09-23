//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation


open class AABackgroundCellRenderer<P, T> where T: AnyObject, P: AnyObject, P: Equatable {
    
    fileprivate var generation = 0
    fileprivate var wasPresented: Bool = false
    fileprivate var requestedConfig: P? = nil
    fileprivate let renderer: (_ config: P)-> T!
    fileprivate let receiver: (T!) -> ()
    
    public init(renderer: @escaping (_ config: P) -> T!, receiver: @escaping (T!) -> ()) {
        self.renderer = renderer
        self.receiver = receiver
    }

    func requestRender(_ config: P) -> Bool {
        // Ignore if not resized
        if requestedConfig == config {
            return false
        }
        let oldConfig = requestedConfig
        let wasConfig = oldConfig != nil
        
        // Releasing in background
        if wasConfig {
            dispatchBackground {
                let _ = oldConfig
            }
        }
        
        if wasPresented {
            // Do Sync rendering when is just resized
            render(config)
        } else {
            requestedConfig = config
            generation += 1
            let curGen = generation
            dispatchBackground {
                if curGen != self.generation {
                    return
                }
                let res = self.renderer(config)
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
    
    func render(_ config: P) {
        // Ignore if not resized
        if requestedConfig == config {
            return
        }
        
        requestedConfig = config
        generation += 1
        wasPresented = true
        receiver(renderer(config))
    }
    
    func cancelRender(_ wasPresented: Bool = false) {
        generation += 1
        let oldConfig = requestedConfig
        requestedConfig = nil
        
        // Releasing in background
        if oldConfig != nil {
            dispatchBackground {
                let _ = oldConfig
            }
        }
        self.wasPresented = wasPresented
    }
}
