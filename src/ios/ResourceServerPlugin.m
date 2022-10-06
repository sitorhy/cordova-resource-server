#import "ResourceServerPlugin.h"
#import "ResourceServer.h"

@implementation ResourceServerPlugin

- (void)start:(CDVInvokedUrlCommand*)command
{
    NSString * contextPath;
    NSNumber * contextPort;
    NSString * servePath;
    NSNumber * servePort;
    contextPath = [command argumentAtIndex:0];
    contextPort = [command argumentAtIndex:1];
    servePath = [command argumentAtIndex:2];
    servePort = [command argumentAtIndex:3];
    
    CDVPluginResult * result = nil;
    
    @try {
        if(contextPort && [contextPort intValue]) {
            int nContextPort = [contextPort intValue];
            self.contextServer = [[AssetsServer alloc] init:nContextPort requestResourcesOn:(servePort ? [servePort intValue] : 10433)];
        }
        
        if(servePort && [servePort intValue]) {
            int nServePort = [servePort intValue];
            self.serveServer = [[AssetsServer alloc] init:nServePort];
        }
        
        if(self.serveServer) {
            [self.serveServer serve:servePath];
        }
        
        if(self.contextServer) {
            [self.contextServer start:contextPath];
        }
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }
    @catch(NSException * e) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)stop:(CDVInvokedUrlCommand*)command
{
    if(self.serveServer) {
        [self.serveServer stop];
    }
    
    if(self.contextServer) {
        [self.serveServer stop];
    }
}

- (void)redirect:(CDVInvokedUrlCommand*)command
{
    dispatch_async(dispatch_get_main_queue(), ^{
        CDVPluginResult * result = nil;
        @try {
            NSString * appURL = [command argumentAtIndex:0];
            NSURL * url = [[NSURL alloc] initWithString:appURL];
            NSURLRequest* appReq = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:20.0];
            [self.webViewEngine loadRequest:appReq];
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        @catch(NSException * e) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    });
}

@end
