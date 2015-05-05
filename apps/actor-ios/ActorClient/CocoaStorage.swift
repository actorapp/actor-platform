//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaStorage : AMBaseAsyncStorageProvider {
    
    let dbPath: String;
    
    init(dbPath: String) {
        self.dbPath = dbPath;
    }

    override func createPreferencesStorage() -> DKPreferencesStorage! {
        return UDPreferencesStorage();
    }
    
    override func createKeyValueWithName(name: String!) -> DKKeyValueStorage! {
        return FMDBKeyValue(databasePath: dbPath, tableName: name);
    }
    
    override func createListWithName(name: String!) -> DKListStorage! {
        return FMDBList(databasePath: dbPath, tableName: name);
    }
}