# Contributor's Guidelines for DanDanPlayForAndroid.

This is the contributor's guide for DanDanPlayForAndroid. In the event that this document was opened intentionally by you, it means you are keen on adding value to DanDanPlayForAndroid, and that is extremely stunning!.

DanDanPlayForAndroid is a completely opensource project. This means that our source code is available to anyone to use, modify, and redistribute within the confines of the [license](https://github.com/xyoye/DanDanPlayForAndroid/blob/master/LICENSE). 
Furthermore, being an open source project means it is available for collaborative development, and we are more than excited to have you contribute towards the advancement of this project any way you can.

We are always excited to receive contributions ranging from use-cases, documentation, code, patches, bug reports, feature requests & enhancements, etc. We do not restrict contributions to programmers, therefore, you do not need to be a programmer to share your ideas about the project. We consider every contribution valuable.
Feel free to post suggestions, bugs & crashes, comments on a code in the project, or just anything that isn't as smooth as it should be as an issue on Github. 
If you're feeling adventurous, you're more than welcome to fork the repository and submit pull requests either to implement a new feature, fix a bug, or clean up the code.

This document will entail the processes and guidelines for contributing to this project.

---


### Submitting Feature Requests

If you wish to make suggestions or feature requests, please check the issue tracker to ascertain if the feature hasn't already been requested before by another user. If the feature has already been requested, but it is a closed issue that hasn't been marked as "wontfix", feel free to reopen the issue or create yours. 
To request a new feature you should [open an issue](https://github.com/xyoye/DanDanPlayForAndroid/issues/new?template=feature_request.md).

In order to help the developer understand the feature request;

- Title of the issue should be explicit, giving insight into the content of the issue.
- The area of the project where the feature should be applied or implemented should be properly stated. Add screenshots of mockup if possible.
- It would be great if a detailed use case is included in your request.
-  Thought should be given to the long-term technical debt and maintenance that feature may require after inclusion.

When submitting a feature request, please make a single issue for each feature request (i.e. don't submit an issue that contains a list of features). Such issues are hard to keep track of and often get lost.

---


## Bug & Crash Reports

Did you encounter an error while using the app? Let the developer know about it by creating a new issue. 

Filing a great bug report helps the developer pinpoint the cause of the bug and effectively work on a fix.

### Steps on how to file a bug report.

Before filing a bug report,

- Ensure you're running the latest version of the software
- Confirm if it's actually a bug and not an error caused by a plugin on your system. Test with other systems to verify
- If the same issue persists after testing on other devices then it is indeed a bug. 
- Check the issue tracker if the bug hasn't been reported by other users. If it has been reported before it is likely to be in [opened issues](https://github.com/xyoye/DanDanPlayForAndroid/issues?q=is%3Aopen+is%3Aissue). Also, check [closed issues](https://github.com/xyoye/DanDanPlayForAndroid/issues?q=is%3Aissue+is%3Aclosed) too.

The most important aspect of a bug report is the details. The more concise the details, the easier it'll be for the developer or someone else to tackle the bug.

- Title of the issue should give the developer insight about what the report is all about. 
- A brief explanation of the behaviour you expected the software to perform.
- The actual behaviour of the software when you experienced the bug.
- Steps to reproduce the bug coupled with screenshots and videos if possible.
- Test environment: which is details of the Device, Operating system, Software version.

---

## Code Contribtuion

Do you have ideas of some new cool functionalities, a bug fix or other code you wish to contribute? This is the perfect section to guide you on that path.

The codebase is maintained using the “contributor workflow”, where everyone without exception, contributes patch proposals using “pull requests”. This facilitates social contribution, easy testing, and peer review.

To contribute a patch, the workflow is as follows:

  - Fork repository
  - Create a topic branch
  - Commit patches

Commits should not be verbose but [atomic](https://en.wikipedia.org/wiki/Atomic_commit#Atomic_commit_convention). This allows easy review of code, easy bug identification and most importantly, easy to roll back changes if any other major issues are found.

Short and understandable commit messages are essential. This allows for easy identification of the changes made. Pull requests should also have a precise and descriptive title. Make it verbose for easy understanding. This will allow other contributors to understand the code and make contributing easier without much hassle. 

If you are submitting a PR for an issue; Let's say bug report or feature enhancement. Please do well to reference that issue on your PR.

 - Commit the local changes to your fork
 - Create a pull request

We expect to see a descriptive message about what the pull request is all about on the body. Include references where necessary, and also justification/reasoning where applicable. 

After you have completed the above mentioned, you can then finally send the pull request. At this stage, your PR will be seen by our collaborators, as well as the other like-minded contributors. There are many things to expect at this stage such as, comments from other contributors and collaborators and possibly, change requests if any is required. Keep in mind that this process may take a while as  PR will be thoroughly tested to make sure everything is perfect before merging. 


### Refactoring

Refactoring is a necessary part of any software project's evolution. The following guidelines cover refactoring pull requests for the project.

There are three categories of refactoring, the code only moves, code style fixes, code refactoring. In general, refactoring pull requests should not mix these three kinds of activity in order to make refactoring pull requests easy to review and uncontroversial. In all cases, refactoring PRs must not change the behaviour of code within the pull request (bugs must be preserved as is).

Project maintainers aim for a quick turnaround on refactoring pull requests, so where possible keep them short, uncomplex and easy to verify. 

---

### Peer Review

Anyone may participate in peer review which is expressed by comments in the pull request. Typically reviewers will review the code for obvious errors, as well as test out the patch set and opine on the technical merits of the patch. Project maintainers take into account the peer review when determining if there is consensus to merge a pull request 

Reviewers should include the commit hash which they reviewed in their comments.

Project maintainers reserve the right to weigh the opinions of peer reviewers using "common sense" judgment and also may weight based on meritocracy: Those that have demonstrated a deeper commitment and understanding towards the project (over time) or have clear domain expertise may naturally have more weight, as one would expect in all walks of life.

---

### Code Quality

When submitting code it is preferred to ensure the code quality is up to par (or better) than the existing one and unit-test don't fail.

### Tests
Wherever possible please include tests, especially if your change implements or impacts a library routine. Even the slightest change can have a ripple effect of chaos.

For code contributions that will lead to huge operational changes, please first discuss the changes you wish to make via an issue, email, or any other method before making a change. 

---

## Documentation

This is the creation of vital documents that are necessary for the project. Documentation also deals with written content creation either standardized documents or blogs. If you wish to create standardized documents or to make corrections and additions to our existing documents, feel free to do so and send a pull request.

For better implementation, ensure to create the document with markdown text styling.
Rename the file with respect to content and add ```.md``` at the end so markdown is effective.
Send a pull request after the document is created.

## Creating a pull request

Here's a detailed content on how to [Create a pull request](https://help.github.com/articles/creating-a-pull-request)

Simply put, the way to create a Pull request is first; 

1. Fork the repository of the project which in this case is [DanDanPlayForAndroid](https://github.com/xyoye/DanDanPlayForAndroid)
2. Commit modifications and changes to your fork
3. Send a [pull request](https://help.github.com/articles/creating-a-pull-request) to the original repository you forked your repository from in step 1

---

## Contact.

For further inquiries, you can contact the developer by opening an [issue](https://github.com/xyoye/DanDanPlayForAndroid/issues/new/choose) on the repository. You can also check out the developer's profile [here](https://github.com/xyoye).

Thank you for your interest in contributing to DanDanPlayForAndroid. I appreciate all the help with finding and fixing bugs, making performance improvements, and other tasks. Every contribution is helpful and I thank you for your effort.
