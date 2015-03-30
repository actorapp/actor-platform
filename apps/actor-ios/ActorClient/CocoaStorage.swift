//
//  CocoaStorage.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 14.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

@objc class CocoaStorage : AMBaseStorageProvider {
    
    let dbPath: String;
    
    init(dbPath: String) {
        self.dbPath = dbPath;
    }

    override func createPreferencesStorage() -> DKPreferencesStorage! {
        return UDPreferencesStorage();
    }
    
    override func createKeyValue(name: String!) -> DKKeyValueStorage! {
        return FMDBKeyValue(databasePath: dbPath, tableName: name);
    }
    
    override func createList(name: String!) -> DKListStorage! {
        return FMDBList(databasePath: dbPath, tableName: name);
    }
}