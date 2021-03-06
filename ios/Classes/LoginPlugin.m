#import "LoginPlugin.h"
#if __has_include(<login_plugin/login_plugin-Swift.h>)
#import <login_plugin/login_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "login_plugin-Swift.h"
#endif

@implementation LoginPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftLoginPlugin registerWithRegistrar:registrar];
}
@end
