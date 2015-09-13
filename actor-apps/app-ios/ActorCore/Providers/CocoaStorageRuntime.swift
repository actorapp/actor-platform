//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

@objc class CocoaStorageRuntime : NSObject, ARStorageRuntime {
    
    let dbPath: String;
    let preferences = UDPreferencesStorage()
    
    override init() {
        self.dbPath = NSSearchPathForDirectoriesInDomains(.DocumentDirectory,
            .UserDomainMask, true)[0].asNS.stringByAppendingPathComponent("actor.db")
    }
    
    func createPreferencesStorage() -> ARPreferencesStorage! {
        return preferences
    }
    
    func createIndexWithName(name: String!) -> ARIndexStorage! {
        return FMDBIndex(databasePath: dbPath, tableName: name)
    }

    func createKeyValueWithName(name: String!) -> ARKeyValueStorage! {
        return FMDBKeyValue(databasePath: dbPath, tableName: name)
    }
    
    func createListWithName(name: String!) -> ARListStorage! {
        return FMDBList(databasePath: dbPath, tableName: name)
    }
    
    func resetStorage() {
        preferences.clear()
        
        let db = FMDatabase(path: dbPath)
        db.open()
        db.executeStatements("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
        db.close()
    }
}