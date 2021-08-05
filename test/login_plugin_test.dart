import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:login_plugin/login_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('login_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await LoginPlugin.platformVersion, '42');
  });
}
