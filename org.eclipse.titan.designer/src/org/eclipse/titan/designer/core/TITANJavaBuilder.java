/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.compiler.ProjectSourceCompiler;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * Build system for java code generation.
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TITANJavaBuilder extends IncrementalProjectBuilder {

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		if (!TITANInstallationValidator.check(true)) {
			return project.getReferencedProjects();
		}

		if (!LicenseValidator.check()) {
			return project.getReferencedProjects();
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final boolean reportDebugInformation = store.getBoolean(PreferenceConstants.DISPLAYDEBUGINFORMATION);

		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Build", 2);
		
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		//TODO: this is temporary code!
		sourceParser.makefileCreatingAnalyzeAll();
		
		progress.worked(1);

		IProgressMonitor codeGeneratorMonitor = progress.newChild(1);
		codeGeneratorMonitor.beginTask("Checking prerequisites", sourceParser.getModules().size() + 1);
		for(Module module : sourceParser.getModules()) {
			TITANDebugConsole.println("Generating code for module `" + module.getIdentifier().getDisplayName() + "'");
			try {
				ProjectSourceCompiler.compile( module, reportDebugInformation );
			} catch ( CoreException e ) {
				ErrorReporter.logExceptionStackTrace("While generating Java code for module " + module.getIdentifier().getDisplayName(), e);
			}

			codeGeneratorMonitor.worked(1);
		}
		TITANDebugConsole.println("Generating code for single main");
		try {
			ProjectSourceCompiler.generateMain( project,sourceParser.getModules(), reportDebugInformation );
		} catch ( CoreException e ) {
			ErrorReporter.logExceptionStackTrace("While generating Java code for main module ", e);
		}

		codeGeneratorMonitor.worked(1);
		codeGeneratorMonitor.done();
		
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		super.clean(monitor);
	}

}
