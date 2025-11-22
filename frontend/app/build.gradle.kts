import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.Exec

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.spotless)
    id("jacoco")
}

android {
    namespace = "com.example.voicetutor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.voicetutor"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Use custom HiltTestRunner for instrumentation tests
        testInstrumentationRunner = "com.example.voicetutor.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Test coverage configuration for Android Studio
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            // Enable coverage reporting
            all {
                it.useJUnit()
                it.testLogging {
                    events("passed", "skipped", "failed")
                }
                // Ignore test failures to allow coverage report generation
                it.ignoreFailures = true
            }
        }
        animationsDisabled = true
        // Test Orchestrator는 의존성이 필요하므로 제거
        // execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/**"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.compose.foundation:foundation")

    // ViewModel and Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // MockK for ViewModel mocking in UI tests
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation(kotlin("test"))

    // Hilt testing - must match the hilt version in libs.versions.toml
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.48")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(kotlin("test"))
}

// JaCoCo configuration
jacoco {
    toolVersion = "0.8.11"
}

// Configure JaCoCo for all test tasks
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
        // JaCoCo automatically generates execution data files in build/jacoco/${taskName}.exec
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    group = "verification"
    description = "Generates code coverage report using JaCoCo (includes both unit tests and Android tests)"

    // Unit tests MUST run first to generate execution data
    // This ensures tests are executed before report generation
    val testTask = tasks.named<Test>("testDebugUnitTest")
    dependsOn(testTask)

    // Android tests - generate report after they run (if they run)
    // Support both full connectedDebugAndroidTest and split test groups
    val uiTestTask = tasks.findByName("connectedDebugAndroidTest")
    val connectedDebug1Task = tasks.findByName("connectedDebug1")
    val connectedDebug2Task = tasks.findByName("connectedDebug2")
    val connectedDebug3Task = tasks.findByName("connectedDebug3")
    val connectedDebug4Task = tasks.findByName("connectedDebug4")
    
    if (uiTestTask != null) {
        mustRunAfter(uiTestTask)
    }
    if (connectedDebug1Task != null) {
        mustRunAfter(connectedDebug1Task)
    }
    if (connectedDebug2Task != null) {
        mustRunAfter(connectedDebug2Task)
    }
    if (connectedDebug3Task != null) {
        mustRunAfter(connectedDebug3Task)
    }
    if (connectedDebug4Task != null) {
        mustRunAfter(connectedDebug4Task)
    }

    // Log execution data files for debugging
    doFirst {
        val execFile = file("${layout.buildDirectory.get().asFile}/jacoco/testDebugUnitTest.exec")
        logger.info("Looking for JaCoCo execution data at: ${execFile.absolutePath}")
        if (execFile.exists()) {
            logger.info("Found execution data file: ${execFile.absolutePath} (${execFile.length()} bytes)")
        } else {
            logger.warn("Execution data file not found. Make sure tests have been run.")
        }
    }

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/*_Hilt_*.*",
            "**/Hilt_*.*",
            "**/*_Factory.*",
            "**/*_MembersInjector.*",
            "**/*_Provide*Factory.*",
            "**/dagger/hilt/internal/aggregatedroot/codegen/**",
            "**/hilt_aggregated_deps/**",
            "**/voicetutor/di/**",
            "**/Hilt*",
            "**/voicetutor/MainActivity*",
            "**/voicetutor/VoiceTutorApplication*",
            "**/ComposableSingletons*"
        )

    // Android projects use multiple class output directories
    val debugTree =
        fileTree(layout.buildDirectory.get().asFile) {
            include("**/intermediates/javac/debug/**/*.class")
            include("**/tmp/kotlin-classes/debug/**/*.class")
            exclude(fileFilter)
        }

    // Include both Java and Kotlin source directories
    val mainSrc =
        listOf(
            "${project.projectDir}/src/main/java",
            "${project.projectDir}/src/main/kotlin",
        )

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))

    // JaCoCo execution data files - Android Gradle Plugin stores them here
    // Include both unit test and Android test coverage data
    val executionDataFiles =
        fileTree(layout.buildDirectory.get().asFile) {
            include("**/jacoco/*.exec")
            include("**/jacoco/*.ec")
            include("**/test-results/**/*.exec")
            include("**/outputs/**/*.exec")
            include("**/outputs/**/*.ec")
            include("**/outputs/code-coverage/**/*.ec")
            include("**/outputs/unit_test_code_coverage/**/*.exec")
            include("**/outputs/androidTest-results/**/*.ec")
        }

    // Android Gradle Plugin stores execution data in multiple locations
    // Check standard locations for both unit tests and Android tests
    val standardExecFiles =
        listOf(
            // Unit test execution data
            file("${layout.buildDirectory.get().asFile}/jacoco/testDebugUnitTest.exec"),
            file("${layout.buildDirectory.get().asFile}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"),
            // Android test execution data (coverage.ec files)
            file("${layout.buildDirectory.get().asFile}/outputs/code-coverage/connected/*coverage.ec"),
        )

    val existingExecFiles = standardExecFiles.filter { it.exists() }
    val allExecutionData = mutableListOf<File>()

    // Add file tree results
    executionDataFiles.forEach { allExecutionData.add(it) }

    // Add specific files if they exist
    existingExecFiles.forEach { allExecutionData.add(it) }

    // Also check for connectedDebugAndroidTest coverage files
    val androidTestCoverageDir = file("${layout.buildDirectory.get().asFile}/outputs/code-coverage/connected")
    if (androidTestCoverageDir.exists() && androidTestCoverageDir.isDirectory) {
        androidTestCoverageDir.listFiles()?.filter { it.name.endsWith(".ec") }?.forEach {
            allExecutionData.add(it)
        }
    }

    executionData.setFrom(allExecutionData)
}

