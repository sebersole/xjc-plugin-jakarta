package org.hibernate.build.gradle.xjc.jakarta;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Steve Ebersole
 */
public class XjcPluginUnitTests {
	@Test
	public void testPlugin() {
		final Project project = ProjectBuilder.builder().build();
		project.getRepositories().add( project.getRepositories().mavenCentral() );

		project.getPlugins().apply( XjcPlugin.class );

		final XjcExtension xjcExtension = (XjcExtension) project.getExtensions().getByName( "xjc" );
		xjcExtension.getSchemas().create( "schema1" );
		xjcExtension.getSchemas().create( "schema2" );

		final XjcTask xjcSchema1Task = (XjcTask) project.getTasks().getByName( "xjcSchema1" );
		assertThat( xjcSchema1Task ).isNotNull();

		assertThat( xjcSchema1Task.getOutputDirectory().get().getAsFile().getAbsolutePath() ).endsWith( "generated/sources/xjc/main/schema1" );

		xjcSchema1Task.getOutputDirectory().set( project.file( "path/to/output" ) );
		xjcSchema1Task.getXsdFile().set( project.file( "path/to/schema1.xsd" ) );
		xjcSchema1Task.getXjcBindingFile().set( project.file( "path/to/schema1.xjc" ) );

		final XjcTask xjcSchema2Task = (XjcTask) project.getTasks().getByName( "xjcSchema2" );
		assertThat( xjcSchema2Task ).isNotNull();
		assertThat( xjcSchema2Task.getOutputDirectory().get().getAsFile().getAbsolutePath() ).endsWith( "generated/sources/xjc/main/schema2" );
	}
}
