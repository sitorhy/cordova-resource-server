//
//  ResourceServer.m
//  local-dev-server-mac
//
//  Created by Sitorhy on 2022/9/4.
//

#import "ResourceServer.h"
#import <DDLog.h>
#import <DDTTYLogger.h>

static const int ddLogLevel = LOG_LEVEL_VERBOSE;

@implementation ResourceServer
{
    NSMutableDictionary * MIME_TYPES;
}

+ (void)initialize
{
    [DDLog addLogger:[DDTTYLogger sharedInstance]];
}

- (instancetype)init:(NSInteger)contextPort requestResourcesOn:(NSInteger)servePort
{
    self = [super init];
    if (self) {
        self.servePort = servePort;
        self.contextPort = contextPort;
        self.httpServer = [[RoutingHTTPServer alloc] init];
        [self.httpServer setType:@"_http._tcp."];
        [self.httpServer setDefaultHeader:@"Server" value:@"Embedded Resource Server"];
        [self.httpServer setDefaultHeader:@"Access-Control-Allow-Origin" value:@"*"];
        [self.httpServer setDefaultHeader:@"Access-Control-Max-Age" value:@"3628800"];
        [self.httpServer setDefaultHeader:@"Access-Control-Allow-Methods" value:@"GET, POST, PUT, OPTIONS, HEAD"];
        [self.httpServer setDefaultHeader:@"Access-Control-Allow-Headers" value:@"Content-Type,Content-Length, Authorization,Origin,Accept,X-Requested-With"];
        
        MIME_TYPES = [[NSMutableDictionary alloc] initWithDictionary:@{
            @"css": @"text/css",
            @"htm": @"text/html",
            @"html": @"text/html",
            @"xml": @"text/xml",
            @"java": @"text/x-java-source, text/java",
            @"md": @"text/plain",
            @"txt": @"text/plain",
            @"asc": @"text/plain",
            @"gif": @"image/gif",
            @"jpg": @"image/jpeg",
            @"jpeg": @"image/jpeg",
            @"png": @"image/png",
            @"svg": @"image/svg+xml",
            @"mp3": @"audio/mpeg",
            @"m3u": @"audio/mpeg-url",
            @"mp4": @"video/mp4",
            @"ogv": @"video/ogg",
            @"flv": @"video/x-flv",
            @"mov": @"video/quicktime",
            @"swf": @"application/x-shockwave-flash",
            @"js": @"application/javascript",
            @"pdf": @"application/pdf",
            @"doc": @"application/msword",
            @"ogg": @"application/x-ogg",
            @"zip": @"application/octet-stream",
            @"exe": @"application/octet-stream",
            @"class": @"application/octet-stream",
            @"m3u8": @"application/vnd.apple.mpegurl",
            @"ts": @"video/mp2t",
            @"json": @"application/json",
            @"ico": @"image/x-icon",
            @"wav": @"audio/vnd.wav",
            @"aac": @"audio/aac",
            @"m4a": @"audio/m4a",
            @"gz": @"application/x-gzip",
            @"tar": @"application/x-tar",
            @"mpg": @"video/mpeg",
            @"mpeg": @"video/mpeg",
            @"ra": @"audio/x-pn-realaudio",
            @"ram": @"audio/x-pn-realaudio",
            @"au": @"audio/basic",
            @"rtf": @"application/rtf",
            @"avi": @"video/x-msvideo",
            @"bin": @"application/octet-stream",
            @"bmp": @"image/bmp",
            @"bz": @"application/x-bzip",
            @"csv": @"text/csv",
            @"docx": @"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            @"eot": @"application/vnd.ms-fontobject",
            @"jar": @"application/java-archive",
            @"mid": @"audio/midi",
            @"midi": @"audio/midi",
            @"otf": @"font/otf",
            @"ppt": @"application/vnd.ms-powerpoint",
            @"pptx": @"application/vnd.openxmlformats-officedocument.presentationml.presentation",
            @"rar": @"application/x-rar-compressed",
            @"tif": @"image/tiff",
            @"tiff": @"image/tiff",
            @"ttf": @"font/ttf",
            @"weba": @"audio/webm",
            @"webm": @"video/webm",
            @"xhtml": @"application/xhtml+xml",
            @"xls": @"application/vnd.ms-excel",
            @"xlsx": @"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            @"woff2": @"font/woff2",
            @"woff": @"font/woff",
            @"7z": @"application/x-7z-compressed"
        }];
    }
    return self;
}

- (instancetype)init:(NSInteger)servePort
{
    return [self init:-1 requestResourcesOn:servePort];
}

