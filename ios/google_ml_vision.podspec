#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#

require 'yaml'
pubspec = YAML.load_file(File.join('..', 'pubspec.yaml'))
libraryVersion = pubspec['version'].gsub('+', '-')

Pod::Spec.new do |s|
  s.name             = 'google_ml_vision'
  s.version          = libraryVersion
  s.summary          = 'Flutter plugin for Google ML Kit'
  s.description      = <<-DESC
Plugin for Google ML Kit
                       DESC
  s.homepage         = 'https://github.com/brianmtully/flutter_google_ml_vision'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Brian M Tully' => 'btully1@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.platform = :ios, '11.0'
  s.dependency 'GoogleMLKit/BarcodeScanning', '~> 2.2.0'
  s.dependency 'GoogleMLKit/FaceDetection', '~> 2.2.0'
  s.dependency 'GoogleMLKit/ImageLabeling', '~> 2.2.0'
  s.dependency 'GoogleMLKit/TextRecognition', '~> 2.2.0'
  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  # Mobile vision doesn't support 32 bit ios
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphoneos*]' => 'arm64' }
  s.static_framework = true
  s.pod_target_xcconfig = { 'GCC_PREPROCESSOR_DEFINITIONS' => "LIBRARY_VERSION=\\@\\\"#{libraryVersion}\\\" LIBRARY_NAME=\\@\\\"google--ml-vis\\\"" }
end
