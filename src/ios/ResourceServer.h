//
//  ResourceServer.h
//  local-dev-server-mac
//
//  Created by Sitorhy on 2022/9/4.
//

#import <Foundation/Foundation.h>
#import <RoutingHTTPServer/RoutingHTTPServer.h>

NS_ASSUME_NONNULL_BEGIN

@interface ResourceServer : NSObject

@property(nonatomic) NSInteger contextPort;

@property(nonatomic) NSInteger servePort;

@property(nonatomic) NSString * resourcePath;

@property(nonatomic) RoutingHTTPServer * httpServer;

- (instancetype)init:(NSInteger) contextPort requestResourcesOn:(NSInteger)servePort;

- (instancetype)init:(NSInteger) servePort;

- (void)serve:(NSString *) resourcePath;

- (void)start:(NSString *) resourcePath;

- (void)stop;

- (NSMutableDictionary *)mimeTypes;

@end

NS_ASSUME_NONNULL_END
