import io.izzel.taboolib.gradle.*

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.11"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

taboolib {
//    install("common")
//    install("common-5")
//    install("module-kether")
//    install("module-ui")
//    install("module-nms")
//    install("module-nms-util")
//    install("module-chat")
//    install("module-effect")
//    install("module-configuration")
//    install("platform-bukkit")
//    install("expansion-command-helper")
    relocate("ink.ptms.um","tkworld.tools.mythicitemstyrke.um")
//    classifier = null
//    version = "6.0.12-26"
    env {
        install(UNIVERSAL, BUKKIT_ALL, NMS_UTIL, EFFECT, UI, KETHER)
    }
    version {
        taboolib = "6.1.1-beta20"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v12001:12001:universal")
    taboo("ink.ptms:um:1.0.1")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
