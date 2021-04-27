// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// @dart=2.9

part of google_ml_vision;

/// Option for controlling additional trade-offs in performing face detection.
///
/// Accurate tends to detect more faces and may be more precise in determining
/// values such as position, at the cost of speed.
enum FaceDetectorMode { accurate, fast }

/// Available face landmarks detected by [FaceDetector].
enum FaceLandmarkType {
  bottomMouth,
  leftCheek,
  leftEar,
  leftEye,
  leftMouth,
  noseBase,
  rightCheek,
  rightEar,
  rightEye,
  rightMouth,
}

/// Available face contour types detected by [FaceDetector].
enum FaceContourType {
  allPoints,
  face,
  leftEye,
  leftEyebrowBottom,
  leftEyebrowTop,
  lowerLipBottom,
  lowerLipTop,
  noseBottom,
  noseBridge,
  rightEye,
  rightEyebrowBottom,
  rightEyebrowTop,
  upperLipBottom,
  upperLipTop
}

/// Detector for detecting faces in an input image.
///
/// A face detector is created via
/// `faceDetector([FaceDetectorOptions options])` in [GoogleVision]:
///
/// ```dart
/// final GoogleVisionImage image =
///     GoogleVisionImage.fromFilePath('path/to/file');
///
/// final FaceDetector faceDetector = GoogleVision.instance.faceDetector();
///
/// final List<Faces> faces = await faceDetector.processImage(image);
/// ```
class FaceDetector {
  FaceDetector._(this.options, this._handle) : assert(options != null);

  /// The options for the face detector.
  final FaceDetectorOptions options;
  final int _handle;
  bool _hasBeenOpened = false;
  bool _isClosed = false;

  /// Detects faces in the input image.
  Future<List<Face>> processImage(GoogleVisionImage visionImage) async {
    assert(!_isClosed);

    _hasBeenOpened = true;
    final List<dynamic> reply =
        await GoogleVision.channel.invokeListMethod<dynamic>(
      'FaceDetector#processImage',
      <String, dynamic>{
        'handle': _handle,
        'options': <String, dynamic>{
          'enableClassification': options.enableClassification,
          'enableLandmarks': options.enableLandmarks,
          'enableContours': options.enableContours,
          'enableTracking': options.enableTracking,
          'minFaceSize': options.minFaceSize,
          'mode': _enumToString(options.mode),
        },
      }..addAll(visionImage._serialize()),
    );

    final List<Face> faces = <Face>[];
    for (final dynamic data in reply) {
      faces.add(Face._(data));
    }

    return faces;
  }

  /// Release resources used by this detector.
  Future<void> close() {
    if (!_hasBeenOpened) _isClosed = true;
    if (_isClosed) return Future<void>.value();

    _isClosed = true;
    return GoogleVision.channel.invokeMethod<void>(
      'FaceDetector#close',
      <String, dynamic>{'handle': _handle},
    );
  }
}

/// Immutable options for configuring features of [FaceDetector].
///
/// Used to configure features such as classification, face tracking, speed,
/// etc.
class FaceDetectorOptions {
  /// Constructor for [FaceDetectorOptions].
  ///
  /// The parameter minFaceValue must be between 0.0 and 1.0, inclusive.
  const FaceDetectorOptions({
    this.enableClassification = false,
    this.enableLandmarks = false,
    this.enableContours = false,
    this.enableTracking = false,
    this.minFaceSize = 0.1,
    this.mode = FaceDetectorMode.fast,
  })  : assert(minFaceSize >= 0.0),
        assert(minFaceSize <= 1.0);

  /// Whether to run additional classifiers for characterizing attributes.
  ///
  /// E.g. "smiling" and "eyes open".
  final bool enableClassification;

  /// Whether to detect [FaceLandmark]s.
  final bool enableLandmarks;

  /// Whether to detect [FaceContour]s.
  final bool enableContours;

  /// Whether to enable face tracking.
  ///
  /// If enabled, the detector will maintain a consistent ID for each face when
  /// processing consecutive frames.
  final bool enableTracking;

  /// The smallest desired face size.
  ///
  /// Expressed as a proportion of the width of the head to the image width.
  ///
  /// Must be a value between 0.0 and 1.0.
  final double minFaceSize;

  /// Option for controlling additional accuracy / speed trade-offs.
  final FaceDetectorMode mode;
}

