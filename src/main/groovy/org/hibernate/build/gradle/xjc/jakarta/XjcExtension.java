package org.hibernate.build.gradle.xjc.jakarta;

import jakarta.inject.Inject;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;

import groovy.lang.Closure;


/**
 * Gradle DSL extension for configuring XJC (JAXB "compilation")
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("UnstableApiUsage")
public class XjcExtension {
	public static final String REGISTRATION_NAME = "xjc";

	private final DirectoryProperty outputDirectory;
	private final NamedDomainObjectContainer<SchemaDescriptor> schemas;

	@Inject
	public XjcExtension(final Project project) {
		outputDirectory = project.getObjects().directoryProperty();
		outputDirectory.convention( project.getLayout().getBuildDirectory().dir( "generated/sources/xjc/main" ) );

		// Create a dynamic container for SchemaDescriptor definitions by the user.
		// 		- for each "compilation" they define, create a Task to perform the "compilation"
		schemas = project.container( SchemaDescriptor.class, new SchemaDescriptorFactory( this, project ) );
	}

	@OutputDirectory
	public DirectoryProperty getOutputDirectory() {
		return outputDirectory;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	public NamedDomainObjectContainer<SchemaDescriptor> schemas(Closure closure) {
		return schemas.configure( closure );
	}

	@SuppressWarnings("unused")
	public final NamedDomainObjectContainer<SchemaDescriptor> getSchemas() {
		return schemas;
	}
}
