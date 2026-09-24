// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("com.android.library") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    //kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}