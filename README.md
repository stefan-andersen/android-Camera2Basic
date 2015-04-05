# periodic-camera-android

## About

This is an extension to the Camera2Basic demo project which enables the user to take sequences of pictures with a configurable interval. This feature may be quite useful for eg. atmospheric balloons, time-lapse or security applications.  

## How to use

The Camera2UtilFragment class is basically the same as the Camera2BasicFragment class (the one that comes with the original demo project). But since most of the core methods of the Camera2BasicFragment class are private, it's not really easy to extend. This is why the Camera2UtilFragment class was assembled. It hides all the complicated Camera 2.0 API logic and provides protected access to the @takePicture method and the @mFile member, which is basically enough to take a simple picture. 

For developers: If all you want is taking a picture without having to deal with the Camera 2.0 API, the Camera2UtilFragment class is what you are looking for. 

## Compatibility

I tested the extension on a Asus Nexus 7 device. 
Camera APIs can be quite a hazzle, so be sure to run the Unit-Tests - which can be found in "/com/example/android/camera2basic/tests/PeriodTests.java" - on any device you plan to use.