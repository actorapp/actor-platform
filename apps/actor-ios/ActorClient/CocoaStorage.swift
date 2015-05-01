//
//  CocoaStorage.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 14.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

@objc class CocoaStorage : AMBaseAsyncStorageProvider {
    
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