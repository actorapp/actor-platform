//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/rpc/RequestNotifyBannerView.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestNotifyBannerView.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface APRequestNotifyBannerView () {
 @public
  jint bannerId_;
  jint viewDuration_;
}

@end

@implementation APRequestNotifyBannerView

+ (APRequestNotifyBannerView *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APRequestNotifyBannerView_fromBytesWithByteArray_(data);
}

- (instancetype)initWithInt:(jint)bannerId
                    withInt:(jint)viewDuration {
  APRequestNotifyBannerView_initWithInt_withInt_(self, bannerId, viewDuration);
  return self;
}

- (instancetype)init {
  APRequestNotifyBannerView_init(self);
  return self;
}

- (jint)getBannerId {
  return self->bannerId_;
}

- (jint)getViewDuration {
  return self->viewDuration_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->bannerId_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->viewDuration_ = [values getIntWithInt:2];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->bannerId_];
  [writer writeIntWithInt:2 withInt:self->viewDuration_];
}

- (NSString *)description {
  NSString *res = @"rpc NotifyBannerView{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"bannerId=", self->bannerId_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", viewDuration=", self->viewDuration_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APRequestNotifyBannerView_HEADER;
}

@end

APRequestNotifyBannerView *APRequestNotifyBannerView_fromBytesWithByteArray_(IOSByteArray *data) {
  APRequestNotifyBannerView_initialize();
  return ((APRequestNotifyBannerView *) BSBser_parseWithBSBserObject_withByteArray_(new_APRequestNotifyBannerView_init(), data));
}

void APRequestNotifyBannerView_initWithInt_withInt_(APRequestNotifyBannerView *self, jint bannerId, jint viewDuration) {
  (void) APRequest_init(self);
  self->bannerId_ = bannerId;
  self->viewDuration_ = viewDuration;
}

APRequestNotifyBannerView *new_APRequestNotifyBannerView_initWithInt_withInt_(jint bannerId, jint viewDuration) {
  APRequestNotifyBannerView *self = [APRequestNotifyBannerView alloc];
  APRequestNotifyBannerView_initWithInt_withInt_(self, bannerId, viewDuration);
  return self;
}

void APRequestNotifyBannerView_init(APRequestNotifyBannerView *self) {
  (void) APRequest_init(self);
}

APRequestNotifyBannerView *new_APRequestNotifyBannerView_init() {
  APRequestNotifyBannerView *self = [APRequestNotifyBannerView alloc];
  APRequestNotifyBannerView_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APRequestNotifyBannerView)
