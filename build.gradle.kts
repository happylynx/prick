import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.21"
    id("application")
}

group = "happybox"
version = "1.0-SNAPSHOT"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation(group = "info.picocli", name = "picocli", version = "3.9.5")
//    testCompile group: 'junit', name: 'junit', version: '4.12'

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.4.1")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.4.1")
//    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-launcher", version = "5.4.1")
}

/*
 *         <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.2.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.2.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <version>LATEST</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>2.23.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>2.23.0</version>
            <scope>test</scope>
        </dependency> 
 */

tasks.jar {
    manifest {
        attributes(
                "Implementation-Version" to 0.1,
                "Main-Class" to "com.github.happylynx.prick.cli.Main"
        )
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

application {
    mainClassName = "com.github.happylynx.prick.cli.Main"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
