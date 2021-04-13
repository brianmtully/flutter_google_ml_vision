// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.brianmtully.flutter.plugins.googlemlvision;

        import androidx.annotation.NonNull;

        import io.flutter.embedding.engine.plugins.FlutterPlugin;
        import io.flutter.plugin.common.MethodCall;
        import io.flutter.plugin.common.MethodChannel;
        import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
        import io.flutter.plugin.common.MethodChannel.Result;
        import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterPlugindExamplePlugin */
public class GoogleMlVisionPlugin implements FlutterPlugin {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "plugins.flutter.brianmtully.com/google_ml_vision");

    channel.setMethodCallHandler(new MlVisionHandler(flutterPluginBinding.getApplicationContext()));
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}

