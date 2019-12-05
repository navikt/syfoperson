import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val springBootVersion = "2.1.8.RELEASE"
val egenAnsattV1Version = "1.0.1"
val cxfVersion = "3.3.3"
val jaxRsApiVersion = "2.0.1"
val oidcSpringSupportVersion = "0.2.4"
val tokenSupportVersion = "1.0.1"
val prometheusVersion = "1.0.6"
val logstashVersion = "4.10"
val slf4jVersion = "1.7.25"
val commonsLangVersion = "3.5"
val commonsTextVersion = "1.8"
val javaxInjectVersion = "1"
val kotlinLibVersion = "1.2.71"
val kotlinJacksonVersion = "2.9.8"


plugins {
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
}

buildscript {
    dependencies {
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://repo.adeo.no/repository/maven-releases/")
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "http://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinLibVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinLibVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("no.nav.security:oidc-support:$oidcSpringSupportVersion")
    implementation("no.nav.security:oidc-spring-support:$oidcSpringSupportVersion")

    implementation("no.nav.syfo.tjenester:egenAnsatt-v1-tjenestespesifikasjon:$egenAnsattV1Version")

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jersey:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-logging:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-cache:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jetty:$springBootVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("javax.ws.rs:javax.ws.rs-api:$jaxRsApiVersion")
    implementation("javax.inject:javax.inject:$javaxInjectVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")

    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testCompile("no.nav.security:oidc-spring-test:$oidcSpringSupportVersion")
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
        kotlinOptions.jvmTarget = "1.8"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }
}
