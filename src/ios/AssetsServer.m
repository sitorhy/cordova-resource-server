//
//  AssetsServer.m
//  Dreambook TV
//
//  Created by Sitorhy on 2022/10/4.
//

#import "AssetsServer.h"

@implementation AssetsServer

- (NSString *)findResource:(NSBundle *)bundle withPath:(NSString *)path
{
    return [bundle pathForResource:path ofType:nil];
}

- (void)handleAssetsRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    NSArray * array = [request.params valueForKey:@"wildcards"];
    NSString * path = array == nil ? @"" : [array componentsJoinedByString:@""];
    NSArray<NSBundle *> *bundles = NSBundle.allBundles;
    __block NSString * resourcePath = nil;
    [bundles enumerateObjectsUsingBlock:^(NSBundle * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSString * full = [self findResource:obj withPath:path];
        if(full){
            resourcePath = full;
            *stop = TRUE;
        }
    }];
    
    if(resourcePath) {
        [response respondWithFile:resourcePath];
    }
    else {
        [response setStatusCode:404];
        [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
        [response respondWithString:@"{\"success\":false,\"message\":\"Resource not found\"}"];
    }
}

- (void)handleIndexRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    NSString * indexHtml = [self.resourcePath stringByAppendingPathComponent:@"index.html"];
    BOOL exist = [[NSFileManager defaultManager] fileExistsAtPath:indexHtml];
    if(exist){
        [response setStatusCode:200];
        NSString * content = [NSString stringWithContentsOfFile:indexHtml encoding:NSUTF8StringEncoding error:nil];
        NSRange range = [content rangeOfString:@"</body>"];
        if(range.length){
            NSMutableString * mu = [NSMutableString stringWithString:content];
            NSString * js = [NSString stringWithFormat:@"http://localhost:%ld/assets/www/cordova.js",(long)self.servePort];
            NSString * script = [NSString stringWithFormat:@"<script type=\"text/javascript\" src=\"%@\"></script>",js];
            [mu insertString:script atIndex:range.location];
            [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"html"]];
            [response respondWithString:mu];
        }
        else{
            [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"html"]];
            [response respondWithString:content];
        }
    }
    else{
        [response setStatusCode:404];
        [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
        [response respondWithString:@"{\"success\":false,\"message\":\"Resource not found\"}"];
    }
}

- (void)start:(NSString *) resourcePath
{
    [self.httpServer handleMethod:@"GET" withPath:@"/" target:self selector:@selector(handleIndexRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/index" target:self selector:@selector(handleIndexRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/index.html" target:self selector:@selector(handleIndexRequest:withResponse:)];
    [super start:resourcePath];
}

- (void)serve:(NSString *)resourcePath
{
    [self.httpServer handleMethod:@"GET" withPath:@"/assets/*" target:self selector:@selector(handleAssetsRequest:withResponse:)];
    [super serve:resourcePath];
}

@end