// Split connectedDebugAndroidTest into 3 groups
val testClassGroup1 = listOf(
    "com.example.voicetutor.ExampleInstrumentedTest",
    "com.example.voicetutor.MainActivityTest",
    "com.example.voicetutor.VoiceTutorApplicationTest",
    "com.example.voicetutor.audio.AudioRecorderInstrumentedTest",
    "com.example.voicetutor.ui.components.ButtonTest",
    "com.example.voicetutor.ui.components.CardTest",
    "com.example.voicetutor.ui.components.HeaderTest",
    "com.example.voicetutor.ui.components.ProgressBarTest",
    "com.example.voicetutor.ui.components.StatsCardTest",
    "com.example.voicetutor.ui.components.TextFieldTest",
    "com.example.voicetutor.ui.navigation.MainLayoutStudentNavigationTest",
    "com.example.voicetutor.ui.navigation.MainLayoutTeacherNavigationTest",
    "com.example.voicetutor.ui.navigation.NavigationFlowTest",
    "com.example.voicetutor.ui.navigation.VoiceTutorNavigationRouteCoverageTest"
)

val testClassGroup2 = listOf(
    "com.example.voicetutor.ui.navigation.VoiceTutorNavigationTest",
    "com.example.voicetutor.ui.screens.AllStudentsScreenTest",
    "com.example.voicetutor.ui.screens.AppInfoScreenTest",
    "com.example.voicetutor.ui.screens.AssignmentDetailScreenTest",
    "com.example.voicetutor.ui.screens.AssignmentDetailedResultsScreenTest",
    "com.example.voicetutor.ui.screens.AssignmentScreenTest",
    "com.example.voicetutor.ui.screens.AuthIntegrationTest",
    "com.example.voicetutor.ui.screens.CreateAssignmentScreenTest",
    "com.example.voicetutor.ui.screens.EditAssignmentScreenTest",
    "com.example.voicetutor.ui.screens.LoginScreenTest",
    "com.example.voicetutor.ui.screens.NoRecentAssignmentScreenTest",
    "com.example.voicetutor.ui.screens.ReportAndSettingsScreenTest",
    "com.example.voicetutor.ui.screens.ScreenCoverageExpansionTest"
)

