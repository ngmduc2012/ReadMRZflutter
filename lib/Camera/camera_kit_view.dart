import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'camera_kit_controller.dart';
import 'dart:developer' as developer;

enum CameraPosition { back, front }

// ignore: must_be_immutable
class CameraKitView extends StatefulWidget {
  final CameraPosition cameraPosition;
  final bool hasFaceDetection;
  final Function showTextResult;
  final CameraKitController cameraKitController;

  _CameraKitViewState viewState;

  CameraKitView(
      {Key key,
      this.hasFaceDetection = false,
      this.cameraKitController,
      this.cameraPosition = CameraPosition.back,
        this.showTextResult,})
      : super(key: key);

  @override
  _CameraKitViewState createState() {
    if (cameraKitController != null) cameraKitController.setView(this);
    viewState = _CameraKitViewState();
    return viewState;
  }
}

class _CameraKitViewState extends State<CameraKitView>
    with WidgetsBindingObserver {
  NativeCameraKitController controller;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  void _onPlatformViewCreated(int id) {
    this.controller = new NativeCameraKitController._(id, context, widget);
    this.controller.initCamera();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        print("Flutter Life Cycle: resumed");
        if (controller != null) controller.resumeCamera();
        break;
      case AppLifecycleState.inactive:
        print("Flutter Life Cycle: inactive");
        if (Platform.isIOS) {
          controller.pauseCamera();
        }
        break;
      case AppLifecycleState.paused:
        print("Flutter Life Cycle: paused");
        controller.pauseCamera();
        break;
      default:
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final String viewType = 'camera';
    final Map<String, dynamic> creationParams = <String, dynamic>{};

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return PlatformViewLink(
          viewType: viewType,
          surfaceFactory:
              (BuildContext context, PlatformViewController controller) {
            return AndroidViewSurface(
              controller: controller,
              gestureRecognizers: const <
                  Factory<OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
            );
          },
          onCreatePlatformView: (PlatformViewCreationParams params) {
            return PlatformViewsService.initSurfaceAndroidView(
              id: params.id,
              viewType: viewType,
              layoutDirection: TextDirection.ltr,
              creationParams: creationParams,
              creationParamsCodec: StandardMessageCodec(),
            )
              ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
              ..addOnPlatformViewCreatedListener(_onPlatformViewCreated)
              ..create();
          },
        );
      case TargetPlatform.iOS:
        return UiKitView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onPlatformViewCreated: _onPlatformViewCreated,
        );
      default:
        throw UnsupportedError("Unsupported platform view");
    }
  }
}

class NativeCameraKitController {
  BuildContext context;
  CameraKitView widget;

  NativeCameraKitController._(int id, this.context, this.widget)
      : _channel =
            new MethodChannel('camera/camera_' + id.toString());

  final MethodChannel _channel;

  Future<dynamic> nativeMethodCallHandler(MethodCall methodCall) async {
    // developer.log("////////${methodCall.arguments}", name: 'ok');
    if (methodCall.method == "callBack" &&
        widget.showTextResult != null) {
      widget.showTextResult(methodCall.arguments);
      // developer.log("////////${methodCall.arguments}", name: 'ok');
    }
    // if (methodCall.method == "getMRZ" &&
    //     widget.showTextResult != null) {
    //   widget.showTextResult(methodCall.arguments);
    // }
    return null;
  }

  String _getCharCameraPosition(CameraPosition cameraPosition) {
    String pos;
    switch (cameraPosition) {
      case CameraPosition.back:
        pos = "B";
        break;
      case CameraPosition.front:
        pos = "F";
        break;
    }
    return pos;
  }

  void initCamera() async {
    _channel.setMethodCallHandler(nativeMethodCallHandler);
    if (Platform.isAndroid) {
      _channel.invokeMethod('initCamera', {
        "cameraPosition": _getCharCameraPosition(widget.cameraPosition),
        "hasFaceDetection": widget.hasFaceDetection
      });
    } else {
      _channel.invokeMethod('initCamera', {
        "cameraPosition": _getCharCameraPosition(widget.cameraPosition),
        "hasFaceDetection": widget.hasFaceDetection
      });
    }
  }

  Future<void> flipCamera() async {
    return _channel.invokeMethod('flipCamera');
  }

  Future<void> resumeCamera() async {
    return _channel.invokeMethod('resumeCamera');
  }

  Future<void> pauseCamera() async {
    return _channel.invokeMethod('pauseCamera');
  }

  Future<void> textResult() {
    return _channel.invokeMethod("textResult");
  }
}
