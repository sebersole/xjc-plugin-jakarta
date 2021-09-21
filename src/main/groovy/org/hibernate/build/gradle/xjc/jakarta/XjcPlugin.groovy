package org.hibernate.build.gradle.xjc.jakarta


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 * @author Steve Ebersole
 */
class XjcPlugin implements Plugin<Project> {
	private static final String ANT_TASK_IMPL = "org.jvnet.jaxb2_commons.xjc.XJC2Task"

	@Override
	void apply(Project project) {
		project.plugins.apply( JavaPlugin.class )

		// Create the Plugin extension object (for users to configure our execution).
		project.extensions.create( XjcExtension.REGISTRATION_NAME, XjcExtension.class, project )

		// Create the Gradle Configuration for users to be able to specify JAXB/XJC dependencies...
		final Configuration configuration = project.configurations.maybeCreate( "xjc" )
		configuration.setDescription( "Dependencies for running xjc (JAXB class generation)" )
		configuration.defaultDependencies {dependencies ->
			dependencies.add( project.getDependencies().create( 'org.glassfish.jaxb:jaxb-xjc:3.0.2' ) )
			dependencies.add( project.getDependencies().create( 'org.glassfish.jaxb:jaxb-runtime:3.0.2' ) )
		}

		// Create the xjc grouping task
		final Task xjcTask = project.getTasks().maybeCreate( "xjc" )
		xjcTask.setGroup( "xjc" )
		xjcTask.setDescription( "Grouping task for executing one-or-more XJC compilations" )

		final SourceSet mainSourceSet = project.getConvention()
				.getPlugin( JavaPluginConvention.class )
				.getSourceSets()
				.findByName( SourceSet.MAIN_SOURCE_SET_NAME )
		project.getTasks().findByName( mainSourceSet.getCompileJavaTaskName() ).dependsOn( xjcTask )

		project.ant.saveStreams = false

		project.afterEvaluate {
			project.ant.taskdef(name: 'xjc', classname: ANT_TASK_IMPL, classpath: project.configurations.xjc.asPath)
		}
	}
}
