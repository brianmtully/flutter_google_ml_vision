// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// @dart=2.9

part of 'google_ml_vision.dart';

void textRecognizerTests() {
  GoogleVisionImage visionImage;

  setUp(() async {
    final tmpFilename = await _loadImage('assets/test_text.png');
    visionImage = GoogleVisionImage.fromFilePath(tmpFilename);
  });

  group('$TextRecognizer', () {
    final recognizer = GoogleVision.instance.textRecognizer();

    test('processImage', () async {
      final text = await recognizer.processImage(visionImage);

      expect(text.text, 'TEXT');
    });

    test('close', () {
      expect(recognizer.close(), completes);
    });
  });
}
