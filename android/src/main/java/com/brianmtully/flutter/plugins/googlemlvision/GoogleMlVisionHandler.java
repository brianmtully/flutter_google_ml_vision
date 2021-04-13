package com.brianmtully.flutter.plugins.googlemlvision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import com.google.mlkit.vision.common.InputImage;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import android.util.Log;

class MlVisionHandler implements MethodCallHandler {
  private final SparseArray<Detector> detectors = new SparseArray<>();
  private final Context applicationContext;

  MlVisionHandler(Context applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "BarcodeDetector#detectInImage":
      case "FaceDetector#processImage":
      case "ImageLabeler#processImage":
      case "TextRecognizer#processImage":
        handleDetection(call, result);
        break;
      case "BarcodeDetector#close":
      case "FaceDetector#close":
      case "ImageLabeler#close":
      case "TextRecognizer#close":
        closeDetector(call, result);
        break;
      default:
        result.notImplemented();
    }
  }

  private void handleDetection(MethodCall call, MethodChannel.Result result) {
    Map<String, Object> options = call.argument("options");

    InputImage image;
    Map<String, Object> imageData = call.arguments();
    try {
      image = dataToVisionImage(imageData);
    } catch (IOException exception) {
      result.error("MLVisionDetectorIOError", exception.getLocalizedMessage(), null);
      return;
    }
    Detector detector = getDetector(call);
    if (detector == null) {
      switch (call.method.split("#")[0]) {
        case "BarcodeDetector":
          detector = new GMLKBarcodeDetector(options);
          break;
        case "FaceDetector":
          detector = new GMLKFaceDetector(options);
          break;
        case "ImageLabeler":
          detector = new GMLKImageLabeler(options);
          break;
        case "TextRecognizer":
          detector = new GMLKTextRecognizer(options);
          break;
      }

      final Integer handle = call.argument("handle");
      addDetector(handle, detector);
    }

    detector.handleDetection(image, result);
  }

  private void closeDetector(final MethodCall call, final MethodChannel.Result result) {
    final Detector detector = getDetector(call);

    if (detector == null) {
      final Integer handle = call.argument("handle");
      final String message = String.format("Object for handle does not exists: %s", handle);
      throw new IllegalArgumentException(message);
    }

    try {
      detector.close();
      result.success(null);
    } catch (IOException e) {
      final String code = String.format("%sIOError", detector.getClass().getSimpleName());
      result.error(code, e.getLocalizedMessage(), null);
    } finally {
      final Integer handle = call.argument("handle");
      detectors.remove(handle);
    }
  }

  private InputImage dataToVisionImage(Map<String, Object> imageData) throws IOException {
    String imageType = (String) imageData.get("type");
    assert imageType != null;

    switch (imageType) {
      case "file":
        final String imageFilePath = (String) imageData.get("path");
        final int rotation = getImageExifOrientation(imageFilePath);

        if (rotation == 0) {
          File file = new File(imageFilePath);
          return InputImage.fromFilePath(this.applicationContext, Uri.fromFile(file));
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        final Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        final Bitmap rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return InputImage.fromBitmap(rotatedBitmap, 0);
      case "bytes":
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) imageData.get("metadata");


        byte[] bytes = (byte[]) imageData.get("bytes");
        assert bytes != null;
        Double width = (Double)metadata.get("width");
        int intWidth = width.intValue();

        Double height = (Double)metadata.get("height");
        int intHeight = height.intValue();
        try {
          InputImage inputImage = InputImage.fromByteArray(bytes,intWidth,intHeight,(int)metadata.get("rotation"), 17); //842094169
          return inputImage;
        } catch(IllegalArgumentException exception) {
          Log.e("GoogleMLVision ", "exception:", exception);
          return null;
        }
      default:
        throw new IllegalArgumentException(String.format("No image type for: %s", imageType));
    }
  }

  private int getImageExifOrientation(String imageFilePath) throws IOException {
    ExifInterface exif = new ExifInterface(imageFilePath);
    int orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        return 90;
      case ExifInterface.ORIENTATION_ROTATE_180:
        return 180;
      case ExifInterface.ORIENTATION_ROTATE_270:
        return 270;
      default:
        return 0;
    }
  }

  private void addDetector(final int handle, final Detector detector) {
    if (detectors.get(handle) != null) {
      final String message = String.format("Object for handle already exists: %s", handle);
      throw new IllegalArgumentException(message);
    }
    detectors.put(handle, detector);
  }

  private Detector getDetector(final MethodCall call) {
    final Integer handle = call.argument("handle");
    return detectors.get(handle);
  }
}
