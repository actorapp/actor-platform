//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/api/rpc/ResponseIntegrationToken.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/ResponseIntegrationToken.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"

@interface APResponseIntegrationToken () {
 @public
  NSString *token_;
  NSString *url_;
}

@end

J2OBJC_FIELD_SETTER(APResponseIntegrationToken, token_, NSString *)
J2OBJC_FIELD_SETTER(APResponseIntegrationToken, url_, NSString *)

@implementation APResponseIntegrationToken

+ (APResponseIntegrationToken *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APResponseIntegrationToken_fromBytesWithByteArray_(data);
}

- (instancetype)initWithNSString:(NSString *)token
                    withNSString:(NSString *)url {
  APResponseIntegrationToken_initWithNSString_withNSString_(self, token, url);
  return self;
}

- (instancetype)init {
  APResponseIntegrationToken_init(self);
  return self;
}

- (NSString *)getToken {
  return self->token_;
}

- (NSString *)getUrl {
  return self->url_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->token_ = [((BSBserValues *) nil_chk(values)) getStringWithInt:1];
  self->url_ = [values getStringWithInt:2];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->token_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeStringWithInt:1 withNSString:self->token_];
  if (self->url_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:2 withNSString:self->url_];
}

- (NSString *)description {
  NSString *res = @"response IntegrationToken{";
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APResponseIntegrationToken_HEADER;
}

@end

APResponseIntegrationToken *APResponseIntegrationToken_fromBytesWithByteArray_(IOSByteArray *data) {
  APResponseIntegrationToken_initialize();
  return ((APResponseIntegrationToken *) BSBser_parseWithBSBserObject_withByteArray_(new_APResponseIntegrationToken_init(), data));
}

void APResponseIntegrationToken_initWithNSString_withNSString_(APResponseIntegrationToken *self, NSString *token, NSString *url) {
  (void) APResponse_init(self);
  self->token_ = token;
  self->url_ = url;
}

APResponseIntegrationToken *new_APResponseIntegrationToken_initWithNSString_withNSString_(NSString *token, NSString *url) {
  APResponseIntegrationToken *self = [APResponseIntegrationToken alloc];
  APResponseIntegrationToken_initWithNSString_withNSString_(self, token, url);
  return self;
}

void APResponseIntegrationToken_init(APResponseIntegrationToken *self) {
  (void) APResponse_init(self);
}

APResponseIntegrationToken *new_APResponseIntegrationToken_init() {
  APResponseIntegrationToken *self = [APResponseIntegrationToken alloc];
  APResponseIntegrationToken_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APResponseIntegrationToken)
