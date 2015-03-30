//
//  FMDBList.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 15.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class FMDBList : NSObject, DKListStorage {
    
    var db :FMDatabase? = nil;
    var isTableChecked: Bool = false;
    
    let databasePath: String;
    let tableName: String;
    
    let queryCreate: String;
    let queryCreateIndex: String;
    let queryCreateFilter: String;
    
    let queryAdd: String;
    let queryItem: String;

    let queryDelete: String;
    let queryDeleteAll: String;
    
    let queryForwardFirst: String;
    let queryForwardMore: String;
    
    let queryForwardFilterFirst: String;
    let queryForwardFilterMore: String;
    
    init (databasePath: String, tableName: String){
        self.databasePath = databasePath
        self.tableName = tableName;
        
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
            "\"ID\" INTEGER NOT NULL," + // 0: id
            "\"SORT_KEY\" INTEGER NOT NULL," + // 1: sortKey
            "\"QUERY\" TEXT," + // 2: query
            "\"BYTES\" BLOB NOT NULL," + // 3: bytes
            "PRIMARY KEY(\"ID\"));";
        self.queryCreateIndex = "CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON " + tableName + " (\"SORT_KEY\");"
        self.queryCreateFilter = "CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON " + tableName + " (\"QUERY\", \"SORT_KEY\");"
        
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)";
        self.queryItem = "SELECT \"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\" FROM " + tableName + " WHERE \"ID\" = ?;";

        self.queryDeleteAll = "DELETE FROM " + tableName + ";";
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\"= ?;";
        
        self.queryForwardFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " ORDER BY SORT_KEY DESC LIMIT ?";
        self.queryForwardMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?";
        
        self.queryForwardFilterFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"QUERY\" LIKE ? OR \"QUERY\" LIKE ? ORDER BY SORT_KEY DESC LIMIT ?";
        self.queryForwardFilterMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?";
    }
    
    func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true;
        
        self.db = FMDatabase(path: databasePath)
        self.db!.open()
        if (!db!.tableExists(tableName)) {
            db!.executeUpdate(queryCreate)
            db!.executeUpdate(queryCreateIndex)
            db!.executeUpdate(queryCreateFilter)
        }
    }
    
    func updateOrAddWithDKListEngineRecord(valueContainer: DKListEngineRecord!) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryAdd, withArgumentsInArray: [valueContainer.getKey().toNSNumber(), valueContainer.dbQuery(), valueContainer.getOrder().toNSNumber(),
            valueContainer.getData().toNSData()])
        db!.commit()
    }
    
    func updateOrAddWithJavaUtilList(items: JavaUtilList!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<items.size() {
            let record = items.getWithInt(i) as! DKListEngineRecord;
            db!.executeUpdate(queryAdd, record.getKey().toNSNumber(), record.dbQuery(), record.getOrder().toNSNumber(),
                            record.getData().toNSData())
        }
        db!.commit()
    }
    
    func delete__WithLong(key: jlong) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDelete, key.toNSNumber());
        db!.commit()
    }
    
    func delete__WithLongArray(keys: IOSLongArray!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<keys.length() {
        var k = keys.longAtIndex(UInt(i));
           db!.executeUpdate(queryDelete, k.toNSNumber());
        }
        db!.commit()
    }
    
    func getCount() -> jint {
        checkTable();
        
        fatalError("Not implemented")
    }
    
    func clear() {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDeleteAll);
        db!.commit()
    }
    
    func loadItemWithLong(key: jlong) -> DKListEngineRecord! {
        checkTable();
        
        var result = db!.executeQuery(queryItem, key.toNSNumber());
        if (result == nil) {
            return nil
        }
        if (result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull){
                query = nil
            }
            var res = DKListEngineRecord(long: jlong(result!.longForColumn("ID")), withLong: jlong(result!.longForColumn("SORT_KEY")), withNSString: query as! String?, withByteArray: result!.dataForColumn("BYTES").toJavaBytes())
            result?.close()
            return res;
        } else {
            result?.close()
            return nil
        }
    }
    
    func loadForwardWithJavaLangLong(sortingKey: JavaLangLong!, withInt limit: jint) -> JavaUtilList! {
        checkTable();
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryForwardFirst, limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryForwardMore, sortingKey!.longValue, limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            res.addWithId(DKListEngineRecord(long: jlong(result!.longForColumn("ID")), withLong: jlong(result!.longForColumn("SORT_KEY")), withNSString: query as! String?, withByteArray: result!.dataForColumn("BYTES").toJavaBytes()))
        }
        result!.close()
        return res;
    }
    
    func loadForwardWithNSString(query: String!, withJavaLangLong sortingKey: JavaLangLong!, withInt limit: jint) -> JavaUtilList! {
        checkTable();
        
        checkTable();
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryForwardFilterFirst, query + "%", "% " + query + "%", limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryForwardFilterMore, query + "%", "% " + query + "%", sortingKey!.longValue, limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            res.addWithId(DKListEngineRecord(long: jlong(result!.longForColumn("ID")), withLong: jlong(result!.longForColumn("SORT_KEY")), withNSString: query as! String?, withByteArray: result!.dataForColumn("BYTES").toJavaBytes()))
        }
        result!.close()
        return res;
    }
    
    func loadBackwardWithJavaLangLong(sortingKey: JavaLangLong!, withInt limit: jint) -> JavaUtilList! {
        checkTable();
        
        fatalError("Not implemented")
    }
    
    func loadBackwardWithNSString(query: String!, withJavaLangLong sortingKey: JavaLangLong!, withInt limit: jint) -> JavaUtilList! {
        checkTable();
        
        fatalError("Not implemented")
    }
}