# Initial Project and Dependency Setup

**_Credit Note:_** Most of this tutorial is taken from Rajawali user Clinton Medbery's tutorial which you can find [on his blog here](http://www.clintonmedbery.com/?p=59). We have altered content to suit our desires for our documentation pages, but all credit for the initial effort of laying out these steps and in particular the images, goes to him.

## Start the Project
If you are starting a new project from scratch, open Android Studio and click on **"New Project..."** in the File Menu. Call your project whatever you like, for example, **"RajawaliBasicProject"**. If you are adding Rajawali functionality to an existing project, open that project. We will continue as if you are starting a new project.

![](http://i0.wp.com/www.clintonmedbery.com/wp-content/uploads/2015/04/Screen-Shot-2015-04-06-at-11.56.15-AM.png)

Next we can just choose the default Android Version and choose **Blank Activity** as our activity. We can keep the name **MainActivity** as our activity name. Keep in mind that Rajawali requires at least API 15, and some functionality requires higher API levels.

## Get Rajawali From Maven and into your Android Studio Project

Next, we want to to get Rajawali into our project. We are going to do this using Maven. This should work for the latest version of Rajawali, but you might have to change out your version for another one. If you cannot find the version, [try looking in the RajawaliExamples gradle dependencies in Github](https://github.com/Rajawali/RajawaliExamples/blob/master/deps.gradle) or from where they host the [Maven package](https://oss.sonatype.org/#nexus-search;quick~rajawali).

In your top level **build.gradle (Project: RajawaliBasicProject)** file, we need to add a few lines.
```gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

Now we need to add a line to **build.gradle (Module: app)**.

```gradle
dependencies {
    // either stable
    implementation "org.rajawali3d:rajawali:1.2.1970"
    // or a snaphot
    implementation 'org.rajawali3d:rajawali:1.3.79-SNAPSHOT@aar'
}
```

which is the last release. If you want the latest build, you need to use a snapshot. Snapshots increment the build number each time and if you plan to use one, you will need to check for the latest. 
The build history can be seen [on Circleci CI](https://circleci.com/gh/Rajawali/Rajawali) and the snapshots [on Sonartype](https://oss.sonatype.org/content/repositories/snapshots/org/rajawali3d/rajawali/)
Note that only `master` branch builds are published as snapshots. Release builds will obviously only change with each release. To find the the latest, simply check the [README](https://github.com/Rajawali/Rajawali/blob/master/README.md).
