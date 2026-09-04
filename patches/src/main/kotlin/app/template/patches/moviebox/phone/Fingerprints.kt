package app.template.patches.moviebox.phone

// All targets verified against smali for com.community.oneroom v4.0.02.0903.02
// No fingerprint objects — phone patch uses mutableClassDefByOrNull directly
// because all target classes and method names are non-obfuscated Kotlin data-class
// accessors and interface methods that survive R8 renaming.
