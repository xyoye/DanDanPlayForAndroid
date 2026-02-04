import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.xyoye.dandanplay.configureCompile
import com.xyoye.dandanplay.configureKotlin
import com.xyoye.dandanplay.configureSigning
import com.xyoye.dandanplay.configureVariantName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目的Application插件
 */
class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig.multiDexEnabled = true
                defaultConfig.targetSdk

                buildFeatures {
                    dataBinding = true
                    viewBinding = true
                }

                splits {
                    abi {
                        isEnable = true
                        reset()
                        include("armeabi-v7a", "arm64-v8a")
                        isUniversalApk = false
                    }
                }

                configureCompile()
                configureKotlin()
                configureSigning(project)

                extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
                    ?.configureVariantName()
            }
        }
    }
}
