//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/bser/Bser.java
//

#ifndef _BSBser_H_
#define _BSBser_H_

#include "J2ObjC_header.h"

@class BSBserObject;
@class BSDataInput;
@class IOSByteArray;

@interface BSBser : NSObject

#pragma mark Public

+ (id)parseWithBSBserObject:(BSBserObject *)res
              withByteArray:(IOSByteArray *)data;

+ (id)parseWithBSBserObject:(BSBserObject *)res
            withBSDataInput:(BSDataInput *)inputStream;

@end

J2OBJC_EMPTY_STATIC_INIT(BSBser)

FOUNDATION_EXPORT id BSBser_parseWithBSBserObject_withBSDataInput_(BSBserObject *res, BSDataInput *inputStream);

FOUNDATION_EXPORT id BSBser_parseWithBSBserObject_withByteArray_(BSBserObject *res, IOSByteArray *data);

J2OBJC_TYPE_LITERAL_HEADER(BSBser)

typedef BSBser ImActorModelDroidkitBserBser;

#endif // _BSBser_H_
