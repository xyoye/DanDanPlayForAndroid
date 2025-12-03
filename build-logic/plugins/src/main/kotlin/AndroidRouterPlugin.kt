import com.xyoye.dandanplay.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目的路由插件
 */
class AndroidRouterPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("ksp", libs.findLibrary("github.router.apt").get())
            }
        }
    }
}