import 'dart:async';

import 'package:flutter/services.dart';

enum LoginType { wexin, facebook, google, apple }

class LoginPlugin {
  static const MethodChannel _channel = MethodChannel('login_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List?> auth(LoginType type) async {
    switch(type)
    {
      case LoginType.wexin:
        break;
      case LoginType.facebook:
        return await _channel.invokeListMethod('authToLogin', {"authType": 7});
      case LoginType.google:
        return await _channel.invokeListMethod('authToLogin', {"authType": 10});
      case LoginType.apple:
        break;
    }



  }


  static Future<List?> authByFacebook() async {
    return await auth(LoginType.facebook);
  }

  static Future<List?> authByGoogle() async {
    return await auth(LoginType.google);
  }

  static Future<dynamic> init(String id) async {
    return await _channel.invokeListMethod('umInit', {"id": id});
  }
}