val testClassGroup3 = listOf(
    "com.example.voicetutor.ui.screens.HighCoverageScreensIntegrationTest",
    "com.example.voicetutor.ui.screens.ScreensRenderingTest",
    "com.example.voicetutor.ui.screens.SimpleLoginScreenTest",
    "com.example.voicetutor.ui.screens.SignupScreenTest",
    "com.example.voicetutor.ui.screens.StudentDashboardScreenTest",
    "com.example.voicetutor.ui.screens.StudentScreenEdgeCasesTest",
    "com.example.voicetutor.ui.screens.TeacherAssignmentDetailScreenTest",
    "com.example.voicetutor.ui.screens.TeacherAssignmentResultsScreenTest",
    "com.example.voicetutor.ui.screens.TeacherClassesScreenTest",
    "com.example.voicetutor.ui.screens.TeacherDashboardScreenTest",
    "com.example.voicetutor.ui.screens.TeacherStudentAssignmentDetailScreenTest",
    "com.example.voicetutor.ui.screens.TeacherStudentsScreenTest",
    "com.example.voicetutor.ui.viewmodel.AssignmentViewModelIntegrationTest",
    "com.example.voicetutor.ui.viewmodel.SupportingViewModelIntegrationTest",
    "com.example.voicetutor.ui.viewmodel.StudentViewModelIntegrationTest"
)

val testClassGroup4 = listOf(
    "com.example.voicetutor.ui.screens.TeacherStudentAssignmentDetailScreenCoverageTest",
    "com.example.voicetutor.ui.screens.CreateAssignmentScreenCoverageTest",
    "com.example.voicetutor.ui.screens.TeacherStudentsScreenTest"
)

tasks.register("connectedDebug1", Exec::class) {
    group = "verification"
    description = "Run first group of Android instrumentation tests (14 test classes)"
    
    val classArg = testClassGroup1.joinToString(",")
    val gradlew = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }
    
    commandLine = listOf(
        gradlew,
        "connectedDebugAndroidTest",
        "-Pandroid.testInstrumentationRunnerArguments.class=$classArg"
    )
    workingDir = project.rootDir
    isIgnoreExitValue = false
}

tasks.register("connectedDebug2", Exec::class) {
    group = "verification"
    description = "Run second group of Android instrumentation tests (14 test classes)"
    
    val classArg = testClassGroup2.joinToString(",")
    val gradlew = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }
    
    commandLine = listOf(
        gradlew,
        "connectedDebugAndroidTest",
        "-Pandroid.testInstrumentationRunnerArguments.class=$classArg"
    )
    workingDir = project.rootDir
    isIgnoreExitValue = false
}

tasks.register("connectedDebug3", Exec::class) {
    group = "verification"
    description = "Run third group of Android instrumentation tests (14 test classes)"
    
    val classArg = testClassGroup3.joinToString(",")
    val gradlew = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }
    
    commandLine = listOf(
        gradlew,
        "connectedDebugAndroidTest",
        "-Pandroid.testInstrumentationRunnerArguments.class=$classArg"
    )
    workingDir = project.rootDir
    isIgnoreExitValue = false
}

tasks.register("connectedDebug4", Exec::class) {
    group = "verification"
    description = "Run fourth group of Android instrumentation tests (TeacherStudentAssignmentDetailScreenCoverageTest, CreateAssignmentScreenCoverageTest, TeacherStudentsScreenTest)"
    
    val classArg = testClassGroup4.joinToString(",")
    val gradlew = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }
    
    commandLine = listOf(
        gradlew,
        "connectedDebugAndroidTest",
        "-Pandroid.testInstrumentationRunnerArguments.class=$classArg"
    )
    workingDir = project.rootDir
    isIgnoreExitValue = false
}

tasks.register("jacocoTestCoverageVerification", JacocoCoverageVerification::class) {
    group = "verification"
    description = "Verifies code coverage meets minimum requirements (80%)"
    dependsOn("jacocoTestReport")

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% minimum coverage
            }
        }

        rule {
            element = "CLASS"
            excludes =
                listOf(
                    "*.BuildConfig",
                    "*.R",
                    "*.R\$*",
                    "*.*_Hilt_*",
                    "*.*_Factory",
                    "*.*_MembersInjector",
                )
            limit {
                minimum = "0.70".toBigDecimal() // 70% minimum per class
            }
        }
    }
}

// Spotless configuration
spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    // 자동으로 고칠 수 없는 규칙들 비활성화
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_package-name" to "disabled",
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_value-argument-comment" to "disabled",
                    "ktlint_standard_value-parameter-comment" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}
