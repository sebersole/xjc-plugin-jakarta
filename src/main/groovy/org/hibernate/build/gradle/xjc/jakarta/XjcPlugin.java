package org.hibernate.build.gradle.xjc.jakarta;

import java.util.LinkedHashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import groovy.lang.Closure;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

/**
 * @author Steve Ebersole
 */
public class XjcPlugin implements Plugin<Project> {
	public static final String DEFAULT_JAXB_RUNTIME_VERSION = "4.0.2";
	public static final String DEFAULT_XJC_DEP = "org.glassfish.jaxb:jaxb-xjc:" + DEFAULT_JAXB_RUNTIME_VERSION;
	public static final String DEFAULT_RUNTIME_DEP = "org.glassfish.jaxb:jaxb-runtime:" + DEFAULT_JAXB_RUNTIME_VERSION;
	private static final String ANT_TASK_IMPL = "org.jvnet.jaxb2_commons.xjc.XJC2Task";

	@Override
	public void apply(final Project project) {
		project.getPlugins().apply( JavaPlugin.class );

		// Create the xjc grouping task
		final Task groupingTask = project.getTasks().create( "xjc", xjcTask -> {
			xjcTask.setGroup( "xjc" );
			xjcTask.setDescription( "Grouping task for executing one-or-more XJC compilations" );
		} );

		// Create the Plugin extension object (for users to configure our execution).
		project.getExtensions().create( XjcExtension.REGISTRATION_NAME, XjcExtension.class, groupingTask, project );

		// Create the Gradle Configuration for users to be able to specify JAXB/XJC dependencies...
		final Configuration configuration = project.getConfigurations().maybeCreate( "xjc" );
		configuration.setDescription( "Dependencies for running xjc (JAXB class generation)" );
		configuration.defaultDependencies( (dependencies) -> {
			final DependencyHandler dependencyHandler = project.getDependencies();
			dependencies.add( dependencyHandler.create( DEFAULT_XJC_DEP ) );
			dependencies.add( dependencyHandler.create( DEFAULT_RUNTIME_DEP ) );
		} );

		final SourceSetContainer sourceSets = project.getExtensions().getByType( SourceSetContainer.class );
		final SourceSet mainSourceSet = sourceSets.getByName( MAIN_SOURCE_SET_NAME );

		project.getTasks().named( mainSourceSet.getCompileJavaTaskName() ).configure( (task) -> task.dependsOn( groupingTask ) );

		project.getAnt().setSaveStreams( false );

		project.afterEvaluate( new Closure( this, this ) {
			public Object doCall(Object it) {
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>( 3 );
				map.put( "name", "xjc" );
				map.put( "classname", ANT_TASK_IMPL );
				map.put( "classpath", project.getConfigurations().getByName( "xjc" ).getAsPath() );
				return project.getAnt().invokeMethod( "taskdef", new Object[] { map } );
			}

			public Object doCall() {
				return doCall( null );
			}
		} );
	}

}
