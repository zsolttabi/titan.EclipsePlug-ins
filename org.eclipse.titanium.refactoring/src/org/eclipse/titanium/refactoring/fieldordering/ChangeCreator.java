package org.eclipse.titanium.refactoring.fieldordering;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link OrderFieldNamesRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Zsolt Tabi
 */
class ChangeCreator {
	// in
	private final IFile selectedFile;

	// out
	private Change change;

	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if (module == null) {
			return null;
		}
		// find all locations in the module that should be edited
		final DefinitionVisitor vis = new DefinitionVisitor();
		module.accept(vis);
		final NavigableSet<ILocateableNode> nodes = vis.getLocations();

		if (nodes.isEmpty()) {
			return null;
		}

		// create a change for each edit location
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);

		for (ILocateableNode node : nodes) {
			
			if (node instanceof Sequence_Value) {
				orderSequence_Value((Sequence_Value) node, rootEdit);
			} else if (node instanceof SequenceOf_Value) {
				orderSequenceOf_Value((SequenceOf_Value) node, rootEdit);
			}
				
		}

		if (!rootEdit.hasChildren()) {
			return null;
		}

		return tfc;
	}
	

	private static void orderSequence_Value(Sequence_Value sequence_Value, MultiTextEdit rootEdit) {
		
		IType type = sequence_Value.getMyGovernor();
		if (!(type instanceof Referenced_Type)) {
			return;
		}
		
		IType refType = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (!(refType instanceof TTCN3_Set_Type)) {
			return;
		}
		
		TTCN3_Set_Type setType = (TTCN3_Set_Type) refType;
		
		if (sequence_Value.getNofComponents() == 0) {
			return;
		}
		
		if (sequence_Value.getSeqValueByIndex(0) == null) { // record with unnamed fields
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (int i = 0; i < setType.getNofComponents(); ++i) {
			
			Identifier identifier = setType.getComponentIdentifierByIndex(i);
			NamedValue componentByName = sequence_Value.getComponentByName(identifier);
			
			if (componentByName == null) { // no value defined
				continue;
			}
			
			if (isFirst) { // we don't always have 0-indexed element
				isFirst = false;
			} else {
				builder.append(", ");
			}
	
			builder.append(identifier.getDisplayName()).append(" := ").append(componentByName.getValue());

		}
				
		int seqStartOffset = sequence_Value.getSeqValueByIndex(0).getLocation().getOffset();
		int seqEndOffset = sequence_Value.getSeqValueByIndex(sequence_Value.getNofComponents()-1).getLocation().getEndOffset();
		rootEdit.addChild(new ReplaceEdit(seqStartOffset, seqEndOffset - seqStartOffset, builder.toString()));		

	}
	
	private static void orderSequenceOf_Value(SequenceOf_Value sequenceOf_Value, MultiTextEdit rootEdit) {

		if (!sequenceOf_Value.isIndexed()) { // ordered
			return;
		}

		if (sequenceOf_Value.getNofComponents() == 0) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		Long maxIndex = getIndexUpperBound(sequenceOf_Value);
		if (maxIndex == null) {
			return;
		}
		for (long i = 0; i < maxIndex; ++i) {

			long realIndex = i + 1;
			IValue indexedValueByRealIndex = sequenceOf_Value.getValues().getIndexedValueByRealIndex((int) realIndex);

			if (indexedValueByRealIndex == null) { // no value defined
				continue;
			}

			if (isFirst) { // we don't always have 0-indexed element
				isFirst = false;
			} else {
				builder.append(", ");
				builder.append("[");
			}

			builder.append(realIndex).append("] := ").append(indexedValueByRealIndex);

		}

		int seqStartOffset = sequenceOf_Value.getIndexByIndex(0).getLocation().getOffset();
		int seqEndOffset = sequenceOf_Value.getValueByIndex(sequenceOf_Value.getNofComponents() - 1).getLocation()
				.getEndOffset();
		rootEdit.addChild(new ReplaceEdit(seqStartOffset, seqEndOffset - seqStartOffset, builder.toString()));
	}
	
	private static Long getIndexUpperBound(SequenceOf_Value sequenceOf_Value) {
		long result = 0;
		for (int i = 0; i < sequenceOf_Value.getNofComponents(); i++) {
			
			IValue indexByIndex = sequenceOf_Value.getIndexByIndex(i);
			if (indexByIndex.getValuetype() != Value_type.INTEGER_VALUE) {
				return null;
			}
			
			long value = ((Integer_Value)indexByIndex).getValue();
			if (value > result) {
				result = value;
			}
		}
		return result;
	}
	
	/**
	 * Collects the locations of all the definitions in a module where the visibility modifier
	 *  is not yet minimal.
	 * <p>
	 * Call on modules.
	 * */

	private static class DefinitionVisitor extends ASTVisitor {

		private final NavigableSet<ILocateableNode> locations;

		DefinitionVisitor() {
			locations = new TreeSet<ILocateableNode>(new LocationComparator());
		}

		private NavigableSet<ILocateableNode> getLocations() {
			return locations;
		}

		@Override
		public int visit(final IVisitableNode node) {
						
			if (node instanceof Sequence_Value) {
				locations.add((Sequence_Value) node);
			} else if (node instanceof SequenceOf_Value) {
				locations.add((SequenceOf_Value) node);
			}
		
			
			return V_CONTINUE;
		}
	}

	/**
	 * Compares {@link ILocateableNode}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private static class LocationComparator implements Comparator<ILocateableNode> {

		@Override
		public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
			final IResource f0 = arg0.getLocation().getFile();
			final IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}

			final int o0 = arg0.getLocation().getOffset();
			final int o1 = arg1.getLocation().getOffset();
			return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
		}

	}

}
