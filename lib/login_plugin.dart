import 'dart:async';

import 'package:flutter/services.dart';

enum LoginType { wexin, facebook, google, apple }

class LoginPlugin {
  static const MethodChannel _channel = MethodChannel('login_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }





  static Future<List?> authByGoogle() async {
    return await _channel.invokeListMethod('authToLogin', {"authType": 10});
  }

  static Future<dynamic> init(String id) async {
    return await _channel.invokeListMethod('umInit', {"id": id});
  }
}
