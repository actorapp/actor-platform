//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/ResponseGetFileUploadPartUrl.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/ResponseGetFileUploadPartUrl.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/ResponseGetFileUploadPartUrl.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcResponseGetFileUploadPartUrl () {
 @public
  NSString *url_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcResponseGetFileUploadPartUrl, url_, NSString *)


#line 24
@implementation ImActorModelApiRpcResponseGetFileUploadPartUrl


#line 27
+ (ImActorModelApiRpcResponseGetFileUploadPartUrl *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcResponseGetFileUploadPartUrl_fromBytesWithByteArray_(data);
}


#line 33
- (instancetype)initWithNSString:(NSString *)url {
  ImActorModelApiRpcResponseGetFileUploadPartUrl_initWithNSString_(self, url);
  return self;
}


#line 37
- (instancetype)init {
  ImActorModelApiRpcResponseGetFileUploadPartUrl_init(self);
  return self;
}


#line 41
- (NSString *)getUrl {
  return self->url_;
}


#line 46
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->url_ = [((BSBserValues *) nil_chk(values)) getStringWithInt:1];
}


#line 51
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->url_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeStringWithInt:1 withNSString:self->url_];
}


#line 59
- (NSString *)description {
  NSString *res = @"tuple GetFileUploadPartUrl{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 66
- (jint)getHeaderKey {
  return ImActorModelApiRpcResponseGetFileUploadPartUrl_HEADER;
}

@end


#line 27
ImActorModelApiRpcResponseGetFileUploadPartUrl *ImActorModelApiRpcResponseGetFileUploadPartUrl_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcResponseGetFileUploadPartUrl_initialize();
  
#line 28
  return ((ImActorModelApiRpcResponseGetFileUploadPartUrl *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcResponseGetFileUploadPartUrl_init(), data));
}


#line 33
void ImActorModelApiRpcResponseGetFileUploadPartUrl_initWithNSString_(ImActorModelApiRpcResponseGetFileUploadPartUrl *self, NSString *url) {
  (void) ImActorModelNetworkParserResponse_init(self);
  
#line 34
  self->url_ = url;
}


#line 33
ImActorModelApiRpcResponseGetFileUploadPartUrl *new_ImActorModelApiRpcResponseGetFileUploadPartUrl_initWithNSString_(NSString *url) {
  ImActorModelApiRpcResponseGetFileUploadPartUrl *self = [ImActorModelApiRpcResponseGetFileUploadPartUrl alloc];
  ImActorModelApiRpcResponseGetFileUploadPartUrl_initWithNSString_(self, url);
  return self;
}


#line 37
void ImActorModelApiRpcResponseGetFileUploadPartUrl_init(ImActorModelApiRpcResponseGetFileUploadPartUrl *self) {
  (void) ImActorModelNetworkParserResponse_init(self);
}


#line 37
ImActorModelApiRpcResponseGetFileUploadPartUrl *new_ImActorModelApiRpcResponseGetFileUploadPartUrl_init() {
  ImActorModelApiRpcResponseGetFileUploadPartUrl *self = [ImActorModelApiRpcResponseGetFileUploadPartUrl alloc];
  ImActorModelApiRpcResponseGetFileUploadPartUrl_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcResponseGetFileUploadPartUrl)
