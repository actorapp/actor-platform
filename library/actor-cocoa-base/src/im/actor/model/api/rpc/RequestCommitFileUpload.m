//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestCommitFileUpload.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestCommitFileUpload.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestCommitFileUpload.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcRequestCommitFileUpload () {
 @public
  IOSByteArray *uploadKey_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcRequestCommitFileUpload, uploadKey_, IOSByteArray *)


#line 20
@implementation ImActorModelApiRpcRequestCommitFileUpload


#line 23
+ (ImActorModelApiRpcRequestCommitFileUpload *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcRequestCommitFileUpload_fromBytesWithByteArray_(data);
}


#line 29
- (instancetype)initWithByteArray:(IOSByteArray *)uploadKey {
  ImActorModelApiRpcRequestCommitFileUpload_initWithByteArray_(self, uploadKey);
  return self;
}


#line 33
- (instancetype)init {
  ImActorModelApiRpcRequestCommitFileUpload_init(self);
  return self;
}


#line 37
- (IOSByteArray *)getUploadKey {
  return self->uploadKey_;
}


#line 42
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->uploadKey_ = [((BSBserValues *) nil_chk(values)) getBytesWithInt:1];
}


#line 47
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->uploadKey_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeBytesWithInt:1 withByteArray:self->uploadKey_];
}


#line 55
- (NSString *)description {
  NSString *res = @"rpc CommitFileUpload{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 62
- (jint)getHeaderKey {
  return ImActorModelApiRpcRequestCommitFileUpload_HEADER;
}

@end


#line 23
ImActorModelApiRpcRequestCommitFileUpload *ImActorModelApiRpcRequestCommitFileUpload_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcRequestCommitFileUpload_initialize();
  
#line 24
  return ((ImActorModelApiRpcRequestCommitFileUpload *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcRequestCommitFileUpload_init(), data));
}


#line 29
void ImActorModelApiRpcRequestCommitFileUpload_initWithByteArray_(ImActorModelApiRpcRequestCommitFileUpload *self, IOSByteArray *uploadKey) {
  (void) ImActorModelNetworkParserRequest_init(self);
  
#line 30
  self->uploadKey_ = uploadKey;
}


#line 29
ImActorModelApiRpcRequestCommitFileUpload *new_ImActorModelApiRpcRequestCommitFileUpload_initWithByteArray_(IOSByteArray *uploadKey) {
  ImActorModelApiRpcRequestCommitFileUpload *self = [ImActorModelApiRpcRequestCommitFileUpload alloc];
  ImActorModelApiRpcRequestCommitFileUpload_initWithByteArray_(self, uploadKey);
  return self;
}


#line 33
void ImActorModelApiRpcRequestCommitFileUpload_init(ImActorModelApiRpcRequestCommitFileUpload *self) {
  (void) ImActorModelNetworkParserRequest_init(self);
}


#line 33
ImActorModelApiRpcRequestCommitFileUpload *new_ImActorModelApiRpcRequestCommitFileUpload_init() {
  ImActorModelApiRpcRequestCommitFileUpload *self = [ImActorModelApiRpcRequestCommitFileUpload alloc];
  ImActorModelApiRpcRequestCommitFileUpload_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcRequestCommitFileUpload)
