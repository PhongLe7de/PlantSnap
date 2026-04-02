import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream -> localProperties.load(stream) }
}

android {
    namespace = "com.plantsnap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.plantsnap"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "com.plantsnap.HiltTestRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties.getProperty("SUPABASE_KEY") ?: ""}\"")
        buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"${localProperties.getProperty("GOOGLE_SERVER_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "PLANTNET_API_KEY", "\"${localProperties.getProperty("PLANTNET_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

afterEvaluate {
    tasks.register<JacocoReport>("jacocoTestReport") {
        dependsOn("connectedDebugAndroidTest")

        reports {
            xml.required.set(true)
            xml.outputLocation.set(
                layout.buildDirectory.file(
                    "reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
                )
            )
            html.required.set(true)
            html.outputLocation.set(
                layout.buildDirectory.dir(
                    "reports/jacoco/jacocoTestReport/html"
                )
            )
            csv.required.set(false)
        }


        sourceDirectories.setFrom(
            files(
                "${projectDir}/src/main/java",
                "${projectDir}/src/main/kotlin"
            )
        )


        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir(
                "intermediates/javac/debug/compileDebugJavaWithJavac/classes"
            )) {
                exclude(
                    // Android generated
                    "**/R.class",
                    "**/R$*.class",
                    "**/BuildConfig.*",
                    "**/Manifest*.*",
                    // Hilt generated
                    "**/*_MembersInjector.class",
                    "**/*_Factory.class",
                    "**/*Module_*Factory.class",
                    "**/Hilt_*.class",
                    "**/*HiltModules*.class",
                    "**/*_HiltComponents*.class",
                    // DI modules and application class
                    "**/di/**",
                    "**/*Application.class",
                    // Pure data / model classes
                    "**/model/**",
                    "**/*Entity.class",
                    "**/*Dto.class",
                    // Navigation constants
                    "**/*NavRoutes*",
                    // Theme
                    "**/ui/theme/**"
                )
            },

            fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(
                    "**/R.class", "**/R$*.class", "**/BuildConfig.*",
                    "**/*_MembersInjector.class", "**/*_Factory.class",
                    "**/Hilt_*.class", "**/di/**", "**/model/**"
                )
            }
        )

        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(
                    "outputs/code_coverage/**/*.ec",
                    "jacoco/**/*.ec"
                )
            }
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.retrofit.converter.kotlinx.serialization)

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation(libs.okhttp.logging)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.compose.auth)

    // Ktor (HTTP engine for Supabase)
    implementation(libs.ktor.client.android)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Credentials (for Google Sign-In)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.googleid)

    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Google Fonts
    implementation(libs.androidx.compose.ui.text.google.fonts)
}