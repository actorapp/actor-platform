//
//  FMDBKeyValue.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 14.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

@objc class FMDBKeyValue: NSObject, DKKeyValueStorage {
    var db :FMDatabase?;
    
    let databasePath: String;
    let tableName: String;
    
    let queryCreate: String;
    let queryItem: String;
    let queryAdd: String;
    let queryDelete: String;
    let queryDeleteAll: String;
    
    var isTableChecked: Bool = false;
    
    init(databasePath: String, tableName: String) {
        self.databasePath = databasePath
        self.tableName = tableName
        
        // Queries
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
            "\"ID\" INTEGER NOT NULL, " +
            "\"BYTES\" BLOB NOT NULL, " +
            "PRIMARY KEY (\"ID\"));";
        self.queryItem = "SELECT \"BYTES\" FROM " + tableName + " WHERE \"ID\" = ?;";
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\", \"BYTES\") VALUES (?, ?);";
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\" = ?;";
        self.queryDeleteAll = "DELETE FROM " + tableName + ";";
        
        super.init()
    }
    
    private func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true;
        
        self.db = FMDatabase(path: databasePath)
        self.db!.open()
        if (!db!.tableExists(tableName)) {
            db!.executeUpdate(queryCreate)
        }
    }
    
    func addOrUpdateItemsWithJavaUtilList(values: JavaUtilList!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<values.size() {
            let record = values.getWithInt(i) as! DKKeyValueRecord;
            db!.executeUpdate(queryAdd, record.getId().toNSNumber(),record.getData()!.toNSData())
        }
        db!.commit()
    }
    
    func addOrUpdateItemWithLong(id_: jlong, withByteArray data: IOSByteArray!) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryAdd, id_.toNSNumber(), data!.toNSData())
        db!.commit()
    }
 
    func removeItemsWithLongArray(ids: IOSLongArray!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<ids.length() {
            var id_ = ids.longAtIndex(UInt(i));
            db!.executeUpdate(queryDelete, id_.toNSNumber())
        }
        db!.commit()
    }
    
    func removeItemWithLong(id_: jlong) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDelete, id_.toNSNumber())
        db!.commit()
    }
    
    func clear() {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDeleteAll);
        db!.commit()
    }
    
    func getValueWithLong(id_: jlong) -> IOSByteArray! {
        checkTable();
        
        var result = db!.dataForQuery(queryItem, id_.toNSNumber());
        if (result == nil) {
            return nil;
        }
        return result.toJavaBytes();
    }
}