//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class CocoaStorageRuntime : NSObject, ARStorageRuntime {
    
    let dbQueue: FMDatabaseQueue
    let preferences = UDPreferencesStorage()
    
    override init() {
        dbQueue = FMDatabaseQueue(path: NSSearchPathForDirectoriesInDomains(.DocumentDirectory,
            .UserDomainMask, true)[0].asNS.stringByAppendingPathComponent("actor.db"))
    }
    
    func createPreferencesStorage() -> ARPreferencesStorage! {
        return preferences
    }
    
    func createKeyValueWithName(name: String!) -> ARKeyValueStorage! {
        return FMDBKeyValue(dbQueue: dbQueue, tableName: name)
    }
    
    func createListWithName(name: String!) -> ARListStorage! {
        return FMDBList(dbQueue: dbQueue, tableName: name)
    }
    
    func resetStorage() {
        preferences.clear()
        dbQueue.inDatabase { (db) in
            db.executeStatements("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
        }
    }
}