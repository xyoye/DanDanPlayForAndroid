import com.xyoye.dandanplay.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目的路由插件
 */
class AndroidRouterPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.kapt")
            }

            extensions.configure<KaptExtension> {
                arguments {
                    arg("AROUTER_MODULE_NAME", project.name)
                }
            }

            dependencies {
                add("kapt", libs.findLibrary("alibaba.arouter.compiler").get())
            }
        }
    }
}