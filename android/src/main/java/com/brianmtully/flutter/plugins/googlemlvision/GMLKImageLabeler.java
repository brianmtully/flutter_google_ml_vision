// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.brianmtully.flutter.plugins.googlemlvision;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import io.flutter.plugin.common.MethodChannel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

class GMLKImageLabeler implements Detector {
  private final ImageLabeler labeler;

  GMLKImageLabeler(Map<String, Object> options) {
      labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
  }

  @Override
  public void handleDetection(final InputImage image, final MethodChannel.Result result) {
    labeler
        .process(image)
        .addOnSuccessListener(
            new OnSuccessListener<List<ImageLabel>>() {
              @Override
              public void onSuccess(List<ImageLabel> visionLabels) {
                List<Map<String, Object>> labels = new ArrayList<>(visionLabels.size());
                for (ImageLabel label : visionLabels) {
                  Map<String, Object> labelData = new HashMap<>();
                  labelData.put("confidence", (double) label.getConfidence());
                  labelData.put("entityId", String.valueOf(label.getIndex()));
                  labelData.put("text", label.getText());

                  labels.add(labelData);
                }

                result.success(labels);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                result.error("imageLabelerError", e.getLocalizedMessage(), null);
              }
            });
  }

  @Override
  public void close() throws IOException {
    labeler.close();
  }
}
