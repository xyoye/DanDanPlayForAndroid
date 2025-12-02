import com.android.build.gradle.LibraryExtension
import com.xyoye.dandanplay.configureCompile
import com.xyoye.dandanplay.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目的Library插件
 */
class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    dataBinding = true
                    viewBinding = true
                }

                configureCompile()
                configureKotlin()
            }
        }
    }
}