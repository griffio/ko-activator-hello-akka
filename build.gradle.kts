buildscript {

  repositories {
    gradleScriptKotlin()
  }

  dependencies {
    classpath(kotlinModule("gradle-plugin"))
  }
}

apply {
  plugin("kotlin")
  plugin<ApplicationPlugin>()
}

configure<ApplicationPluginConvention> {
  mainClassName = "MainKt"
}

repositories {
  gradleScriptKotlin()
}

dependencies {
  compile(kotlinModule("stdlib"))
  compile("com.typesafe.akka:akka-actor_2.11:2.4.8")
  testCompile("org.jetbrains.spek:spek:+")
}