![Rajawali](http://www.rozengain.com/files/rajawali-logo.jpg)

## About
[![AndroidLibs](https://img.shields.io/badge/AndroidLibs-Rajawali-brightgreen.svg?style=flat)](https://www.android-libs.com/lib/rajawali?utm_source=github-badge&utm_medium=github-badge&utm_campaign=github-badge)
[![License](https://img.shields.io/badge/license-Apache%202.0%20License-blue.svg)](https://github.com/Rajawali/Rajawali/blob/master/LICENSE.txt)
[![Maven Central](https://img.shields.io/maven-central/v/org.rajawali3d/rajawali.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22rajawali%22)
[![Trending](http://starveller.sigsev.io/api/repos/Rajawali/Rajawali/badge)](http://starveller.sigsev.io/Rajawali/Rajawali)

### Build Status
**Master Branch Status**  
[![master](https://travis-ci.org/Rajawali/Rajawali.svg?branch=master)](https://travis-ci.org/Rajawali/Rajawali)
[![codecov](https://codecov.io/gh/Rajawali/Rajawali/branch/master/graph/badge.svg)](https://codecov.io/gh/Rajawali/Rajawali)

**Development Branch Status**  
[![development](https://travis-ci.org/Rajawali/Rajawali.svg?branch=v2.0-development)](https://travis-ci.org/Rajawali/Rajawali)
[![codecov](https://codecov.io/gh/Rajawali/Rajawali/branch/v2.0-development/graph/badge.svg)](https://codecov.io/gh/Rajawali/Rajawali)

### News

**11/16/2016**
`Bombshell 1.1.777` has been released. It fixes a small number of bugs and adds a Scan Line post processing effect (thanks @contriteobserver). 

**11/15/2016**
One of the developers (@jwoolston) is now offering consulting/development services, particularly related to Rajawali. Details will be handled on a case by case basis but everything from paid assistance debugging to development of specific modifications or full apps using Rajawali are possible. To be clear, support on the Issues page is always free, however it is handled on a lower priority basis. Any development which happens under a paid contract is subject to whatever copyright terms the client specifies to the extent they are compatible with the licensing terms of Rajawali (see [LICENSE.txt](./LICENSE.txt))

**9/7/2016**
The official release of `Bombshell 1.1.610` is out. We will continue to support this release moving forward how ever bug fixes will be minimal. The decision was made that the design of the core engine was inhibiting correcting some of the larger issues. To this end, we have begun development of a `2.0` version - see issue [1755](https://github.com/Rajawali/Rajawali/issues/1755) for details. Development is happening in the `v2.0-development` branch [here](https://github.com/Rajawali/Rajawali/tree/v2.0-development)

**8/29/2016**
An initial effort for adding unit testing to Rajawali has been made. This initial focus has been on the core math classes and will ideally expand as bug fixes progress. Pull requests for unit tests are welcome and any "bug fix" PRs which include unit tests  or issues which include unit tests to demonstrate a failure will be given the highest priority. 

### General

Rajawali is a 3D engine for Android based on OpenGL ES 2.0/3.0. It can be used for normal apps as well as live wallpapers.

Want to keep the developers going? Buy them a beer! (http://www.pledgie.com/campaigns/21807)

[Join the Rajawali Community](https://plus.google.com/u/0/communities/116529974266844528013) on [![Rajawali Community on Google Plus](http://sinceresocial.com/wp-content/uploads/2012/05/google+-20px.png)](https://plus.google.com/u/0/communities/116529974266844528013) to stay up-to-date on the latest news. 

The [RajawaliExamples](https://github.com/MasDennis/RajawaliExamples) project is an ever growing toolkit for developing 3D content. [Check it out!](https://github.com/MasDennis/RajawaliExamples)

## Made With Rajawali

Numerous apps and live wallpapers have been made with Rajawali. [Check them out!](https://plus.google.com/u/0/communities/116529974266844528013/stream/526227da-cf2d-46f9-8ad6-beaca7b8ddd5)https://youtu.be/ch0v4mNhHoc

## Rajawali Testing

Rajawali includes a number of unit, integration and GL specific integration tests. The unit and integration tests run on each build via Android Emulator. The GL integration tests require a physical device and cannot currently be run as part of our Travis CI build. A few of the developers have their own small device labs and we run the GL integration tests manually as part of our development and checking pull requests. At present, the device testing matrix is as follows:

|Device Name|Model Number|Android Version|API Level|CPU|GPU|GL Version|EGL Version|
|:----------|:----------:|:-------------:|:-------:|:---:|:---:|:--------:|:---------:|
|Samsung Galaxy Tab 2 7.0+|SGH-T869|4.0.4|15|ARMv7|Mali-400MP|2.0|1.4|
|Samsung Galaxy Nexus|Galaxy Nexus|4.3|18|ARMv7|PowerVR SGX 540|2.0|1.4|
|Sasung Galaxy S4 Play|GT-I9505G|4.4.4|19|ARMv7|Adreno 320|3.0|1.4|
|Sasung Galaxy S4 Play|GT-I9505G|5.0|21|ARMv7|Adreno 320|3.0|1.4|
|Samsung Nexus 10|Nexus 10|5.0.2|21|ARMv7|Mali-T604|3.1|1.4|
|Asus Nexus 7 (Gen 1)|Nexus 7|5.1.1|22|ARMv7|nVidia Tegra 3|2.0|1.4|
|LG Nexus 4|Nexus 4|5.1.1|22|ARMv7 (Krait 300)|Adreno 320|3.0|1.4|
|LG Nexus 5|Nexus 5|5.1.1|22|ARMv7 (Krait 400)|Adreno 330|3.0|1.4|
|OnePlus One|A0001|6.0.1|23|ARMv7 (Krait 400)|Adreno 330|3.0|1.4|
|Barns & Noble Nook 7" (2016)|BNTV450|6.0|23|ARM64v8|Mali-T720|3.1 + AEP|1.4|
|Samsung Galaxy Tab S2|SM-T710|6.0.1|23|ARMv7 (Samsung Exynos 5433)|Mali-T760|3.1 + AEP|1.4|
|HTC Nexus 9|Nexus 9|7.0|24|ARMv7|nVidia Tegra K1 Kepler DX1|3.1|1.4|
|Huawei Nexus 6P|Nexus 6P|7.1.1|25|ARM64v8|Adreno 430|3.1|1.4|
|Google Pixel XL|Pixel XL|7.1|25|ARM64v8|Adreno 530|3.2 (3.1 + AEP)|1.4|

As we acquire more devices, this list will grow. Of course, not every capability can be tested against every device, however the engine is written so that devices not supporting a capability should not crash, and the tests look to ensure this is the case.

## Using Rajawali

Using Rajawali is as simple as adding a single line to your gradle dependencies:

`compile 'org.rajawali3d:rajawali:x.x.x@aar` where x.x.x is the version number (and the last number is the build number). If you wish to use the `master` branch snapshot, append `-SNAPSHOT`. For example, to use release `1.0.325`, you would use:

`compile 'org.rajawali3d:rajawali:1.0.325@aar`

To use the `master` branch build 48 snapshot `1.0.48-SNAPSHOT`, you would use:

`compile 'org.rajawali3d:rajawali:1.0.48-SNAPSHOT@aar`

All commits to `master` and `development` branch are deployed as snapshots. All tags will be deployed as releases. To see the latest build number, see the [build history](https://travis-ci.org/Rajawali/Rajawali/builds) and be sure you choose a `master` branch build.

For the above to work you will need to make sure your repository list includes:

`mavenCentral()` for releases, and `maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }` for snapshots.

Alternatively, if you checkout Rajawali via GIT, you can run `gradle assembleRelease uploadArchives` (or simply add a Gralde launch config to Android Studio for the `assembleRelease` and `uploadArchives` tasks). This will deploy to your local maven ropository. Consuming apps should have `mavenLocal()` in their repository list and add `compile 'org.rajawali3d:rajawali:1.0.0-SNAPSHOT@aar` to their dependencies.

## Rajawali Examples On The Market

[Download the free app](https://market.android.com/details?id=com.monyetmabuk.rajawali.tutorials)

## Rajawali + Augmented Reality

[RajawaliVuforia GitHub](https://github.com/MasDennis/RajawaliVuforia)

[YouTube Video of RajawaliVuforia in action](http://www.youtube.com/watch?v=rjLa4K9Ffuo)

## Rajawali + Virtual Reality

[RajawaliVR GitHub](https://github.com/MasDennis/RajawaliVR)

[YouTube Video of RajawaliVR in action](https://www.youtube.com/watch?v=3L0l_jmkcBE&feature=youtu.be)

## Tutorials
1. [Basic Setup & Sphere (Maven)](http://www.clintonmedbery.com/?p=59)
~~1. [Basic Setup & a Sphere (Out of date)](https://github.com/MasDennis/Rajawali/wiki/Tutorial-01----Basic-Setup-&-a-Sphere)~~  
2. [Creating a Live Wallpaper and Importing a Model](https://github.com/MasDennis/Rajawali/wiki/Tutorial-02-Creating-a-Live-Wallpaper-and-Importing-a-Model)  
3. [Materials](https://github.com/MasDennis/Rajawali/wiki/Tutorial-03-Materials)  
4. [Optimization](https://github.com/MasDennis/Rajawali/wiki/Tutorial-04-Optimization)  
5. [Skybox] (https://github.com/MasDennis/Rajawali/wiki/Tutorial-05-Skybox)  
6. [Screenshots] (https://github.com/MasDennis/Rajawali/wiki/Tutorial-06-Screenshots)  
7. [Plugins] (https://github.com/MasDennis/Rajawali/wiki/Tutorial-05-Plugins)  
8. [User Interface Elements] (https://github.com/MasDennis/Rajawali/wiki/Tutorial-08-Adding-User-Interface-Elements)  
9. [Creating a Custom Material GLSL Shader](https://github.com/MasDennis/Rajawali/wiki/Tutorial-09-Creating-a-Custom-Material---GLSL-Shader)  
10. [2D Renderer](https://github.com/MasDennis/Rajawali/wiki/Tutorial-10-2D-Renderer)  
11. [Particles](https://github.com/MasDennis/Rajawali/wiki/Tutorial-11-Particles)  
12. [Object Picking](https://github.com/MasDennis/Rajawali/wiki/Tutorial-12-Object-Picking)  
13. [Animation Classes](https://github.com/MasDennis/Rajawali/wiki/Tutorial-13-Animation-Classes)  
14. [Bump Normal Mapping](https://github.com/MasDennis/Rajawali/wiki/Tutorial-14-Bump-Normal-Mapping)  
15. [MD2 Animation](https://github.com/MasDennis/Rajawali/wiki/Tutorial-15-MD2-Animation)  
16. [Collision Detection](https://github.com/MasDennis/Rajawali/wiki/Tutorial-16-Collision-Detection)  
17. [Importing .Obj Files](https://github.com/MasDennis/Rajawali/wiki/Tutorial-17-Importing-.Obj-Files)  
18. [Drawing Lines](https://github.com/MasDennis/Rajawali/wiki/Tutorial-18-Drawing-Lines)  
19. [Catmull Rom Splines](https://github.com/MasDennis/Rajawali/wiki/Tutorial-19-Catmull-Rom-Splines)  
20. [Animated Sprites](https://github.com/MasDennis/Rajawali/wiki/Tutorial-20-Animated-Sprites)  
21. [Fog](https://github.com/MasDennis/Rajawali/wiki/Tutorial-21-Fog)  
22. [More Optimisation](https://github.com/MasDennis/Rajawali/wiki/Tutorial-22-More-Optimisation)  
23. [Custom Vertex Shader](https://github.com/MasDennis/Rajawali/wiki/Tutorial-23-Custom-Vertex-Shader)  
24. [Using Geometry Data To Position And Rotate Objects](https://github.com/MasDennis/Rajawali/wiki/Tutorial-24-Using-Geometry-Data-To-Position-And-Rotate-Objects)  
25. [Video Material](https://github.com/MasDennis/Rajawali/wiki/Tutorial-25-Video-Material)  
26. [Orthographic Camera](https://github.com/MasDennis/Rajawali/wiki/Tutorial-26-Orthographic-Camera)
27. [Texture Compression](https://github.com/MasDennis/Rajawali/wiki/Tutorial-27-Texture-Compression)
28. [Transparent Textures](https://github.com/MasDennis/Rajawali/wiki/Tutorial-28-Transparent-Textures)
29. [Skeletal Animation](https://github.com/MasDennis/Rajawali/wiki/Tutorial-29-Skeletal-Animation)
30. [Creating a Day Dream](https://github.com/MasDennis/Rajawali/wiki/Tutorial-30-Creating-a-Day-Dream)
31. [Using RajawaliScene] (https://github.com/MasDennis/Rajawali/wiki/Tutorial-31-Using-RajawaliScene)

## Tutorials & Articles By Others
* (Kean Walmsley from Autodesk) Creating a 3D viewer for our Apollonian service using Android [Part 1](http://through-the-interface.typepad.com/through_the_interface/2012/04/creating-a-3d-viewer-for-our-apollonian-service-using-android-part-1.html) [Part 2](http://through-the-interface.typepad.com/through_the_interface/2012/05/creating-a-3d-viewer-for-our-apollonian-service-using-android-part-2.html) [Part 3](http://through-the-interface.typepad.com/through_the_interface/2012/05/creating-a-3d-viewer-for-our-apollonian-service-using-android-part-3.html)
* Rajawali と戯れる [Part 1](http://dev.classmethod.jp/smartphone/android/android-rajawali-tutorials-01/) [Part 2](http://dev.classmethod.jp/smartphone/android/android-rajawali-tutorials-02/) (Japanese)
* [Introducing Plugin Architecture for Rajawali](http://www.andrewjo.com/blog/mobile-development/introducing-plugin-architecture-for-rajawali)
* [Object Occlusion Testing in Rajawali](http://www.andrewjo.com/blog/mobile-development/object-occlusion-testing-in-rajawali)
* [Ниже пример как сделать простую 3D модель и запустить сцену на Android](http://konsultantspb.ru/3d-engine-rajawali/)(Russian)
* [Android in razvoj 3D igre](http://www.monitor.si/clanek/android-in-razvoj-3d-igre/142302/)(Slovenian)
* [Rajawali là gì?](http://www.trithucmoi.co/en/component/content/article/101-rajawali-va-ardor3d.html)(Vietnamese)

## Learn Rajawali at a Training Center
* [Android Game Development in India](http://virtualinfocom.com/android_game/android_game_development_institute.html)
* [Android Application Programming in India](http://virtualinfocom.com/android_game_application_development_training.html)

