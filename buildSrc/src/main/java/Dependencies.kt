object Dependencies {
    private object Versions {
        const val kotlin = "1.7.10"
        const val kotlin_coroutines = "1.6.4"
        const val arouter = "1.5.2"
        const val room = "2.4.3"
        const val retrofit = "2.9.0"
        const val moshi = "1.13.0"
        const val exoplayer = "2.18.1"
        const val lifecycle = "2.5.1"
        const val navigation = "2.3.0"
    }

    object Alibaba {
        const val arouter_api = "com.alibaba:arouter-api:${Versions.arouter}"
        const val arouter_compiler = "com.alibaba:arouter-compiler:${Versions.arouter}"
        const val alicloud_feedback = "com.aliyun.ams:alicloud-android-feedback:3.3.7"
        const val alicloud_analysis = "com.aliyun.ams:alicloud-android-man:1.2.0"
    }

    object AndroidX {
        const val junit_ext = "androidx.test.ext:junit:1.1.1"
        const val espresso = "androidx.test.espresso:espresso-core:3.2.0"

        const val core = "androidx.core:core-ktx:1.8.0"
        const val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
        const val lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"

        const val activity_ktx = "androidx.activity:activity-ktx:1.5.1"

        const val appcompat = "androidx.appcompat:appcompat:1.5.0"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.1"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val multidex = "androidx.multidex:multidex:2.0.1"
        const val palette = "androidx.palette:palette:1.0.0"
        const val startup = "androidx.startup:startup-runtime:1.1.1"
        const val preference = "androidx.preference:preference:1.2.0"
        const val paging = "androidx.paging:paging-runtime-ktx:3.1.1"

        const val room_ktx = "androidx.room:room-ktx:${Versions.room}"
        const val room = "androidx.room:room-runtime:${Versions.room}"
        const val room_compiler = "androidx.room:room-compiler:${Versions.room}"

        const val navigation_fragment_ktx =
            "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
        const val navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    }

    object Apache {
        const val commons_net = "commons-net:commons-net:3.7.2"
    }

    object Github {
        const val banner = "io.github.youth5201314:banner:2.2.2"
        const val glide = "com.github.bumptech.glide:glide:4.13.2"
        //ftp
        const val nano_http = "org.nanohttpd:nanohttpd:2.3.1"
        //smb
        const val smbj = "com.hierynomus:smbj:0.10.0"
        const val dcerpc = "com.rapid7.client:dcerpc:0.10.0"
        //switch keyboard panel
        const val keyboard_panel = "com.github.albfernandez:juniversalchardet:2.4.0"
        const val jsoup = "org.jsoup:jsoup:1.11.2"
    }

    object Google {
        const val material = "com.google.android.material:material:1.6.1"

        const val exoplayer = "com.google.android.exoplayer:exoplayer:${Versions.exoplayer}"
        const val exoplayer_core =
            "com.google.android.exoplayer:exoplayer-core:${Versions.exoplayer}"
        const val exoplayer_dash =
            "com.google.android.exoplayer:exoplayer-dash:${Versions.exoplayer}"
        const val exoplayer_hls = "com.google.android.exoplayer:exoplayer-hls:${Versions.exoplayer}"
        const val exoplayer_smoothstraming =
            "com.google.android.exoplayer:exoplayer-smoothstreaming:${Versions.exoplayer}"
        const val exoplayer_rtmp =
            "com.google.android.exoplayer:extension-rtmp:${Versions.exoplayer}"
    }
    object Huawei {
        const val scan = "com.huawei.hms:scan:1.3.1.300"
    }

    object Junit {
        const val junit = "junit:junit:4.12"
    }

    object Kotlin {
        const val lib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
        const val stdlib_jdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
        const val coroutines_core =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin_coroutines}"
        const val coroutines_android =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines}"
    }

    object Square {
        const val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.9.1"
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val retrofit_moshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
        const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
        const val moshi_codegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    }

    object Tencent {
        const val mmkv = "com.tencent:mmkv-static:1.2.14"
        const val bugly = "com.tencent.bugly:crashreport_upgrade:1.6.1"
        const val bugly_native = "com.tencent.bugly:nativecrashreport:3.9.2"
    }

    object VLC {
        const val vlc = "org.videolan.android:libvlc-all:3.5.1"
    }
}