- (NSMutableDictionary *)mimeTypes
{
    return MIME_TYPES;
}

- (void)handleResourceRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    NSArray * array = [request.params valueForKey:@"wildcards"];
    NSString * path = [array componentsJoinedByString:@""];
    NSString * filePath = [self.resourcePath stringByAppendingFormat:@"/%@",path];
    NSFileManager * fm = [NSFileManager defaultManager];
    BOOL isDir;
    if([fm fileExistsAtPath:filePath isDirectory:&isDir]){
        if(isDir) {
            [response setStatusCode:401];
            [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
            [response respondWithString:[NSString stringWithFormat:@"{\"success\":false,\"message\":\"%@\"}",@"Access Denied"]];
        }
        else
        {
            [response respondWithFile:filePath async:TRUE];
        }
    }
    else
    {
        [response setStatusCode:404];
        [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
        [response respondWithString:@"{\"success\":false,\"message\":\"Resource not found\"}"];
    }
}

- (void)handleIconRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    NSString * base64 = @"AAABAAEAEBAAAAEAGABoAwAAFgAAACgAAAAQAAAAIAAAAAEAGAAAAAAAAAMAACMuAAAjLgAAAAAAAAAAAACkZxWkZxajZRSiYxChYAyeXgedXASdWwKcWgGcWgGcWgGcWgGcWgGcWgGcWgGcWgGmaxunaxumahmkZxajZBCgYAueXgedXQWdWwGcWgGcWwKdXASdXAWdWwKcWgGcWgGpcCSpcCOobyGlaRijZROjZBCfXwmaWAOdXAOdWwKbWAKVTgCUTgCaVwGdWwKcWgGsdiysdi2pcSWxfTaueC+hYxClaBexezOcWwKbWACfXw/Rrn7XuI6mah2ZVQCdWwKwezSwfDembiHVt43RsIOcXASzgDvjy6ydWwKcWwLo1Lnv38rr2MDx4s6hYg6bWACyfzyzgD2tdy/o1bvp1LqgYg7Iom778eWpbyGfXwjNqXafXxChYRX+9+2+kFOVTwC0hEOzgT+6jVD47N337N2ueTHexKL///vCmF+bWQCaVwWbVwCdWgr3697DmWCUTgC2iEmxgD3OrH/059fy49DProHp1r3x4s3exaOfYQ2laBelZxbFm2P/+fKxezSYUwC4jU+zhEHn1LngyKnWuJH57uDo1LnLpnX16dmsdiyiZRLkza//+vPOqnmdWgGfXge7jlO8kVb369zProDDnGb/+/TbwZ2zgkD47uDDmmOgYxDHn2v47d+qcSWfXwmiYw66kVbMqnz57uHBmmO8klb47d7OrX+ufDbr2cHdw6GvezaueTPs2sLgx6agYw+maxq9lF3izK/x4s63i0/RsYb47d7AmGG1hkfVt5D+9+7269v369z47d//+fC9kVambB3Enm3QsYjNrYG/l2DKp3jOrYG9lFy9lFzAmGHRsojSs4nRsYbProHOrH25i06ueDLHpXfEoXHEoG/GonLDnGnAmWPCm2fBmmW/lmG6j1a5jVK3iU21hkezgkG0hEKzg0PHpXbIpnrJp3rHpXbHpHXGonPFn27EnmvDnGjCm2fBmWS/lmC9klq8kFa5i0+1iEnGo3PIpXjJpnnIpnnIpXjIpHbGonPFoG/EnmzDnGjBmWS/lmC8k1m7j1W5jFC3ikwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    NSData * data = [[NSData alloc] initWithBase64EncodedString:base64 options:0];
    [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"ico"]];
    [response respondWithData:data];
}

- (void)handleVersionRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
    [response respondWithString:@"{\"success\":true,\"data\":\"0.1.0\"}"];
}

