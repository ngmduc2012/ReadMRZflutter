import 'camera_kit_view.dart';

class CameraKitController {
  CameraKitView cameraKitView;

  pauseCamera() {
    cameraKitView.viewState.controller.pauseCamera();
  }

  resumeCamera() {
    cameraKitView.viewState.controller.resumeCamera();
  }

  flipCamera() {
    cameraKitView.viewState.controller.flipCamera();
  }

  textResult() {
    cameraKitView.showTextResult.toString();
  }

  ///Connect view to this controller
  void setView(CameraKitView cameraKitView) {
    this.cameraKitView = cameraKitView;
  }
}
