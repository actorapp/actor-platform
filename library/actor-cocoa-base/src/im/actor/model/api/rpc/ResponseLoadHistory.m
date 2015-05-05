//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/ResponseLoadHistory.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/ResponseLoadHistory.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/HistoryMessage.h"
#include "im/actor/model/api/User.h"
#include "im/actor/model/api/rpc/ResponseLoadHistory.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"

@interface ImActorModelApiRpcResponseLoadHistory () {
 @public
  id<JavaUtilList> history_;
  id<JavaUtilList> users_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcResponseLoadHistory, history_, id<JavaUtilList>)
J2OBJC_FIELD_SETTER(ImActorModelApiRpcResponseLoadHistory, users_, id<JavaUtilList>)


#line 24
@implementation ImActorModelApiRpcResponseLoadHistory


#line 27
+ (ImActorModelApiRpcResponseLoadHistory *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcResponseLoadHistory_fromBytesWithByteArray_(data);
}


#line 34
- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)history
                    withJavaUtilList:(id<JavaUtilList>)users {
  ImActorModelApiRpcResponseLoadHistory_initWithJavaUtilList_withJavaUtilList_(self, history, users);
  return self;
}


#line 39
- (instancetype)init {
  ImActorModelApiRpcResponseLoadHistory_init(self);
  return self;
}


#line 43
- (id<JavaUtilList>)getHistory {
  return self->history_;
}

- (id<JavaUtilList>)getUsers {
  return self->users_;
}


#line 52
- (void)parseWithBSBserValues:(BSBserValues *)values {
  id<JavaUtilList> _history = new_JavaUtilArrayList_init();
  for (jint i = 0; i < [((BSBserValues *) nil_chk(values)) getRepeatedCountWithInt:1]; i++) {
    [_history addWithId:new_ImActorModelApiHistoryMessage_init()];
  }
  self->history_ = [values getRepeatedObjWithInt:1 withJavaUtilList:_history];
  id<JavaUtilList> _users = new_JavaUtilArrayList_init();
  for (jint i = 0; i < [values getRepeatedCountWithInt:2]; i++) {
    [_users addWithId:new_ImActorModelApiUser_init()];
  }
  self->users_ = [values getRepeatedObjWithInt:2 withJavaUtilList:_users];
}


#line 66
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeRepeatedObjWithInt:1 withJavaUtilList:self->history_];
  [writer writeRepeatedObjWithInt:2 withJavaUtilList:self->users_];
}


#line 72
- (NSString *)description {
  NSString *res = @"tuple LoadHistory{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 79
- (jint)getHeaderKey {
  return ImActorModelApiRpcResponseLoadHistory_HEADER;
}

@end


#line 27
ImActorModelApiRpcResponseLoadHistory *ImActorModelApiRpcResponseLoadHistory_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcResponseLoadHistory_initialize();
  
#line 28
  return ((ImActorModelApiRpcResponseLoadHistory *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcResponseLoadHistory_init(), data));
}

void ImActorModelApiRpcResponseLoadHistory_initWithJavaUtilList_withJavaUtilList_(ImActorModelApiRpcResponseLoadHistory *self, id<JavaUtilList> history, id<JavaUtilList> users) {
  (void) ImActorModelNetworkParserResponse_init(self);
  
#line 35
  self->history_ = history;
  self->users_ = users;
}


#line 34
ImActorModelApiRpcResponseLoadHistory *new_ImActorModelApiRpcResponseLoadHistory_initWithJavaUtilList_withJavaUtilList_(id<JavaUtilList> history, id<JavaUtilList> users) {
  ImActorModelApiRpcResponseLoadHistory *self = [ImActorModelApiRpcResponseLoadHistory alloc];
  ImActorModelApiRpcResponseLoadHistory_initWithJavaUtilList_withJavaUtilList_(self, history, users);
  return self;
}


#line 39
void ImActorModelApiRpcResponseLoadHistory_init(ImActorModelApiRpcResponseLoadHistory *self) {
  (void) ImActorModelNetworkParserResponse_init(self);
}


#line 39
ImActorModelApiRpcResponseLoadHistory *new_ImActorModelApiRpcResponseLoadHistory_init() {
  ImActorModelApiRpcResponseLoadHistory *self = [ImActorModelApiRpcResponseLoadHistory alloc];
  ImActorModelApiRpcResponseLoadHistory_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcResponseLoadHistory)
