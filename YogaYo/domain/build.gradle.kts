plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id ("kotlin-kapt")
    kotlin("plugin.serialization")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {

    implementation("javax.inject:javax.inject:1")

    // Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp3
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // kotlinx.serialization core (애노테이션 처리 등에 필요)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1") // 버전은 프로젝트의 다른 모듈과 통일

    // kotlinx.serialization json (필요하다면 추가, domain 모듈에서 직접 파싱 안 하면 생략 가능하나 추가하는 것이 편리)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1") // 버전은 프로젝트의 다른 모듈과 통일
}
