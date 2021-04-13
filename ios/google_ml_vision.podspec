#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#

require 'yaml'
pubspec = YAML.load_file(File.join('..', 'pubspec.yaml'))
libraryVersion = pubspec['version'].gsub('+', '-')

Pod::Spec.new do |s|
  s.name             = 'google_ml_vision'
  s.version          = '0.0.1'
  s.summary          = 'Flutter plugin for Google ML Kit'
  s.description      = <<-DESC
An SDK that brings Google's machine learning expertise to Android and iOS apps in a powerful yet
 easy-to-use package.
                       DESC
  s.homepage         = 'https://www.brianmtully.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Flutter Team' => 'flutter-dev@googlegroups.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'GoogleMLKit/BarcodeScanning'
  s.dependency 'GoogleMLKit/FaceDetection'
  s.dependency 'GoogleMLKit/ImageLabeling'
  s.dependency 'GoogleMLKit/TextRecognition'
  s.ios.deployment_target = '10.0'
  s.static_framework = true

  s.pod_target_xcconfig = { 'GCC_PREPROCESSOR_DEFINITIONS' => "LIBRARY_VERSION=\\@\\\"#{libraryVersion}\\\" LIBRARY_NAME=\\@\\\"google--ml-vis\\\"" }
end
