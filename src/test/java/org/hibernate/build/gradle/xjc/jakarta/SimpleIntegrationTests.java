package org.hibernate.build.gradle.xjc.jakarta;

import java.io.File;
import java.nio.file.Path;

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
		System.out.println( "Project dir : " + projectDir.toAbsolutePath() );
		Copier.copyProject( "simple/build.gradle", projectDir );

		final GradleRunner gradleRunner = GradleRunner.create()
				.withProjectDir( projectDir.toFile() )
				.withPluginClasspath()
				.withDebug( true )
				.withArguments( "clean", "compileJava", "--stacktrace", "--no-build-cache" )
				.forwardOutput();

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
}
