package org.hibernate.build.gradle.xjc.jakarta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Steve Ebersole
 */
public class SimpleIntegrationTests {
	@Test
    void testCompile(@TempDir Path projectDir) {
		final GradleRunner gradleRunner = createGradleRunner( projectDir, "compileJava" );

		final BuildResult result = gradleRunner.build();
		final BuildTask task = result.task( ":compileJava" );
		assertThat( task ).isNotNull();
		assertThat( task.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final File outputDir = new File( projectDir.toFile(), "build/generated/sources/xjc/main" );
		assertThat( outputDir ).exists();

		final File generatedSourcesDir = new File( outputDir, "simple" );
		assertThat( generatedSourcesDir ).exists();

		final File generatedPackageDir = new File( generatedSourcesDir, "org/hibernate/build/gradle/xjc/jakarta" );
		assertThat( generatedPackageDir ).exists();
		assertThat( generatedPackageDir ).isDirectoryContaining( file -> file.getName().equals( "JaxbRootImpl.java" ) );
	}

	private GradleRunner createGradleRunner(Path projectDir, String... tasks) {
		System.out.println( "Project dir : " + projectDir.toAbsolutePath() );
		Copier.copyProject( "simple/build.gradle", projectDir );

		final File mavenLocalPath = new File( projectDir.toFile(), "maven-local" );
		assertThat( mavenLocalPath.mkdir() ).isTrue();

		final ArrayList<String> arguments = new ArrayList<>( Arrays.asList( tasks ) );
		arguments.add( "-PlocalMavenRepo=" + mavenLocalPath.getAbsolutePath() );
		arguments.add( "--stacktrace" );

		return GradleRunner.create()
				.withProjectDir( projectDir.toFile() )
				.withPluginClasspath()
				.withDebug( true )
				.withArguments( arguments )
				.forwardOutput();
	}

	@Test
    void testJar(@TempDir Path projectDir) throws IOException {
		final GradleRunner gradleRunner = createGradleRunner( projectDir, "jar" );

		final BuildResult result = gradleRunner.build();
		final BuildTask task = result.task( ":jar" );
		assertThat( task ).isNotNull();
		assertThat( task.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final File libsDir = new File( projectDir.toFile(), "build/libs" );
		assertThat( libsDir ).exists();

		File jar = null;
		for ( File file : libsDir.listFiles() ) {
			if ( file.getName().endsWith( ".jar" ) ) {
				jar = file;
				break;
			}
		}

		assertThat( jar ).exists();

		final JarFile jarFile = new JarFile( jar );
		final JarEntry generatedSourceFileEntry = jarFile.getJarEntry( "org/hibernate/build/gradle/xjc/jakarta/JaxbRootImpl.class" );
		assertThat( generatedSourceFileEntry ).isNotNull();
	}

	@Test
    void testSourcesJar(@TempDir Path projectDir) throws IOException {
		final GradleRunner gradleRunner = createGradleRunner( projectDir, "sourcesJar" );

		final BuildResult result = gradleRunner.build();
		final BuildTask task = result.task( ":sourcesJar" );
		assertThat( task ).isNotNull();
		assertThat( task.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final File libsDir = new File( projectDir.toFile(), "build/libs" );
		assertThat( libsDir ).exists();

		File sourcesJar = null;
		for ( File file : libsDir.listFiles() ) {
			if ( file.getName().endsWith( "-sources.jar" ) ) {
				sourcesJar = file;
				break;
			}
		}

		assertThat( sourcesJar ).exists();

		final JarFile sourcesJarFile = new JarFile( sourcesJar );
		final JarEntry generatedSourceFileEntry = sourcesJarFile.getJarEntry( "org/hibernate/build/gradle/xjc/jakarta/JaxbRootImpl.java" );
		assertThat( generatedSourceFileEntry ).isNotNull();
	}

	@Test
    void testPublishing(@TempDir Path projectDir) {
		final GradleRunner gradleRunner = createGradleRunner( projectDir, "publish" );

		final BuildResult result = gradleRunner.build();

		final BuildTask metadataFileTask = result.task( ":generateMetadataFileForMainPublication" );
		assertThat( metadataFileTask ).isNotNull();
		assertThat( metadataFileTask.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final BuildTask pomTask = result.task( ":generatePomFileForMainPublication" );
		assertThat( pomTask ).isNotNull();
		assertThat( pomTask.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final BuildTask publishTask = result.task( ":publishMainPublicationToTempRepository" );
		assertThat( publishTask ).isNotNull();
		assertThat( publishTask.getOutcome() ).isEqualTo( TaskOutcome.SUCCESS );

		final File mavenLocalPath = new File( projectDir.toFile(), "maven-local" );
		assertThat( mavenLocalPath ).exists();
		assertThat( mavenLocalPath.listFiles() ).isNotEmpty();
		assertThat( mavenLocalPath ).isNotEmptyDirectory();
	}
}
