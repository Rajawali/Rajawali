# Initial Project and Dependency Setup

**_Credit Note:_** Most of this tutorial is taken from Rajawali user Clinton Medbery's tutorial which you can find [on his blog here](http://www.clintonmedbery.com/?p=59). We have altered content to suit our desires for our documentation pages, but all credit for the initial effort of laying out these steps and in particular the images, goes to him.

## Start the Project
If you are starting a new project from scratch, open Android Studio and click on **"New Project..."** in the File Menu. Call your project whatever you like, for example, **"RajawaliBasicProject"**. If you are adding Rajawali functionality to an existing project, open that project. We will continue as if you are starting a new project.

![](http://i0.wp.com/www.clintonmedbery.com/wp-content/uploads/2015/04/Screen-Shot-2015-04-06-at-11.56.15-AM.png)

Next we can just choose the default Android Version and choose **Blank Activity** as our activity. We can keep the name **MainActivity** as our activity name. Keep in mind that Rajawali requires at least API 15, and some functionality requires higher API levels.

## Get Rajawali From Maven and into your Android Studio Project

Next, we want to to get Rajawali into our project. We are going to do this using Maven. This should work for the latest version of Rajawali, but you might have to change out your version for another one. If you cannot find the version, [try looking in the RajawaliExamples gradle dependencies in Github](https://github.com/Rajawali/RajawaliExamples/blob/master/deps.gradle) or from where they host the [Maven package](https://oss.sonatype.org/#nexus-search;quick~rajawali).

In your **build.gradle (Project: RajawaliBasicProject)** file, we need to add a few lines.
```gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.1.0'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        // This is only necessary if you want to use snapshots
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        jcenter()
    }
}
```

Notice the addition of  `mavenCentral()` and `maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }`. This will allow Android Studio to connect to this Maven Server to retrieve a library. If you plan on doing local development on the Rajawali library itself and want your changes to be picked up without them being in the main repository, you will also need to add `mavenLocal()`. Gradle will check the repositories in the order listed, so make sure you take this into account.

Now we need to add a line to **build.gradle (Module: app)**.

```gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:22.0.0'
    compile 'org.rajawali3d:rajawali:1.1.337@aar'
}
```

This is where the note from earlier about retrieving the build version might come in handy. We added the line `compile 'org.rajawali3d:rajawali:1.1.337@aar` which is the last release. If you want the latest build, you need to use a snapshot. Snapshots increment the build number each time and if you plan to use one, you will need to check for the latest. The build history can be seen [on Travis CI](https://travis-ci.org/Rajawali/Rajawali/builds). Note that only `master` branch builds are published as snapshots. Release builds will obviously only change with each release. To find the the latest, simply check the [README](https://github.com/Rajawali/Rajawali/blob/master/README.md).

Now we should see a note at the top asking you the sync the gradle. Click Sync Now and it should build. If it does not, you might want to recheck your code.
