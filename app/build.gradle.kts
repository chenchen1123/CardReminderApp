import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// 自动生成递增的时间戳版本号 (格式: 20260813)
val timeVersionCode = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
val timeVersionName = "2.6.${SimpleDateFormat("MMddHH").format(Date())}"

android {
    namespace = "com.cardreminder.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cardreminder.app"
        minSdk = 24
        targetSdk = 34
        versionCode = timeVersionCode
        versionName = timeVersionName
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.6.0")
}
