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
