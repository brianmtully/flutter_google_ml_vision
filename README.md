# Google ML Kit Vision Plugin

(https://pub.dev/packages/google_ml_vision)

A Flutter plugin to use the capabilities of on-device Google ML Kit Vision APIs

## Usage

To use this plugin, add `google_ml_vision` as a [dependency in your pubspec.yaml file](https://flutter.io/platform-plugins/).


## Using an ML Vision Detector

### 1. Create a `GoogleVisionImage`.

Create a `GoogleVisionImage` object from your image. To create a `GoogleVisionImage` from an image `File` object:

```dart
final File imageFile = getImageFile();
final GoogleVisionImage visionImage = GoogleVisionImage.fromFile(imageFile);
```

### 2. Create an instance of a detector.

```dart
final BarcodeDetector barcodeDetector = GoogleVision.instance.barcodeDetector();
final FaceDetector faceDetector = GoogleVision.instance.faceDetector();
final ImageLabeler labeler = GoogleVision.instance.imageLabeler();
final TextRecognizer textRecognizer = GoogleVision.instance.textRecognizer();
```

You can also configure all detectors, except `TextRecognizer`, with desired options.

```dart
final ImageLabeler labeler = GoogleVision.instance.imageLabeler(
  ImageLabelerOptions(confidenceThreshold: 0.75),
);
```

### 3. Call `detectInImage()` or `processImage()` with `visionImage`.

```dart
final List<Barcode> barcodes = await barcodeDetector.detectInImage(visionImage);
final List<Face> faces = await faceDetector.processImage(visionImage);
final List<ImageLabel> labels = await labeler.processImage(visionImage);
final VisionText visionText = await textRecognizer.processImage(visionImage);
```

### 4. Extract data.

a. Extract barcodes.

```dart
for (Barcode barcode in barcodes) {
  final Rectangle<int> boundingBox = barcode.boundingBox;
  final List<Point<int>> cornerPoints = barcode.cornerPoints;

  final String rawValue = barcode.rawValue;

  final BarcodeValueType valueType = barcode.valueType;

  // See API reference for complete list of supported types
  switch (valueType) {
    case BarcodeValueType.wifi:
      final String ssid = barcode.wifi.ssid;
      final String password = barcode.wifi.password;
      final BarcodeWiFiEncryptionType type = barcode.wifi.encryptionType;
      break;
    case BarcodeValueType.url:
      final String title = barcode.url.title;
      final String url = barcode.url.url;
      break;
  }
}
```

b. Extract faces.

```dart
for (Face face in faces) {
  final Rectangle<int> boundingBox = face.boundingBox;

  final double rotY = face.headEulerAngleY; // Head is rotated to the right rotY degrees
  final double rotZ = face.headEulerAngleZ; // Head is tilted sideways rotZ degrees

  // If landmark detection was enabled with FaceDetectorOptions (mouth, ears,
  // eyes, cheeks, and nose available):
  final FaceLandmark leftEar = face.getLandmark(FaceLandmarkType.leftEar);
  if (leftEar != null) {
    final Point<double> leftEarPos = leftEar.position;
  }

  // If classification was enabled with FaceDetectorOptions:
  if (face.smilingProbability != null) {
    final double smileProb = face.smilingProbability;
  }

  // If face tracking was enabled with FaceDetectorOptions:
  if (face.trackingId != null) {
    final int id = face.trackingId;
  }
}
```

c. Extract labels.

```dart
for (ImageLabel label in labels) {
  final String text = label.text;
  final String entityId = label.entityId;
  final double confidence = label.confidence;
}
```

d. Extract text.

```dart
String text = visionText.text;
for (TextBlock block in visionText.blocks) {
  final Rect boundingBox = block.boundingBox;
  final List<Offset> cornerPoints = block.cornerPoints;
  final String text = block.text;
  final List<RecognizedLanguage> languages = block.recognizedLanguages;

  for (TextLine line in block.lines) {
    // Same getters as TextBlock
    for (TextElement element in line.elements) {
      // Same getters as TextBlock
    }
  }
}
```

### 5. Release resources with `close()`.

```dart
barcodeDetector.close();
faceDetector.close();
labeler.close();
textRecognizer.close();
```

## Getting Started

See the `example` directory for a complete sample app using Google Machine Learning.
