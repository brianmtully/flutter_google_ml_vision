// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// @dart=2.9

part of google_ml_vision;

/// Used for finding [ImageLabel]s in a supplied image.
///
/// When you use the API, you get a list of the entities that were recognized:
/// people, things, places, activities, and so on. Each label found comes with a
/// score that indicates the confidence the ML model has in its relevance. With
/// this information, you can perform tasks such as automatic metadata
/// generation and content moderation.
///
/// A image labeler is created via
/// `imageLabeler([ImageLabelerOptions options])`:
///
/// ```dart
/// final GoogleVisionImage image =
///     GoogleVisionImage.fromFilePath('path/to/file');
///
/// final ImageLabeler imageLabeler =
///     GoogleVision.instance.imageLabeler(options);
///
/// final List<ImageLabel> labels = await imageLabeler.processImage(image);
/// ```
class ImageLabeler {
  ImageLabeler._({
    @required ImageLabelerOptions options,
    @required int handle,
  })  : _options = options,
        _handle = handle,
        assert(options != null);

  final ImageLabelerOptions _options;
  final int _handle;
  bool _hasBeenOpened = false;
  bool _isClosed = false;

  /// Finds entities in the input image.
  Future<List<ImageLabel>> processImage(GoogleVisionImage visionImage) async {
    assert(!_isClosed);

    _hasBeenOpened = true;
    final List<dynamic> reply =
        await GoogleVision.channel.invokeListMethod<dynamic>(
      'ImageLabeler#processImage',
      <String, dynamic>{
        'handle': _handle,
        'options': <String, dynamic>{
          'confidenceThreshold': _options.confidenceThreshold,
        },
      }..addAll(visionImage._serialize()),
    );

    final List<ImageLabel> labels = <ImageLabel>[];
    for (final dynamic data in reply) {
      labels.add(ImageLabel._(data));
    }

    return labels;
  }

  /// Release resources used by this labeler.
  Future<void> close() {
    if (!_hasBeenOpened) _isClosed = true;
    if (_isClosed) return Future<void>.value();

    _isClosed = true;
    return GoogleVision.channel.invokeMethod<void>(
      'ImageLabeler#close',
      <String, dynamic>{'handle': _handle},
    );
  }
}

/// Options for on device image labeler.
///
/// Confidence threshold could be provided for the label detection. For example,
/// if the confidence threshold is set to 0.7, only labels with
/// confidence >= 0.7 would be returned. The default threshold is 0.5.
class ImageLabelerOptions {
  /// Constructor for [ImageLabelerOptions].
  ///
  /// Confidence threshold could be provided for the label detection.
  /// For example, if the confidence threshold is set to 0.7, only labels with
  /// confidence >= 0.7 would be returned. The default threshold is 0.5.
  const ImageLabelerOptions({this.confidenceThreshold = 0.5})
      : assert(confidenceThreshold >= 0.0),
        assert(confidenceThreshold <= 1.0);

  /// The minimum confidence threshold of labels to be detected.
  ///
  /// Required to be in range [0.0, 1.0].
  final double confidenceThreshold;
}

/// Represents an entity label detected by [ImageLabeler].
class ImageLabel {
  ImageLabel._(dynamic data)
      : confidence = data['confidence']?.toDouble(),
        entityId = data['entityId'],
        text = data['text'];

  /// The overall confidence of the result. Range [0.0, 1.0].
  final double confidence;

  /// The opaque entity ID.
  ///
  /// IDs are available in Google Knowledge Graph Search API
  /// https://developers.google.com/knowledge-graph/
  final String entityId;

  /// A detected label from the given image.
  ///
  /// The label returned here is in English only. The end developer should use
  /// [entityId] to retrieve unique id.
  final String text;
}