- (void)handleListRequest:(RouteRequest *)request withResponse:(RouteResponse *)response
{
    NSArray * array = [request.params valueForKey:@"wildcards"];
    NSString * path = array == nil ? @"" : [array componentsJoinedByString:@""];
    NSString * filePath = [self.resourcePath stringByAppendingFormat:@"/%@",path];
    NSFileManager * fm = [NSFileManager defaultManager];
    BOOL isDir;
    if([fm fileExistsAtPath:filePath isDirectory:&isDir]){
        if(isDir) {
            NSError * error;
            NSArray* dirs = [fm contentsOfDirectoryAtPath:filePath error:&error];
            if(error)
            {
                [response setStatusCode:401];
                [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
                [response respondWithString:[NSString stringWithFormat:@"{\"success\":false,\"message\":%@}",error]];
            }
            else
            {
                NSArray * filtered = dirs;
                NSString * type = [request.params objectForKey:@"type"];
                NSMutableArray * infos = [[NSMutableArray alloc] init];
                if(type) {
                    filtered = [dirs filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(id  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
                        NSString * path = evaluatedObject;
                        NSString * ext = [path pathExtension];
                        return [type caseInsensitiveCompare:ext] == NSOrderedSame;
                    }]];
                }
                [filtered enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                    NSString *filename = (NSString *)obj;
                    NSError * error;
                    NSDictionary * attrs = [fm attributesOfItemAtPath:[filePath stringByAppendingPathComponent:filename] error:&error];
                    BOOL isItemDir;
                    [fm fileExistsAtPath:[filePath stringByAppendingPathComponent:filename] isDirectory: &isItemDir];
                    if(!error) {
                        long size = [attrs fileSize];
                        NSString * info = [NSString stringWithFormat:@"{\"size\":%ld,\"name\":\"%@\",\"isFile\":%@}",size,filename,isItemDir ? @"false" : @"true"];
                        [infos addObject:info];
                    }
                }];
                NSString * childrenInfos = [infos componentsJoinedByString:@","];
                NSDictionary * dirAttrs = [fm attributesOfItemAtPath:filePath error:nil];
                NSString * dirname = [[filePath lastPathComponent] stringByDeletingPathExtension];
                long dirSize = dirAttrs == nil ? 0 : [dirAttrs fileSize];
                NSString * dirInfo = [NSString stringWithFormat:@"{\"size\":%ld,\"type\":\"%@\",\"name\":\"%@\",\"isFile\":false,\"children\":[%@]}",dirSize,type,dirname,childrenInfos];
                [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
                [response respondWithString:[NSString stringWithFormat:@"{\"success\":true,\"data\":%@}",dirInfo]];
                
            }
        }
        else
        {
            NSError * error;
            NSDictionary * attrs = [fm attributesOfItemAtPath:filePath error:&error];
            if(error)
            {
                [response setStatusCode:401];
                [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
                [response respondWithString:[NSString stringWithFormat:@"{\"success\":false,\"message\":%@}",error]];
            }
            else
            {
                long size = [attrs fileSize];
                NSString * name = [[filePath lastPathComponent] stringByDeletingPathExtension];
                NSString * info = [NSString stringWithFormat:@"{\"size\":%ld,\"name\":\"%@\",\"isFile\":true}",size,name];
                [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
                [response respondWithString:[NSString stringWithFormat:@"{\"success\":true,\"data\":%@}",info]];
            }
        }
    }
    else
    {
        [response setStatusCode:404];
        [response setHeader:@"Content-Type" value:[[self mimeTypes] valueForKey:@"json"]];
        [response respondWithString:@"{\"success\":false,\"message\":\"Resource not found\"}"];
    }
}

- (void)handleMethod:(NSString *)method withPath:(NSString *)path target:(id)target selector:(SEL)selector
{
    [self.httpServer handleMethod:method withPath:path target:target selector:selector];
}

- (void)start:(NSString *)resourcePath
{
    self.resourcePath = resourcePath;
    NSError * error;
    [self.httpServer setPort:self.contextPort];
    [self.httpServer setDocumentRoot:self.resourcePath];
    BOOL success = [self.httpServer start:&error];
    if(!success)
    {
        DDLogError(@"Error starting HTTP Server: %@", error);
    }
}

- (void)serve:(NSString *)resourcePath
{
    self.resourcePath = resourcePath;
    [self.httpServer setPort:self.servePort];
    [self.httpServer handleMethod:@"GET" withPath:@"/" target:self selector:@selector(handleVersionRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/resource/*" target:self selector:@selector(handleResourceRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/list" target:self selector:@selector(handleListRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/list/*" target:self selector:@selector(handleListRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/version" target:self selector:@selector(handleVersionRequest:withResponse:)];
    [self.httpServer handleMethod:@"GET" withPath:@"/favicon.ico" target:self selector:@selector(handleIconRequest:withResponse:)];
    NSError * error;
    BOOL success = [self.httpServer start:&error];
    if(!success)
    {
        DDLogError(@"Error starting HTTP Server: %@", error);
    }
}

- (void)stop
{
    if(self.httpServer) {
        [self.httpServer stop];
    }
}

@end
