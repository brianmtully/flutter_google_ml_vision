// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.brianmtully.flutter.plugins.googlemlvision;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import io.flutter.plugin.common.MethodChannel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GMLKFaceDetector implements Detector {
  private final FaceDetector detector;

  GMLKFaceDetector(Map<String, Object> options) {
    detector = FaceDetection.getClient(parseOptions(options));
  }

  @Override
  public void handleDetection(final InputImage image, final MethodChannel.Result result) {
    detector
        .process(image)
        .addOnSuccessListener(
            new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> foundFaces) {
                List<Map<String, Object>> faces = new ArrayList<>(foundFaces.size());
                for (Face face : foundFaces) {
                  Map<String, Object> faceData = new HashMap<>();

                  faceData.put("left", (double) face.getBoundingBox().left);
                  faceData.put("top", (double) face.getBoundingBox().top);
                  faceData.put("width", (double) face.getBoundingBox().width());
                  faceData.put("height", (double) face.getBoundingBox().height());

                  faceData.put("headEulerAngleY", face.getHeadEulerAngleY());
                  faceData.put("headEulerAngleZ", face.getHeadEulerAngleZ());
                  if (face.getSmilingProbability() != null) {
                    faceData.put("smilingProbability", face.getSmilingProbability());
                  }

                  if (face.getLeftEyeOpenProbability()
                      != null) {
                    faceData.put("leftEyeOpenProbability", face.getLeftEyeOpenProbability());
                  }

                  if (face.getRightEyeOpenProbability()
                      != null) {
                    faceData.put("rightEyeOpenProbability", face.getRightEyeOpenProbability());
                  }

                  if (face.getTrackingId() != null) {
                    faceData.put("trackingId", face.getTrackingId());
                  }

                  faceData.put("landmarks", getLandmarkData(face));

                  faceData.put("contours", getContourData(face));

                  faces.add(faceData);
                }

                result.success(faces);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                result.error("faceDetectorError", exception.getLocalizedMessage(), null);
              }
            });
  }

  private Map<String, double[]> getLandmarkData(Face face) {
    Map<String, double[]> landmarks = new HashMap<>();

    landmarks.put("bottomMouth", landmarkPosition(face, FaceLandmark.MOUTH_BOTTOM));
    landmarks.put("leftCheek", landmarkPosition(face, FaceLandmark.LEFT_CHEEK));
    landmarks.put("leftEar", landmarkPosition(face, FaceLandmark.LEFT_EAR));
    landmarks.put("leftEye", landmarkPosition(face, FaceLandmark.LEFT_EYE));
    landmarks.put("leftMouth", landmarkPosition(face, FaceLandmark.MOUTH_LEFT));
    landmarks.put("noseBase", landmarkPosition(face, FaceLandmark.NOSE_BASE));
    landmarks.put("rightCheek", landmarkPosition(face, FaceLandmark.RIGHT_CHEEK));
    landmarks.put("rightEar", landmarkPosition(face, FaceLandmark.RIGHT_EAR));
    landmarks.put("rightEye", landmarkPosition(face, FaceLandmark.RIGHT_EYE));
    landmarks.put("rightMouth", landmarkPosition(face, FaceLandmark.MOUTH_RIGHT));

    return landmarks;
  }

  private Map<String, List<double[]>> getContourData(Face face) {
    Map<String, List<double[]>> contours = new HashMap<>();

    contours.put("allPoints", allContourPoints(face));
    contours.put("face", contourPosition(face, FaceContour.FACE));
    contours.put("leftEye", contourPosition(face, FaceContour.LEFT_EYE));
    contours.put(
        "leftEyebrowBottom", contourPosition(face, FaceContour.LEFT_EYEBROW_BOTTOM));
    contours.put(
        "leftEyebrowTop", contourPosition(face, FaceContour.LEFT_EYEBROW_TOP));
    contours.put(
        "lowerLipBottom", contourPosition(face, FaceContour.LOWER_LIP_BOTTOM));
    contours.put("lowerLipTop", contourPosition(face, FaceContour.LOWER_LIP_TOP));
    contours.put("noseBottom", contourPosition(face, FaceContour.NOSE_BOTTOM));
    contours.put("noseBridge", contourPosition(face, FaceContour.NOSE_BRIDGE));
    contours.put("rightEye", contourPosition(face, FaceContour.RIGHT_EYE));
    contours.put(
        "rightEyebrowBottom",
        contourPosition(face, FaceContour.RIGHT_EYEBROW_BOTTOM));
    contours.put(
        "rightEyebrowTop", contourPosition(face, FaceContour.RIGHT_EYEBROW_TOP));
    contours.put(
        "upperLipBottom", contourPosition(face, FaceContour.UPPER_LIP_BOTTOM));
    contours.put("upperLipTop", contourPosition(face, FaceContour.UPPER_LIP_TOP));

    return contours;
  }

  private double[] landmarkPosition(Face face, int landmarkInt) {
    FaceLandmark landmark = face.getLandmark(landmarkInt);
    if (landmark != null) {

      return new double[] {landmark.getPosition().x, landmark.getPosition().y};
    }

    return null;
  }

  private List<double[]> contourPosition(Face face, int contourInt) {
    FaceContour contour = face.getContour(contourInt);
    if (contour != null) {
      List<PointF> contourPoints = contour.getPoints();
      List<double[]> result = new ArrayList<double[]>();

      for (int i = 0; i < contourPoints.size(); i++) {
        result.add(new double[] {contourPoints.get(i).x, contourPoints.get(i).y});
      }

      return result;
    }

    return null;
  }

  private List<double[]> allContourPoints(Face face) {
    List<FaceContour> contours = face.getAllContours();
    List<double[]> result = new ArrayList<double[]>();
    for (int i = 0; i < contours.size(); i++) {
      List<PointF> contourPoints = contours.get(i).getPoints();
      for (int j = 0; j < contourPoints.size(); j++) {
        result.add(new double[]{contourPoints.get(j).x, contourPoints.get(j).y});
      }

    }
    return result;
  }


  private FaceDetectorOptions parseOptions(Map<String, Object> options) {
    int classification =
        (boolean) options.get("enableClassification")
            ? FaceDetectorOptions.CLASSIFICATION_MODE_ALL
            : FaceDetectorOptions.CLASSIFICATION_MODE_NONE;

    int landmark =
        (boolean) options.get("enableLandmarks")
            ? FaceDetectorOptions.LANDMARK_MODE_ALL
            : FaceDetectorOptions.LANDMARK_MODE_NONE;

    int contours =
        (boolean) options.get("enableContours")
            ? FaceDetectorOptions.CONTOUR_MODE_ALL
            : FaceDetectorOptions.CONTOUR_MODE_NONE;

    int mode;
    switch ((String) options.get("mode")) {
      case "accurate":
        mode = FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
        break;
      case "fast":
        mode = FaceDetectorOptions.PERFORMANCE_MODE_FAST;
        break;
      default:
        throw new IllegalArgumentException("Not a mode:" + options.get("mode"));
    }

    FaceDetectorOptions.Builder builder =
        new FaceDetectorOptions.Builder()
            .setClassificationMode(classification)
            .setLandmarkMode(landmark)
            .setContourMode(contours)
            .setMinFaceSize((float) ((double) options.get("minFaceSize")))
            .setPerformanceMode(mode);

    if ((boolean) options.get("enableTracking")) {
      builder.enableTracking();
    }

    return builder.build();
  }

  @Override
  public void close() throws IOException {
    detector.close();
  }
}
