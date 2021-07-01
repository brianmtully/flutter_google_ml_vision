// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

part of google_ml_vision;

enum _ImageType { file, bytes }

/// Indicates the image rotation.
///
/// Rotation is counter-clockwise.
enum ImageRotation { rotation0, rotation90, rotation180, rotation270 }

/// Detected language from text recognition in regular and document images.
class RecognizedLanguage {
  RecognizedLanguage._(dynamic data) : languageCode = data['languageCode'];

  /// The BCP-47 language code, such as, en-US or sr-Latn. For more information,
  /// see http://www.unicode.org/reports/tr35/#Unicode_locale_identifier.
  final String? languageCode;
}

/// The Google machine learning vision API.
///
/// You can get an instance by calling [GoogleVision.instance] and then get
/// a detector from the instance:
///
/// ```dart
/// TextRecognizer textRecognizer = GoogleVision.instance.textRecognizer();
/// ```
class GoogleVision {
  GoogleVision._();

  @visibleForTesting
  static const MethodChannel channel =
      MethodChannel('plugins.flutter.brianmtully.com/google_ml_vision');

  @visibleForTesting
  static int nextHandle = 0;

  /// Singleton of [GoogleVision].
  ///
  /// Use this get an instance of a detector:
  ///
  /// ```dart
  /// TextRecognizer textRecognizer = GoogleVision.instance.textRecognizer();
  /// ```
  static final GoogleVision instance = GoogleVision._();

  /// Creates an instance of [BarcodeDetector].
  BarcodeDetector barcodeDetector([BarcodeDetectorOptions? options]) {
    return BarcodeDetector._(
      options ?? const BarcodeDetectorOptions(),
      nextHandle++,
    );
  }

  /// Creates an instance of [FaceDetector].
  FaceDetector faceDetector([FaceDetectorOptions? options]) {
    return FaceDetector._(
      options ?? const FaceDetectorOptions(),
      nextHandle++,
    );
  }

  /// Creates an on device instance of [ImageLabeler].
  ImageLabeler imageLabeler([ImageLabelerOptions? options]) {
    return ImageLabeler._(
      options: options ?? const ImageLabelerOptions(),
      handle: nextHandle++,
    );
  }

  /// Creates an instance of [TextRecognizer].
  TextRecognizer textRecognizer() {
    return TextRecognizer._(
      handle: nextHandle++,
    );
  }
}

/// Represents an image object used for both on-device and cloud API detectors.
///
/// Create an instance by calling one of the factory constructors.
class GoogleVisionImage {
  GoogleVisionImage._({
    required _ImageType type,
    GoogleVisionImageMetadata? metadata,
    String? filePath,
    Uint8List? bytes,
  })  : _filePath = filePath,
        _metadata = metadata,
        _bytes = bytes,
        _type = type;

  /// Construct a [GoogleVisionImage] from a file.
  factory GoogleVisionImage.fromFile(File imageFile) {
    return GoogleVisionImage._(
      type: _ImageType.file,
      filePath: imageFile.path,
    );
  }

  /// Construct a [GoogleVisionImage] from a file path.
  factory GoogleVisionImage.fromFilePath(String imagePath) {
    return GoogleVisionImage._(
      type: _ImageType.file,
      filePath: imagePath,
    );
  }

  /// Construct a [GoogleVisionImage] from a list of bytes.
  ///
  /// On Android, expects `android.graphics.ImageFormat.NV21` format. Note:
  /// Concatenating the planes of `android.graphics.ImageFormat.YUV_420_888`
  /// into a single plane, converts it to `android.graphics.ImageFormat.NV21`.
  ///
  /// On iOS, expects `kCVPixelFormatType_32BGRA` format. However, this should
  /// work with most formats from `kCVPixelFormatType_*`.
  factory GoogleVisionImage.fromBytes(
    Uint8List bytes,
    GoogleVisionImageMetadata metadata,
  ) {
    return GoogleVisionImage._(
      type: _ImageType.bytes,
      bytes: bytes,
      metadata: metadata,
    );
  }

  final Uint8List? _bytes;
  final String? _filePath;
  final GoogleVisionImageMetadata? _metadata;
  final _ImageType _type;

  Map<String, dynamic> _serialize() => <String, dynamic>{
        'type': _enumToString(_type),
        'bytes': _bytes,
        'path': _filePath,
        'metadata': _type == _ImageType.bytes ? _metadata!._serialize() : null,
      };
}

/// Plane attributes to create the image buffer on iOS.
///
/// When using iOS, [bytesPerRow], [height], and [width] throw [AssertionError]
/// if `null`.
class GoogleVisionImagePlaneMetadata {
  GoogleVisionImagePlaneMetadata({
    required this.bytesPerRow,
    this.height,
    this.width,
  })  : assert(defaultTargetPlatform != TargetPlatform.iOS || height != null),
        assert(defaultTargetPlatform != TargetPlatform.iOS || width != null);

  /// The row stride for this color plane, in bytes.
  final int bytesPerRow;

  /// Height of the pixel buffer on iOS.
  final int? height;

  /// Width of the pixel buffer on iOS.
  final int? width;

  Map<String, dynamic> _serialize() => <String, dynamic>{
        'bytesPerRow': bytesPerRow,
        'height': height,
        'width': width,
      };
}

/// Image metadata used by [GoogleVision] detectors.
///
/// [rotation] defaults to [ImageRotation.rotation0]. Currently only rotates on
/// Android.
///
/// When using iOS, [rawFormat] and [planeData] throw [AssertionError] if
/// `null`.
class GoogleVisionImageMetadata {
  GoogleVisionImageMetadata({
    required this.size,
    this.rawFormat,
    this.planeData,
    this.rotation = ImageRotation.rotation0,
  })  : assert(
          defaultTargetPlatform != TargetPlatform.iOS || rawFormat != null,
        ),
        assert(
          defaultTargetPlatform != TargetPlatform.iOS || planeData != null,
        );

  /// Size of the image in pixels.
  final Size size;

  /// Rotation of the image for Android.
  ///
  /// Not currently used on iOS.
  final ImageRotation rotation;

  /// Raw version of the format from the iOS platform.
  ///
  /// Since iOS can use any planar format, this format will be used to create
  /// the image buffer on iOS.
  ///
  /// On iOS, this is a `FourCharCode` constant from Pixel Format Identifiers.
  /// See https://developer.apple.com/documentation/corevideo/1563591-pixel_format_identifiers?language=objc
  ///
  /// Not used on Android.
  final Object? rawFormat;

  /// The plane attributes to create the image buffer on iOS.
  ///
  /// Not used on Android.
  final List<GoogleVisionImagePlaneMetadata>? planeData;

  int _imageRotationToInt(ImageRotation rotation) {
    switch (rotation) {
      case ImageRotation.rotation90:
        return 90;
      case ImageRotation.rotation180:
        return 180;
      case ImageRotation.rotation270:
        return 270;
      default:
        assert(rotation == ImageRotation.rotation0);
        return 0;
    }
  }

  Map<String, dynamic> _serialize() => <String, dynamic>{
        'width': size.width,
        'height': size.height,
        'rotation': _imageRotationToInt(rotation),
        'rawFormat': rawFormat,
        'planeData': planeData
            ?.map((GoogleVisionImagePlaneMetadata plane) => plane._serialize())
            .toList(),
      };
}

String _enumToString(dynamic enumValue) {
  final String enumString = enumValue.toString();
  return enumString.substring(enumString.indexOf('.') + 1);
}
