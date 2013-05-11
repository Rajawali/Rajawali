Issue Submission Guidelines
---------------------------

While the Rajawali collaborators are very happy to assist and provide support for the library, they are all doing this voluntarily on their personal time and are not compensated whatsoever for their efforts. So before you post, please read the guidelines below.
* When submitting a support issue about a question, please make sure it is related to the Rajawali library. Below are some common support questions we get that has nothing to do with Rajawali and should be asked elsewhere such as [Stack Overflow](http://www.stackoverflow.com/):
  * How to use SharedPreferences and preference listeners in your application
  * How to capture touch events and detect gestures
  * How to use broadcast receivers to receive system events
  * Anything about custom GLSL shaders themselves (submitting support issue on getting them hooked up to Rajawali is fine as long as you tried the tutorial [here](https://github.com/MasDennis/Rajawali/wiki/Tutorial-09-Creating-a-Custom-Material---GLSL-Shader) first)
* Search the Issues list before you submit an issue. If you are new to the Rajawali framework, chances are, somebody asked a similar question before you did. We often notice that same questions that have been answered before get asked over and over again. Below are the most common answered questions that pop back up:
  * [How to import Rajawali library into your project](https://github.com/MasDennis/Rajawali/wiki/Importing-Rajawali-and-RajawaliExamples)
  * [How to use object parsers to import exported 3D objects into your scene](https://github.com/MasDennis/Rajawali/wiki/Tutorial-02-Creating-a-Live-Wallpaper-and-Importing-a-Model)
* When submitting an issue, please be as constructive as possible. Non-constructive questions such as merely asking for more examples will be viewed as asking us to do your work for you. These issues will be closed immediately by our collaborators.
* If you think there is a bug in the library, please provide exact details to reproduce the bug and stack traces (in case of runtime exceptions) when submitting an issue.
* Please use Markdown to format your issue and to properly format your code. This ensures better readability for others which helps to receive better support. Hard-to-understand submission resulting from poorly formatted post will result in immediate closure by our collaborators.
* Rajawali is documented and supported in language that is more widely used internationally: English. Our collaborators are located all over the globe in America, Asia, and Europe and use English to collaborate and provide support. Please submit your issues written in comprehensive English so that our collaborators and others around the world may be able to easily read and understand what your issue is. Submissions written in any other languages or in very uncomprehensive English will be closed. If English is not your native tongue, please use a translator and we will do our best to accommodate.
* If there are no activity within the issue thread for 10 days, it will be deemed stale by our collaborators and will be closed. You may re-open the issue after 10 days if you need further help. When the issue is solved, please mention whether the issue was helpful (it would also be nice to thank whoever helped solve the issue) and close the issue.

Pull Request Guidelines
-----------------------

* Changes should be tested with https://github.com/MasDennis/RajawaliExamples
* Affected public classes and functions should have accurate Javadoc.
* Affected documentation, such as tutorials, should be clearly noted in the pull request description.
* Source code formatting should be hygienic:
  * No trailing or inconsistent whitespace.
  * No exceptionally long lines.
  * Consistent placement of brackets.
  * ...etc.
  * _Hint: Use Eclipse's auto-formatter on affected files (but please commit separately from functional changes)._
* Commit history should be relatively hygienic:
  * _Roughly_ one commit per logical change.
  * Log messages are clear and understandable.
  * Few (or no) "merge" commits.
  * _Hint: `git rebase -i` is great for cleaning up a branch._

Pull requests for branches that are still in development should be prefixed with `WIP:` so that they don't get accidently merged. Remove `WIP:` once the pull request is considered finalized and ready to be reviewed and merged.

License
-------

By contributing code to Rajawali, you are agreeing to release it under the [Apache License, Version 2.0](http://opensource.org/licenses/Apache-2.0).
