//
//  AssetsServer.h
//  Dreambook TV
//
//  Created by Sitorhy on 2022/10/4.
//

#import "ResourceServer.h"

NS_ASSUME_NONNULL_BEGIN

@interface AssetsServer : ResourceServer

- (void)start:(NSString *) resourcePath;

- (void)serve:(NSString *) resourcePath;

@end

NS_ASSUME_NONNULL_END
