// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// @dart=2.9

part of 'google_ml_vision.dart';

void imageLabelerTests() {
  group('$ImageLabeler', () {
    final ImageLabeler labeler = GoogleVision.instance.imageLabeler();

    test('processImage', () async {
      final String tmpFilename = await _loadImage('assets/test_barcode.jpg');
      final GoogleVisionImage visionImage =
          GoogleVisionImage.fromFilePath(tmpFilename);

      final List<ImageLabel> labels = await labeler.processImage(visionImage);

      expect(labels.length, greaterThan(0));
    });

    test('close', () {
      expect(labeler.close(), completes);
    });
  });
}
