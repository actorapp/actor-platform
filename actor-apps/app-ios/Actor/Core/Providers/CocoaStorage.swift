//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaStorage : AMBaseAsyncStorageProvider {
    
    let dbPath: String;
    let preferences = UDPreferencesStorage()
    
    override init() {
        self.dbPath = NSSearchPathForDirectoriesInDomains(.DocumentDirectory,
            .UserDomainMask, true)[0].stringByAppendingPathComponent("actor.db")
    }

    override func createPreferencesStorage() -> DKPreferencesStorage! {
        return preferences
    }
    
    override func createIndexWithName(name: String!) -> DKIndexStorage! {
        return FMDBIndex(databasePath: dbPath, tableName: name)
    }
    
    override func createKeyValueWithName(name: String!) -> DKKeyValueStorage! {
        return FMDBKeyValue(databasePath: dbPath, tableName: name)
    }
    
    override func createListWithName(name: String!) -> DKListStorage! {
        return FMDBList(databasePath: dbPath, tableName: name)
    }
    
    override func getMessagesLoadGap() -> jint {
        return 30
    }
    
    override func getMessagesLoadPage() -> jint {
        return 30
    }
    
    override func resetStorage() {
        preferences.clear()
        
        var db = FMDatabase(path: dbPath)
        db.open()
        db.executeStatements("select 'drop table ' || name || ';' from sqlite_master where type = 'table';")
        db.close()
    }
}