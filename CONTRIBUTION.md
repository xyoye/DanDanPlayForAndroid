# 参与仓库建设

首先，感谢您有参与本仓库建设的意愿。作为一个还在成长中的项目，我们十分欢迎您的加入。请仔细阅读以下内容，这将对您参与到建设中来的行为起到帮助作用。

## 开发环境

该项目在Android Studio 3+ with Gradle 4+的开发环境中开发，若您使用的版本过低，可能会导致问题。请在参与贡献前检查更新。

## KeyUtil.java 丢失？

当您clone本仓库后，您会发现Android Studio找不到KeyUtil.java。处于安全性方面的考虑，我们并没有直接在项目中存储需要的API Key。KeyUtil.java则是负责传递所需的API Key给程序的其它部分。同样出于安全性的考虑，我们没有在项目中包括该类。
所以，在您要参与贡献之前，请联系仓库的任一主要开发者，并说明您的参与意愿，我们将会把KeyUtil.java发送给您。
如果您要使用自己的API Key，您需要在自己的KeyUtil.java中提供以下三个函数来返回对应API Key：
```java
public static String getAppId(Context context){};
public static String getAppId2(Context context){};
public static String getAppSecret(Context context){};
```
