// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.brianmtully.flutter.plugins.googlemlvision;

import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import io.flutter.plugin.common.MethodChannel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

class GMLKTextRecognizer implements Detector {
  private final TextRecognizer recognizer;

  GMLKTextRecognizer(Map<String, Object> options) {
      recognizer = TextRecognition.getClient();
  }

  @Override
  public void handleDetection(final InputImage image, final MethodChannel.Result result) {
    recognizer
        .process(image)
        .addOnSuccessListener(
            new OnSuccessListener<Text>() {
              @Override
              public void onSuccess(Text googleVisionText) {
                Map<String, Object> visionTextData = new HashMap<>();
                visionTextData.put("text", googleVisionText.getText());

                List<Map<String, Object>> allBlockData = new ArrayList<>();
                for (Text.TextBlock block : googleVisionText.getTextBlocks()) {
                  Map<String, Object> blockData = new HashMap<>();
                  addData(
                      blockData,
                      block.getBoundingBox(),
                      block.getCornerPoints(),
                          block.getRecognizedLanguage(),
                      block.getText());

                  List<Map<String, Object>> allLineData = new ArrayList<>();
                  for (Text.Line line : block.getLines()) {
                    Map<String, Object> lineData = new HashMap<>();
                    addData(
                        lineData,
                        line.getBoundingBox(),
                        line.getCornerPoints(),
                        line.getRecognizedLanguage(),
                        line.getText());

                    List<Map<String, Object>> allElementData = new ArrayList<>();
                    for (Text.Element element : line.getElements()) {
                      Map<String, Object> elementData = new HashMap<>();
                      addData(
                          elementData,
                          element.getBoundingBox(),
                          element.getCornerPoints(),
                          element.getRecognizedLanguage(),
                          element.getText());

                      allElementData.add(elementData);
                    }
                    lineData.put("elements", allElementData);
                    allLineData.add(lineData);
                  }
                  blockData.put("lines", allLineData);
                  allBlockData.add(blockData);
                }

                visionTextData.put("blocks", allBlockData);
                result.success(visionTextData);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                result.error("textRecognizerError", exception.getLocalizedMessage(), null);
              }
            });
  }

  private void addData(
      Map<String, Object> addTo,
      Rect boundingBox,
      Point[] cornerPoints,
      String language,
      String text) {

    if (boundingBox != null) {
      addTo.put("left", (double) boundingBox.left);
      addTo.put("top", (double) boundingBox.top);
      addTo.put("width", (double) boundingBox.width());
      addTo.put("height", (double) boundingBox.height());
    }

    List<double[]> points = new ArrayList<>();
    if (cornerPoints != null) {
      for (Point point : cornerPoints) {
        points.add(new double[] {(double) point.x, (double) point.y});
      }
    }
    addTo.put("points", points);

    List<Map<String, Object>> allLanguageData = new ArrayList<>();
   // for (RecognizedLanguage language : languages) {
      Map<String, Object> languageData = new HashMap<>();
      languageData.put("languageCode", language);
      allLanguageData.add(languageData);
    //}
    addTo.put("recognizedLanguages", allLanguageData);
    addTo.put("text", text);
  }

  @Override
  public void close() throws IOException {
    recognizer.close();
  }
}