/// Represents a face detected by [FaceDetector].
class Face {
  Face._(dynamic data)
      : boundingBox = Rect.fromLTWH(
          data['left'],
          data['top'],
          data['width'],
          data['height'],
        ),
        headEulerAngleY = data['headEulerAngleY'],
        headEulerAngleZ = data['headEulerAngleZ'],
        leftEyeOpenProbability = data['leftEyeOpenProbability'],
        rightEyeOpenProbability = data['rightEyeOpenProbability'],
        smilingProbability = data['smilingProbability'],
        trackingId = data['trackingId'],
        _landmarks = Map<FaceLandmarkType, FaceLandmark>.fromIterables(
            FaceLandmarkType.values,
            FaceLandmarkType.values.map((FaceLandmarkType type) {
          final List<dynamic> pos = data['landmarks'][_enumToString(type)];
          return (pos == null)
              ? null
              : FaceLandmark._(
                  type,
                  Offset(pos[0], pos[1]),
                );
        })),
        _contours = Map<FaceContourType, FaceContour>.fromIterables(
            FaceContourType.values,
            FaceContourType.values.map((FaceContourType type) {
          /// added empty map to pass the tests
          final List<dynamic> arr =
              (data['contours'] ?? <String, dynamic>{})[_enumToString(type)];
          return (arr == null)
              ? null
              : FaceContour._(
                  type,
                  arr
                      .map<Offset>((dynamic pos) => Offset(pos[0], pos[1]))
                      .toList(),
                );
        }));

  final Map<FaceLandmarkType, FaceLandmark> _landmarks;
  final Map<FaceContourType, FaceContour> _contours;

  /// The axis-aligned bounding rectangle of the detected face.
  ///
  /// The point (0, 0) is defined as the upper-left corner of the image.
  final Rect boundingBox;

  /// The rotation of the face about the vertical axis of the image.
  ///
  /// Represented in degrees.
  ///
  /// A face with a positive Euler Y angle is turned to the camera's right and
  /// to its left.
  ///
  /// The Euler Y angle is guaranteed only when using the "accurate" mode
  /// setting of the face detector (as opposed to the "fast" mode setting, which
  /// takes some shortcuts to make detection faster).
  final double headEulerAngleY;

  /// The rotation of the face about the axis pointing out of the image.
  ///
  /// Represented in degrees.
  ///
  /// A face with a positive Euler Z angle is rotated counter-clockwise relative
  /// to the camera.
  ///
  /// ML Kit always reports the Euler Z angle of a detected face.
  final double headEulerAngleZ;

  /// Probability that the face's left eye is open.
  ///
  /// A value between 0.0 and 1.0 inclusive, or null if probability was not
  /// computed.
  final double leftEyeOpenProbability;

  /// Probability that the face's right eye is open.
  ///
  /// A value between 0.0 and 1.0 inclusive, or null if probability was not
  /// computed.
  final double rightEyeOpenProbability;

  /// Probability that the face is smiling.
  ///
  /// A value between 0.0 and 1.0 inclusive, or null if probability was not
  /// computed.
  final double smilingProbability;

  /// The tracking ID if the tracking is enabled.
  ///
  /// Null if tracking was not enabled.
  final int trackingId;

  /// Gets the landmark based on the provided [FaceLandmarkType].
  ///
  /// Null if landmark was not detected.
  FaceLandmark getLandmark(FaceLandmarkType landmark) => _landmarks[landmark];

  /// Gets the contour based on the provided [FaceContourType].
  ///
  /// Null if contour was not detected.
  FaceContour getContour(FaceContourType contour) => _contours[contour];
}

/// Represent a face landmark.
///
/// A landmark is a point on a detected face, such as an eye, nose, or mouth.
class FaceLandmark {
  FaceLandmark._(this.type, this.position);

  /// The [FaceLandmarkType] of this landmark.
  final FaceLandmarkType type;

  /// Gets a 2D point for landmark position.
  ///
  /// The point (0, 0) is defined as the upper-left corner of the image.
  final Offset position;
}

/// Represent a face contour.
///
/// Contours of facial features.
class FaceContour {
  FaceContour._(this.type, this.positionsList);

  /// The [FaceContourType] of this contour.
  final FaceContourType type;

  /// Gets a 2D point [List] for contour positions.
  ///
  /// The point (0, 0) is defined as the upper-left corner of the image.
  final List<Offset> positionsList;
}
