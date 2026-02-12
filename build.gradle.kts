// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Acá le decimos "declarame estos plugins con estas versiones, pero NO los apliques todavía"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
}