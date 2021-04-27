// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// @dart=2.9

part of google_ml_vision;

/// Detector for performing optical character recognition(OCR) on an input image.
///
/// A text recognizer is created via `textRecognizer()` in [GoogleVision]:
///
/// ```dart
/// final GoogleVisionImage image =
///     GoogleVisionImage.fromFilePath('path/to/file');
///
/// final TextRecognizer textRecognizer =
///     GoogleVision.instance.textRecognizer();
///
/// final List<VisionText> recognizedText =
///     await textRecognizer.processImage(image);
/// ```
class TextRecognizer {
  TextRecognizer._({
    @required int handle,
  }) : _handle = handle;

  final int _handle;

  bool _hasBeenOpened = false;
  bool _isClosed = false;

  /// Detects [VisionText] from a [GoogleVisionImage].
  Future<VisionText> processImage(GoogleVisionImage visionImage) async {
    assert(!_isClosed);
    assert(visionImage != null);

    _hasBeenOpened = true;

    final Map<String, dynamic> reply =
        await GoogleVision.channel.invokeMapMethod<String, dynamic>(
      'TextRecognizer#processImage',
      <String, dynamic>{
        'handle': _handle,
        'options': <String, dynamic>{},
      }..addAll(visionImage._serialize()),
    );

    return VisionText._(reply);
  }

  /// Releases resources used by this recognizer.
  Future<void> close() {
    if (!_hasBeenOpened) _isClosed = true;
    if (_isClosed) return Future<void>.value();

    _isClosed = true;
    return GoogleVision.channel.invokeMethod<void>(
      'TextRecognizer#close',
      <String, dynamic>{'handle': _handle},
    );
  }
}

/// Recognized text in an image.
class VisionText {
  VisionText._(Map<String, dynamic> data)
      : text = data['text'],
        blocks = List<TextBlock>.unmodifiable(data['blocks']
            .map<TextBlock>((dynamic block) => TextBlock._(block)));

  /// String representation of the recognized text.
  final String text;

  /// All recognized text broken down into individual blocks/paragraphs.
  final List<TextBlock> blocks;
}

/// Abstract class representing dimensions of recognized text in an image.
abstract class TextContainer {
  TextContainer._(Map<dynamic, dynamic> data)
      : boundingBox = data['left'] != null
            ? Rect.fromLTWH(
                data['left'],
                data['top'],
                data['width'],
                data['height'],
              )
            : null,
        cornerPoints = List<Offset>.unmodifiable(
            data['points'].map<Offset>((dynamic point) => Offset(
                  point[0],
                  point[1],
                ))),
        recognizedLanguages = List<RecognizedLanguage>.unmodifiable(
            data['recognizedLanguages'].map<RecognizedLanguage>(
                (dynamic language) => RecognizedLanguage._(language))),
        text = data['text'];

  /// Axis-aligned bounding rectangle of the detected text.
  ///
  /// The point (0, 0) is defined as the upper-left corner of the image.
  ///
  /// Could be null even if text is found.
  final Rect boundingBox;

  /// The four corner points in clockwise direction starting with top-left.
  ///
  /// Due to the possible perspective distortions, this is not necessarily a
  /// rectangle. Parts of the region could be outside of the image.
  ///
  /// Could be empty even if text is found.
  final List<Offset> cornerPoints;

  /// All detected languages from recognized text.
  ///
  /// On-device text recognizers only detect Latin-based languages.
  /// If no languages are recognized, the list is empty.
  final List<RecognizedLanguage> recognizedLanguages;

  /// The recognized text as a string.
  ///
  /// Returned in reading order for the language. For Latin, this is top to
  /// bottom within a Block, and left-to-right within a Line.
  final String text;
}

/// A block of text (think of it as a paragraph) as deemed by the OCR engine.
class TextBlock extends TextContainer {
  TextBlock._(Map<dynamic, dynamic> block)
      : lines = List<TextLine>.unmodifiable(
            block['lines'].map<TextLine>((dynamic line) => TextLine._(line))),
        super._(block);

  /// The contents of the text block, broken down into individual lines.
  final List<TextLine> lines;
}

/// Represents a line of text.
class TextLine extends TextContainer {
  TextLine._(Map<dynamic, dynamic> line)
      : elements = List<TextElement>.unmodifiable(line['elements']
            .map<TextElement>((dynamic element) => TextElement._(element))),
        super._(line);

  /// The contents of this line, broken down into individual elements.
  final List<TextElement> elements;
}

/// Roughly equivalent to a space-separated "word."
///
/// The API separates elements into words in most Latin languages, but could
/// separate by characters in others.
///
/// If a word is split between two lines by a hyphen, each part is encoded as a
/// separate element.
class TextElement extends TextContainer {
  TextElement._(Map<dynamic, dynamic> element) : super._(element);
}
