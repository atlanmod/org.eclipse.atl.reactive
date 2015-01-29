package org.eclipse.m2m.atl.reactive.comparison;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EReferenceImpl;

public class ComparisonUtil {
	
	public static String getContainingClass(EObject diffElement){
		return "";
	}
	
	public static String getContainingBinding(EObject diffElement){
		return "";
	}
	
	public static String getHelperName(EObject helper){
		EObject definition = (EObject) helper.eGet(helper.eClass().getEStructuralFeature("definition"));
		EObject feature = (EObject) definition.eGet(definition.eClass().getEStructuralFeature("feature"));
		return (String) feature.eGet(feature.eClass().getEStructuralFeature("name"));
	}
	
	public static DiffObject getContainingBindingOrRule(EObject diffElement){
		EObject currentElement = diffElement;
		while (!currentElement.eClass().getName().equals("Binding") 
				&& !currentElement.eClass().getName().equals("MatchedRule")
				&& !currentElement.eClass().getName().equals("Helper")
				&& !currentElement.eClass().getName().equals("Module")){
			currentElement = currentElement.eContainer();
		}
		String objectName;
		if (currentElement.eClass().getName().equals("Binding")){
			DiffObject o = getContainingBindingOrRule(currentElement.eContainer());
			objectName = (String) currentElement.eGet(currentElement.eClass().getEStructuralFeature("propertyName"));
			o.setBindingName(objectName);
			o.setChangeKind(DiffObject.BINDING_CHANGE);
			return o;
		}else if (currentElement.eClass().getName().equals("Helper")){
			//FIXME
			//The name of the helper needs to be extracted from the model
			objectName = getHelperName(currentElement);
			return new DiffObject(objectName, DiffObject.HELPER_CHANGE);
		}else if (currentElement.eClass().getName().equals("Module")){
			objectName = (String) currentElement.eGet(currentElement.eClass().getEStructuralFeature("name"));
			return new DiffObject(objectName, DiffObject.MODULE_CHANGE);
		}else{
			objectName = (String) currentElement.eGet(currentElement.eClass().getEStructuralFeature("name"));
			return new DiffObject(objectName, DiffObject.RULE_CHANGE);
		}
	}
	
	public static List<DiffObject> getTransformationChanges(List<Diff> differences){
		List<DiffObject> transformationDiffs = new ArrayList<DiffObject>();
		EObject changeValue;
		for (Diff diff : differences) {
			if (diff.getMatch().getLeft() != null){
				changeValue = diff.getMatch().getLeft();
			}else{
				changeValue = diff.getMatch().getRight();
			}
			DiffObject dfo = getContainingBindingOrRule(changeValue);
			if (!duplicate(dfo,transformationDiffs)){
				transformationDiffs.add(dfo);
			}
		}
		return transformationDiffs;
	}
	
	public static List<Diff> filterDiffs(List<Diff> differences){
		List<Diff> relevantDiffs = new ArrayList<Diff>();
		for (Diff diff : differences) {
			// allow only certain kinds of diff elements
			if (diff instanceof ReferenceChange && ((ReferenceChange) diff).getKind() == DifferenceKind.CHANGE) {
				EReferenceImpl ref = (EReferenceImpl) ((ReferenceChange) diff).getReference();
				if (!ref.getName().equals("model") && !ref.getName().equals("metamodel")){
					//ModelUtils.assertEquals(diff.getMatch().getLeft(), diff.getMatch().getRight(), ((ReferenceChange) diff).getReference());
					relevantDiffs.add(diff);
				}
			} else if (diff instanceof AttributeChange){
				if (!((AttributeChange) diff).getAttribute().getName().equals("location")) {
					relevantDiffs.add(diff);
				}
			} 
		}
		return relevantDiffs;
	}
	
	public static List<Diff> calculateDifferences(EObject left, EObject right){
		DefaultComparisonScope scope = new DefaultComparisonScope(left, right, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		return comparison.getDifferences();
	}
	
	public static boolean duplicate(DiffObject e, List<DiffObject> list){
		for (DiffObject diff : list){
			if (diff.compareTo(e) == 0){
				return true;
			}
		}
		return false;
	}

}
