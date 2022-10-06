#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "AssetsServer.h"

@interface ResourceServerPlugin : CDVPlugin {}

@property AssetsServer * contextServer;

@property AssetsServer * serveServer;

- (void)start:(CDVInvokedUrlCommand*)command;

- (void)stop:(CDVInvokedUrlCommand*)command;

- (void)redirect:(CDVInvokedUrlCommand*)command;

@end
