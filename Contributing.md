#English

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

---

#中文版

#DanDanPlayForAndroid的贡献者指南

这是一份DanDanPlayForAndroid的贡献者指南。如果您有意打开此文档，则意味着您愿意为DanDanPlayForAndroid增加价值，这真是太棒了！

DanDanPlayForAndroid是一个完全开源的项目。这意味着任何人都可以在[license](https://github.com/xyoye/DanDanPlayForAndroid/blob/master/LICENSE)的范围内使用，修改和重新分发我们的源代码。
此外，作为一个开源项目意味着它可用于协作开发，我们非常高兴能让您以任何方式为该项目的进步做出贡献。

我们总是很高兴收到用例，文档，代码，补丁，错误报告，功能请求和增强等方面的贡献。我们不限制对程序员的贡献，因此，您不需要成为程序员来分享您的关于该项目的想法。我们认为每一项贡献都是有价值的。自由的发布相关建议、错误崩溃信息、项目代码的评论以及或许只是看起来不够完美的问题，它们都应该成为GitHub上的一个issue。
如果你有冒险精神的话，非常欢迎您Fork存储库并提交拉取请求，以实现新功能、修复错误或清理代码。

本文件将包含为该项目做出贡献的流程和指南。

---

###提交功能请求

如果您想提出建议或功能请求，请检查issue，以确定该需求是否已被其他用户提交。如果已经请求了该需求，但这是一个尚未标记为“wontfix”的已关闭问题，请随时重新打开该问题或创建您的问题。
要申请新功能，您应该[创建一个issue](https://github.com/xyoye/DanDanPlayForAndroid/issues/new?template=feature_request.md)。

为了帮助开发人员理解功能请求;

 - 问题的标题应该是明确的，从而深入了解问题的内容。
 - 应适当说明应用或实施该特征的项目区域。如果可能，添加模型的屏幕截图。
 - 如果您的请求中包含详细的用例，那就太棒了。
 - 应考虑包含后可能需要的长期技术债和维护。

提交功能请求时，请为每个功能请求提出一个问题（即不提交包含功能列表的问题），否则这些问题很难跟踪并可能丢失。

---


## 错误报告

您在使用该应用时遇到错误吗？创建新issue来让开发人员知道它。

提交一个很棒的错误报告可以帮助开发人员查明错误的原因并有效地解决问题。

###如何提交错误报告的步骤。

在提交错误报告之前，

 - 确保您运行的是最新版本的软件
 - 确认它是否真的是一个错误而不是由系统上的插件引起的错误。与其他系统一起测试以验证
 - 如果在其他设备上测试后问题仍然存在，则确实存在错误。
 - 确认该问题未被其他用户反馈。请检查issue列表，如果已经被反馈，它可能在[已开启的问题](https://github.com/xyoye/DanDanPlayForAndroid/issues?q=is%3Aopen+is%3Aissue)或[已结束的问题](https://github.com/xyoye/DanDanPlayForAndroid/issues?q=is%3Aissue+is%3Aclosed)中。

错误报告最重要的是报告的内容。内容越是简洁明了，开发人员或其他人就越容易解决这个问题。

 - 问题的标题应该让开发人员了解报告的内容。
 - 简要说明您希望软件执行的行为。
 - 遇到错误时软件的实际行为。
 - 如果可能，重现错误以及屏幕截图和视频的步骤。
 - 测试环境：设备，操作系统，软件版本的详细信息。

---

## 代码贡献

您是否有一些新的功能、错误修复或您希望贡献的其他代码的想法？这部分将引导你如何提交你的贡献。

通过“贡献者工作流流程”维护代码库，使用“Pull Request”提供补丁提议。这有助于其它人的贡献，易于测试和同行评审。

要提供补丁，工作流程如下：

   - Fork存储库
   - 创建主题分支
   - 提交补丁

提交不应该冗长，而是具有[原子性](https://en.wikipedia.org/wiki/Atomic_commit#Atomic_commit_convention)。这样可以轻松查看代码，轻松识别错误，最重要的是，如果发现任何其他主要问题，可以轻松回滚更改。

简短易懂的提交消息至关重要。这样可以轻松识别所做的更改。拉请求还应具有精确的描述性标题以便于理解。这将使其他贡献者理解代码并使贡献更容易。

如果您要为某个Issue提交Pull Request,请在Pull Request上引用该问题。

  - 将本地更改提交到你fork的存储库
  - 创建Pull Request

我们希望看到一条关于拉请求的描述性消息。必要时包括参考资料，适用时也包括理由/推理。

完成上述操作后，您就可以最终发送请求了。在这个阶段，我们的合作者以及其他志同道合的贡献者将看到您的Pull Request。在这个阶段，有许多事情需要期待，例如来自其他贡献者和合作者的评论，可能还需要更改请求（如果需要的话）。请记住，此过程可能需要一段时间，因为在合并之前，将对Pull Request进行彻底测试，以确保一切都是完美的


###重构

重构是任何软件项目发展的必要组成部分。以下指导原则涵盖了重构项目的Pull Request。

重构分为三类：代码仅移动、代码样式修复、代码重构。一般来说，重构拉请求不应该混合这三种活动，以使重构拉请求易于审查和无争议。在任何情况下，重构Pull Request都不能改变其中代码的行为（bug必须保持原样）。

项目维护人员的目标是快速完成重构Pull Request，因此尽可能使它保持简短、不复杂和易于验证。

---

### 同行评审

任何人都可以参与评审和评论Pull Request。通常，审阅者将检查代码中是否存在明显的错误，以及测试补丁集并考虑补丁的技术优势。项目维护人员在合并拉取请求时会考虑同行评审的意见。

审阅者应在审核评论提交时应包含该提交的哈希值。

项目维护者保留使用“常识”判断来权衡同行评审者意见的权利，也可以根据精英制度来权衡：那些对项目（随着时间的推移）表现出更深刻的承诺和理解，或具有明确的领域专业知识的人，自然会有更大的权重，正如人们在各行各业所期望的那样。

---

###代码质量

提交代码时，最好确保代码质量高于现有代码（或更高），并且单元测试不会失败。

###测试
在可能的情况下，请包括测试，特别是当您的更改实现或影响库例程时。即使是最微小的变化也会产生混乱的涟漪效应。

对于将导致巨大操作更改的代码贡献，请首先通过问题、电子邮件或任何其他方法讨论您希望进行的更改，然后再进行更改。

---

##文档

这是创建项目所必需的重要文档。文档还处理标准化文档或博客中的书面内容创建。如果您希望创建标准化文档或对现有文档进行更正和添加，请随时这样做并发送请求。

为了更好地实现，请确保使用markdown文本样式创建文档。根据内容重命名文件，并在末尾添加.md，这样标记就有效了。创建文档后发送Pull Request。

##创建拉取请求

以下是有关如何创建[Pull Request](https://help.github.com/articles/creating-a-pull-request)的详细内容

简单地说，创建Pull Request的方法是：

1.fork项目的存储库，在本例中是[DanDanPlayForAndroid](https://github.com/xyoye/DanDanPlayForAndroid)
2.对fork进行修改和更改
3.将[Pull Request](https://help.github.com/articles/creating-a-pull-request)发送到您在步骤1中分叉存储库的原始存储库

---

##联系。

如需进一步咨询，您可以通过在存储库上打开[issue](https://github.com/xyoye/DanDanPlayForAndroid/issues/new/choose)来联系开发人员。您还可以查看[开发人员的个人资料](https://github.com/xyoye)。

感谢您有兴趣为DanDanPlayForAndroid做出贡献。我非常感谢查找和修复错误，改进性能以及其他任务的所有帮助。每一个贡献都是有帮助的，我感谢您的努力。