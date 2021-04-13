// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.brianmtully.flutter.plugins.googlemlvision;

import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.common.InputImage;
import io.flutter.plugin.common.MethodChannel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GMLKBarcodeDetector implements Detector {
  private final BarcodeScanner detector;

  GMLKBarcodeDetector(Map<String, Object> options) {
    detector = BarcodeScanning.getClient(parseOptions(options));
  }

  @Override
  public void handleDetection(final InputImage image, final MethodChannel.Result result) {
    detector
        .process(image)
        .addOnSuccessListener(
            new OnSuccessListener<List<Barcode>>() {
              @Override
              public void onSuccess(List<Barcode> visionBarcodes) {
                List<Map<String, Object>> barcodes = new ArrayList<>();

                for (Barcode barcode : visionBarcodes) {
                  Map<String, Object> barcodeMap = new HashMap<>();

                  Rect bounds = barcode.getBoundingBox();
                  if (bounds != null) {
                    barcodeMap.put("left", (double) bounds.left);
                    barcodeMap.put("top", (double) bounds.top);
                    barcodeMap.put("width", (double) bounds.width());
                    barcodeMap.put("height", (double) bounds.height());
                  }

                  List<double[]> points = new ArrayList<>();
                  if (barcode.getCornerPoints() != null) {
                    for (Point point : barcode.getCornerPoints()) {
                      points.add(new double[] {(double) point.x, (double) point.y});
                    }
                  }
                  barcodeMap.put("points", points);

                  barcodeMap.put("rawValue", barcode.getRawValue());
                  barcodeMap.put("displayValue", barcode.getDisplayValue());
                  barcodeMap.put("format", barcode.getFormat());
                  barcodeMap.put("valueType", barcode.getValueType());

                  Map<String, Object> typeValue = new HashMap<>();
                  switch (barcode.getValueType()) {
                    case Barcode.TYPE_EMAIL:
                      Barcode.Email email = barcode.getEmail();

                      typeValue.put("type", email.getType());
                      typeValue.put("address", email.getAddress());
                      typeValue.put("body", email.getBody());
                      typeValue.put("subject", email.getSubject());

                      barcodeMap.put("email", typeValue);
                      break;
                    case Barcode.TYPE_PHONE:
                      Barcode.Phone phone = barcode.getPhone();

                      typeValue.put("number", phone.getNumber());
                      typeValue.put("type", phone.getType());

                      barcodeMap.put("phone", typeValue);
                      break;
                    case Barcode.TYPE_SMS:
                      Barcode.Sms sms = barcode.getSms();

                      typeValue.put("message", sms.getMessage());
                      typeValue.put("phoneNumber", sms.getPhoneNumber());

                      barcodeMap.put("sms", typeValue);
                      break;
                    case Barcode.TYPE_URL:
                      Barcode.UrlBookmark urlBookmark = barcode.getUrl();

                      typeValue.put("title", urlBookmark.getTitle());
                      typeValue.put("url", urlBookmark.getUrl());

                      barcodeMap.put("url", typeValue);
                      break;
                    case Barcode.TYPE_WIFI:
                      Barcode.WiFi wifi = barcode.getWifi();

                      typeValue.put("ssid", wifi.getSsid());
                      typeValue.put("password", wifi.getPassword());
                      typeValue.put("encryptionType", wifi.getEncryptionType());

                      barcodeMap.put("wifi", typeValue);
                      break;
                    case Barcode.TYPE_GEO:
                      Barcode.GeoPoint geoPoint = barcode.getGeoPoint();

                      typeValue.put("latitude", geoPoint.getLat());
                      typeValue.put("longitude", geoPoint.getLng());

                      barcodeMap.put("geoPoint", typeValue);
                      break;
                    case Barcode.TYPE_CONTACT_INFO:
                      Barcode.ContactInfo contactInfo = barcode.getContactInfo();

                      List<Map<String, Object>> addresses = new ArrayList<>();
                      for (Barcode.Address address : contactInfo.getAddresses()) {
                        Map<String, Object> addressMap = new HashMap<>();
                        if (address.getAddressLines() != null) {
                          addressMap.put("addressLines", Arrays.asList(address.getAddressLines()));
                        }
                        addressMap.put("type", address.getType());

                        addresses.add(addressMap);
                      }
                      typeValue.put("addresses", addresses);

                      List<Map<String, Object>> emails = new ArrayList<>();
                      for (Barcode.Email contactEmail : contactInfo.getEmails()) {
                        Map<String, Object> emailMap = new HashMap<>();
                        emailMap.put("address", contactEmail.getAddress());
                        emailMap.put("type", contactEmail.getType());
                        emailMap.put("body", contactEmail.getBody());
                        emailMap.put("subject", contactEmail.getSubject());

                        emails.add(emailMap);
                      }
                      typeValue.put("emails", emails);

                      Map<String, Object> nameMap = new HashMap<>();
                      Barcode.PersonName name = contactInfo.getName();
                      if (name != null) {
                        nameMap.put("formattedName", name.getFormattedName());
                        nameMap.put("first", name.getFirst());
                        nameMap.put("last", name.getLast());
                        nameMap.put("middle", name.getMiddle());
                        nameMap.put("prefix", name.getPrefix());
                        nameMap.put("pronunciation", name.getPronunciation());
                        nameMap.put("suffix", name.getSuffix());
                      }
                      typeValue.put("name", nameMap);

                      List<Map<String, Object>> phones = new ArrayList<>();
                      for (Barcode.Phone contactPhone : contactInfo.getPhones()) {
                        Map<String, Object> phoneMap = new HashMap<>();
                        phoneMap.put("number", contactPhone.getNumber());
                        phoneMap.put("type", contactPhone.getType());

                        phones.add(phoneMap);
                      }
                      typeValue.put("phones", phones);

                      if (contactInfo.getUrls() != null) {
                        typeValue.put("urls", Arrays.asList(contactInfo.getUrls()));
                      }
                      typeValue.put("jobTitle", contactInfo.getTitle());
                      typeValue.put("organization", contactInfo.getOrganization());

                      barcodeMap.put("contactInfo", typeValue);
                      break;
                    case Barcode.TYPE_CALENDAR_EVENT:
                      Barcode.CalendarEvent calendarEvent =
                          barcode.getCalendarEvent();

                      typeValue.put("eventDescription", calendarEvent.getDescription());
                      typeValue.put("location", calendarEvent.getLocation());
                      typeValue.put("organizer", calendarEvent.getOrganizer());
                      typeValue.put("status", calendarEvent.getStatus());
                      typeValue.put("summary", calendarEvent.getSummary());
                      if (calendarEvent.getStart() != null) {
                        typeValue.put("start", calendarEvent.getStart().getRawValue());
                      }
                      if (calendarEvent.getEnd() != null) {
                        typeValue.put("end", calendarEvent.getEnd().getRawValue());
                      }

                      barcodeMap.put("calendarEvent", typeValue);
                      break;
                    case Barcode.TYPE_DRIVER_LICENSE:
                      Barcode.DriverLicense driverLicense =
                          barcode.getDriverLicense();

                      typeValue.put("firstName", driverLicense.getFirstName());
                      typeValue.put("middleName", driverLicense.getMiddleName());
                      typeValue.put("lastName", driverLicense.getLastName());
                      typeValue.put("gender", driverLicense.getGender());
                      typeValue.put("addressCity", driverLicense.getAddressCity());
                      typeValue.put("addressStreet", driverLicense.getAddressStreet());
                      typeValue.put("addressState", driverLicense.getAddressState());
                      typeValue.put("addressZip", driverLicense.getAddressZip());
                      typeValue.put("birthDate", driverLicense.getBirthDate());
                      typeValue.put("documentType", driverLicense.getDocumentType());
                      typeValue.put("licenseNumber", driverLicense.getLicenseNumber());
                      typeValue.put("expiryDate", driverLicense.getExpiryDate());
                      typeValue.put("issuingDate", driverLicense.getIssueDate());
                      typeValue.put("issuingCountry", driverLicense.getIssuingCountry());

                      barcodeMap.put("driverLicense", typeValue);
                      break;
                  }

                  barcodes.add(barcodeMap);
                }
                result.success(barcodes);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                result.error("barcodeDetectorError", exception.getLocalizedMessage(), null);
              }
            });
  }

  private BarcodeScannerOptions parseOptions(Map<String, Object> optionsData) {
    Integer barcodeFormats = (Integer) optionsData.get("barcodeFormats");
    return new BarcodeScannerOptions.Builder()
        .setBarcodeFormats(barcodeFormats)
        .build();
  }

  @Override
  public void close() throws IOException {
    detector.close();
  }
}
