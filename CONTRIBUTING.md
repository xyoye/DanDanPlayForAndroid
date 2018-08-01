# 参与仓库建设

首先，感谢您有参与本仓库建设的意愿。作为一个还在成长中的项目，我们十分欢迎您的加入。在开始前，请阅读CODE_OF_CONDUCT.md（或其翻译版本CODE_OF_CONDUCT_ZH.md）来了解我们的“参与者公约”。在之后，请仔细阅读以下内容，这将对您参与到建设中来的行为起到帮助作用。

## 开发之前

本节告知您在开发之前应当做的事情。

### 联系我们

在开发前，请联系我们表明您的参与意愿，这样我们可以给您发送KeyUtil.java等开发必需的文件。

我们的联系方式可在CONTRIBUTOR.md中找到。我们十分欢迎任何对本项目有贡献的参与行为。欢迎联系！

### 开发环境

该项目在Android Studio 3+ 与 Gradle 4+的开发环境中开发，minSDK版本为21，targetSDK为26。若您使用的版本过低，可能会导致问题。请在参与贡献前检查更新。

### KeyUtil.java 丢失？

当您clone本仓库后，您会发现Android Studio找不到KeyUtil.java。出于安全性方面的考虑，我们并没有直接在项目中存储需要的API Key，而是进行了打乱处理。KeyUtil类则是负责解码并传递所需的API Key给程序的其它部分。由于先前提到的考虑，我们没有在项目中包括该类。
所以，在您要参与贡献之前，请联系仓库的任一主要开发者，并说明您的参与意愿，我们将会把KeyUtil.java发送给您。
如果您要使用自己的API Key，您需要在自己的KeyUtil.java中提供以下三个函数来返回对应API Key：

```java
public static String getAppId(Context context){};//弹弹Play API APPID
public static String getAppId2(Context context){};//腾讯Bugly APPID
public static String getAppSecret(Context context){};//弹弹Play API 密钥
```

在获得KeyUtil.java后，请不要将其发送给除已知贡献者之外的其他任何人，这有助于保护本项目所使用服务的安全。