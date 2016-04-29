/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Peter Olah
 */
//FIXME clean up selection methods if this way of working is tested to be OK
public final class BrokenPartsViaReferences extends SelectionMethodBase implements IBaseAnalyzer {
	// when the definition based search for parts to be analyzed exceeds this limit we switch back to the import based method.
	// 1 second in nanoseconds
	private final static long TIMELIMIT = (long)1e+9;

	private final CompilationTimeStamp timestamp;
	private final Map<Module, List<Assignment>> moduleAndBrokenAssignments;
	private final int brokenModulesLimit;//FIXME should be a local constant
	private boolean analyzeOnlyAssignments;
	
	public BrokenPartsViaReferences(final SelectionAlgorithm selectionAlgorithm, final CompilationTimeStamp timestamp) {
		super(selectionAlgorithm);
		moduleAndBrokenAssignments = new HashMap<Module, List<Assignment>>();
		analyzeOnlyAssignments = false;
		brokenModulesLimit = Platform.getPreferencesService().getInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.BROKENMODULESRATIO, 100, null);
		this.timestamp = timestamp;
		header = "\n**Selection with Broken parts via references is started at:";
		footer = "**Selection with Broken parts via references is ended at:  ";
	}

	public Map<Module, List<Assignment>> getModuleAndBrokenDefs() {
		return moduleAndBrokenAssignments;
	}

	public boolean getAnalyzeOnlyDefinitions() {
		return analyzeOnlyAssignments;
	}

	@Override
	public void execute() {
		if (writeDebugInfo) {
			TITANDebugConsole.println(String.format(format, header, simpleDateFormat.format(new Date())));
		}
		start = System.nanoTime();

		List<Module> startModules = new ArrayList<Module>();
		Map<Module, List<Module>> invertedImports = buildInvertedImportStructure(allModules, startModules);

		computeAnalyzeOnlyDefinitionsFlag(allModules, startModules);

		if (analyzeOnlyAssignments) {
			Map<Module, List<AssignmentHandler>> result = collectBrokenParts(startModules, invertedImports);
			if (writeDebugInfo && (System.nanoTime()-start < TIMELIMIT)) {
				writeDebugInfo(result);
			}
			collectRealBrokenParts(result);
		}

		if(writeDebugInfo && System.nanoTime() - start > TIMELIMIT) {
			TITANDebugConsole.println("  Switching back to old selection format");
		}
		// if we need to use the old selection or the new selection method took too long
		if(!analyzeOnlyAssignments || (System.nanoTime()-start > TIMELIMIT)) {
			analyzeOnlyAssignments = false;
			modulesToCheck.clear();
			moduleAndBrokenAssignments.clear();
			List<Module> modules = collectBrokenModulesViaInvertedImports(startModules, invertedImports);
			modulesToCheck.addAll(modules);
		}

		afterExecute();

		end = System.nanoTime() - start;
		if (writeDebugInfo) {
			TITANDebugConsole.println(String.format(format, footer, simpleDateFormat.format(new Date())));
			infoAfterExecute();
		}
	}

	public void computeAnalyzeOnlyDefinitionsFlag(final List<Module> allModules, final List<Module> startModules) {
		float brokenModulesRatio = (float) ((startModules.size() * 100.0) / allModules.size());
		if (Float.compare(brokenModulesRatio, (float) brokenModulesLimit) < 0) {
			analyzeOnlyAssignments = true;
		}
	}

	/**
	 * It is build an inverted import structure and identify startmodules, which CompilationTimeStamp is null.
	 * 
	 * @param allModules
	 *            the list of modules to be check. Initially all modules.
	 * @param startModules
	 *            the list of modules to be check. Initially all modules, but the function will remove those that can be skipped.
	 * 
	 * @return invertedImports contains the next:<br>
	 *         - key: a module.<br>
	 *         - values: in these modules the key module is used, so all values imported this module, it is an inverted "imported" connection.<br>
	 *         If module A import B, C, D and module B import D, E then the next structure will be built:<br>
	 *         A = [],<br>
	 *         B = [A],<br>
	 *         C = [A],<br>
	 *         D = [A, B],<br>
	 *         E = [B]<br>
	 */
	protected Map<Module, List<Module>> buildInvertedImportStructure(final List<Module> allModules, final List<Module> startModules) {

		Map<Module, List<Module>> invertedImports = new HashMap<Module, List<Module>>();

		for (Module actualModule : allModules) {
			// Collect injured modules directly into startModules, we will start the checking from these modules.
			// Collect modules which have not been checked semantically.
			if ((actualModule.getLastCompilationTimeStamp() == null || !semanticallyChecked.contains(actualModule.getName())) && !startModules.contains(actualModule) ) {
				startModules.add(actualModule);
			}

			// We have to add all module to get a correct inverted import structure.
			// It covers the case when a module is a top-level module, so it has't got any import.
			if (!invertedImports.containsKey(actualModule)) {
				invertedImports.put(actualModule, new ArrayList<Module>());
			}

			for (Module actualImportedModule : actualModule.getImportedModules()) {
				if (invertedImports.containsKey(actualImportedModule)) {
					List<Module> dependentModules = invertedImports.get(actualImportedModule);

					if (!dependentModules.contains(actualModule)) {
						dependentModules.add(actualModule);
					}
				} else {
					List<Module> temp = new ArrayList<Module>();
					temp.add(actualModule);
					invertedImports.put(actualImportedModule, temp);
				}
			}
		}

		return invertedImports;
	}

	protected List<Module> collectBrokenModulesViaInvertedImports(final List<Module> startModules, final Map<Module, List<Module>> invertedImports) {
		
		List<Module> startModulesCopy = new ArrayList<Module>(startModules);

		List<Module> result = new ArrayList<Module>();
		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		if (writeDebugInfo) {
			for (Module startModule: startModules) {
				TITANDebugConsole.println("  ** Module " + startModule.getName() + " can not be skipped as it was not yet analyzed.", stream);
			}
		}

		for (int s = 0; s < startModulesCopy.size(); ++s) {
			Module startModule = startModulesCopy.get(s);
			if (!result.contains(startModule)) {
				result.add(startModule);
			}
			List<Module> whereStartModuleUsed = invertedImports.get(startModule);
			for (int d = 0; d < whereStartModuleUsed.size(); ++d) {
				Module dependentModule = whereStartModuleUsed.get(d);
				if (!startModulesCopy.contains(dependentModule)) {
					startModulesCopy.add(dependentModule);
					if (writeDebugInfo) {
						TITANDebugConsole.println("  ** Module " + dependentModule.getName() + " can not be skipped as it depends on " + startModule.getName() + " which needs to be checked.", stream);
					}
				}
			}
		}
		return result;
	}

	protected Map<Module, List<AssignmentHandler>> collectBrokenParts(final List<Module> startModules, final Map<Module, List<Module>> invertedImports) {

		List<Module> startModulesCopy = new ArrayList<Module>(startModules);

		Map<Module, List<AssignmentHandler>> moduleAndBrokenAssignments = new HashMap<Module, List<AssignmentHandler>>();

		processStartModules(startModulesCopy, moduleAndBrokenAssignments);

		for (int i = 0; i < startModulesCopy.size() && (System.nanoTime()-start < TIMELIMIT); ++i) {
			Module startModule = startModulesCopy.get(i);
			List<AssignmentHandler> startAssignments;
			if (moduleAndBrokenAssignments.containsKey(startModule)) {
				startAssignments = moduleAndBrokenAssignments.get(startModule);
			} else {
				startAssignments = getAssignmentsFrom(startModule);
				moduleAndBrokenAssignments.put(startModule, startAssignments);
			}

			if (!startAssignments.isEmpty()) {
				List<Module> whereStartModuleUsed = invertedImports.get(startModule);
				for (int j = 0; j < whereStartModuleUsed.size(); ++j) {
					Module dependentModule = whereStartModuleUsed.get(j);
					List<AssignmentHandler> dependentAssignments;
					if (moduleAndBrokenAssignments.containsKey(dependentModule)) {
						dependentAssignments = moduleAndBrokenAssignments.get(dependentModule);
					} else {
						dependentAssignments = getAssignmentsFrom(dependentModule);
						moduleAndBrokenAssignments.put(dependentModule, dependentAssignments);
					}

					// We have to separate broken and not broken definition, because of postcheck.
					List<AssignmentHandler> brokens = new ArrayList<AssignmentHandler>();
					List<AssignmentHandler> notBrokens = new ArrayList<AssignmentHandler>();

					for (int s = 0; s < startAssignments.size(); ++s) {
						AssignmentHandler startAssignment = startAssignments.get(s);
						if (startAssignment.getIsContagious()) {
							for (int d = 0; d < dependentAssignments.size(); ++d) {
								AssignmentHandler dependentAssignment = dependentAssignments.get(d);
								dependentAssignment.check(startAssignment);
								if (dependentAssignment.getIsInfected()) {
									if (!startModulesCopy.contains(dependentModule)) {
										startModulesCopy.add(dependentModule);
									}

									brokens.add(dependentAssignment);
								}
							}
						}
					}
					
					for (int d = 0; d < dependentAssignments.size(); ++d) {
						AssignmentHandler dependentAssignment = dependentAssignments.get(d);
						if (!dependentAssignment.getIsInfected()) {
							notBrokens.add(dependentAssignment);
						}
					}

					// Have to post check of local definition of modules.
					// A definition can reference an other definition too.
					checkLocalAssignments(brokens, notBrokens);

					// If dependent module not added startModules,
					// it means it has not got broken definition,
					// so we have to delete it from moduleAndBrokenDefs.
					if (!startModulesCopy.contains(dependentModule)) {
						moduleAndBrokenAssignments.remove(dependentModule);
					}

				}
			}

		}

		return moduleAndBrokenAssignments;
	}

	protected void collectRealBrokenParts(final Map<Module, List<AssignmentHandler>> moduleAndAssignments ) {
		for (Map.Entry<Module, List<AssignmentHandler>> entry : moduleAndAssignments.entrySet()) {
			List<Assignment> assignments = new ArrayList<Assignment>();
			for (AssignmentHandler assignmentHandler : entry.getValue()) {
				if (assignmentHandler.getIsInfected()) {
					assignments.add(assignmentHandler.getAssignment());
				}
			}
			if (!assignments.isEmpty()) {
				Module module = entry.getKey();
				moduleAndBrokenAssignments.put(module, assignments);
				modulesToCheck.add(module);
			}
		}
	}

	public void processStartModules(final List<Module> startModules, final Map<Module, List<AssignmentHandler>> moduleAndBrokenAssignments) {
		for (Module startModule : startModules) {
			if(System.nanoTime()-start > TIMELIMIT) {
				return;
			}
			if (startModule instanceof TTCN3Module && startModule.getLastCompilationTimeStamp() != null) {
				Assignments startAssignments = startModule.getAssignments();
				List<AssignmentHandler> brokens = new ArrayList<AssignmentHandler>();
				List<AssignmentHandler> notBrokens = new ArrayList<AssignmentHandler>();
				for (int d = 0; d < startAssignments.getNofAssignments(); ++d) {
					Assignment startAssignment = startAssignments.getAssignmentByIndex(d);
					AssignmentHandler assignmentHandler = AssignmentHandlerFactory.getDefinitionHandler(startAssignment);
					boolean wasNull = false;
					if (startAssignment.getLastTimeChecked() == null) {
						wasNull = true;
						startAssignment.check(timestamp);
					}
					startAssignment.accept(assignmentHandler);
					if (wasNull) {
						assignmentHandler.initStartParts();
						assignmentHandler.addReason("Definition's CompilationTimeStamp is null, because of incremental parsing.");
						brokens.add(assignmentHandler);
					} else if (assignmentHandler.getIsInfected()) {
						assignmentHandler.addReason("Definition contains an infected reference.");
						brokens.add(assignmentHandler);
					} else {
						notBrokens.add(assignmentHandler);
					}
				}

				if (!brokens.isEmpty()) {
					checkLocalAssignments(brokens, notBrokens);
					if (moduleAndBrokenAssignments.containsKey(startModule)) {
						moduleAndBrokenAssignments.get(startModule).addAll(brokens);
					} else {
						moduleAndBrokenAssignments.put(startModule, brokens);
					}
				}
			} else {
				startModule.check(timestamp);
				List<AssignmentHandler> startAssignments = getAssignmentsFrom(startModule);
				for (AssignmentHandler assignmentHandler : startAssignments) {
					assignmentHandler.initStartParts();
					assignmentHandler.addReason("Parent module's CompilationTimeStamp is null.");
				}
				if (moduleAndBrokenAssignments.containsKey(startModule)) {
					moduleAndBrokenAssignments.get(startModule).addAll(startAssignments);
				} else {
					moduleAndBrokenAssignments.put(startModule, startAssignments);
				}
			}
		}
	}
	
	public List<AssignmentHandler> getAssignmentsFrom(final Module module) {
		List<AssignmentHandler> assignmentHandlers = new ArrayList<AssignmentHandler>();
		Assignments assignments = module.getAssignmentsScope();
		for (int d = 0; d < assignments.getNofAssignments(); ++d) {
			Assignment assignment = assignments.getAssignmentByIndex(d);
			AssignmentHandler assignmentHandler = AssignmentHandlerFactory.getDefinitionHandler(assignment);
			assignment.accept(assignmentHandler);
			assignmentHandlers.add(assignmentHandler);
		}

		return assignmentHandlers;
	}

	protected void checkLocalAssignments(final List<AssignmentHandler> brokens, final List<AssignmentHandler> notBrokens) {

		if (brokens.isEmpty() || notBrokens.isEmpty()) {
			return;
		}
		
		HashMap<String, AssignmentHandler> brokenMap = new HashMap<String, AssignmentHandler>(brokens.size() + notBrokens.size());
		for(AssignmentHandler handler: brokens) {
			brokenMap.put(handler.getAssignment().getIdentifier().getDisplayName(), handler);
		}
		
		boolean proceed = true;
		while (proceed) {
			proceed = false;
			
			
			for (int i = notBrokens.size() -1; i >=0; --i) {
				AssignmentHandler notBroken = notBrokens.get(i);
				boolean found = false;
				for (String name : notBroken.getContagiousReferences()) {
					if(brokenMap.containsKey(name)) {
						notBroken.check(brokenMap.get(name));
						found = true;
						break;
					}
				}
				if(!found) {
					for (String name : notBroken.getNonContagiousReferences()) {
						if(brokenMap.containsKey(name)) {
							notBroken.check(brokenMap.get(name));
							found = true;
							break;
						}
					}
				}
				
				if(found) {
					proceed = true;
					notBrokens.remove(i);
					brokens.add(notBroken);
					brokenMap.put(notBroken.getAssignment().getIdentifier().getDisplayName(), notBroken);
				}
			}
		}
	}

	protected void writeDebugInfo(final Map<Module, List<AssignmentHandler>> moduleAndAssignments) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				TITANDebugConsole.println("  Detailed info:");

				for (Map.Entry<Module, List<AssignmentHandler>> entry : moduleAndAssignments.entrySet()) {
					List<AssignmentHandler> values = entry.getValue();

					TITANDebugConsole.println("    module: " + entry.getKey().getIdentifier().getDisplayName());

					for (AssignmentHandler assignmentHandler : values) {
						TITANDebugConsole.println("         " + assignmentHandler + " | " + assignmentHandler.getReasons());
						if(assignmentHandler.getIsInfected() || assignmentHandler.getIsContagious()) {
							TITANDebugConsole.println("            " + (assignmentHandler.getIsInfected() ? "[+]" : "[-]") + " : infected");
							TITANDebugConsole.println("            " + (assignmentHandler.getIsContagious() ? "[+]" : "[-]") + " : contagious");
							TITANDebugConsole.println("            nonContagious references: " + assignmentHandler.getNonContagiousReferences());
							TITANDebugConsole.println("            contagious references: " + assignmentHandler.getContagiousReferences());
							TITANDebugConsole.println("            infected references: " + assignmentHandler.getInfectedReferences());
						}
					}
				}

				TITANDebugConsole.println("  in dot format:");
				TITANDebugConsole.println("  digraph {");
				TITANDebugConsole.println("    rankdir=LR;");

				ArrayList<Module> modules = new ArrayList<Module>(moduleAndAssignments.keySet());
				Collections.sort(modules, new Comparator<Module>() {

					@Override
					public int compare(final Module o1, final Module o2) {
						return o1.getName().compareTo(o2.getName());
					}
					
				});
				
				for (Module module : modules) {
					String moduleName = module.getName();
					TITANDebugConsole.println("    subgraph cluster_" + moduleName + " {");
					TITANDebugConsole.println("    label=\" " + module.getIdentifier().getDisplayName() + "\";");

					List<AssignmentHandler> values = moduleAndAssignments.get(module);

					for (AssignmentHandler assignmentHandler : values) {
						for(String reference: assignmentHandler.getInfectedReferences()) {
							TITANDebugConsole.println("      " + assignmentHandler.getAssignment().getIdentifier().getDisplayName() + "->" + reference + ";");
						}
					}
					TITANDebugConsole.println("    }");
				}
				TITANDebugConsole.println("  }");

			}
		});
	}
}