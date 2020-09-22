import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val springBootVersion = "2.1.8.RELEASE"
val jaxRiVersion = "2.3.2"
val nimbusSDKVersion = "7.0.3"
val oidcSupportVersion = "0.2.18"
val tokenSupportVersion = "1.0.1"
val prometheusVersion = "1.0.6"
val logstashVersion = "4.10"
val slf4jVersion = "1.7.25"
val commonsLangVersion = "3.5"
val commonsTextVersion = "1.8"
val javaxActivationVersion = "1.2.0"
val javaxInjectVersion = "1"
val kotlinLibVersion = "1.3.60"
val kotlinJacksonVersion = "2.9.8"

plugins {
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "http://packages.confluent.io/maven/")
    maven(url = "https://repo1.maven.org/maven2/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinLibVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinLibVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("com.nimbusds:oauth2-oidc-sdk:$nimbusSDKVersion")
    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")

    implementation("com.sun.xml.ws:jaxws-ri:$jaxRiVersion")
    implementation("com.sun.activation:javax.activation:$javaxActivationVersion")

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jersey:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-logging:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jetty:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("javax.inject:javax.inject:$javaxInjectVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.ApplicationKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "11"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "11"
    }
}
