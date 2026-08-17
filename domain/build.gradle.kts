plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.softspector.testtable.domain"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":common"))
    api(libs.androidx.paging3)
    api(libs.kotlinx.coroutines.core)
}
