package org.eclipse.m2m.atl.reactive;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.engine.emfvm.ASM;

public interface ILazyTransformation {
	
	void addReferenceModel(String name, Resource resource);
	void addSourceModel(String name, String referenceModelName, Resource resource);
	void addTargetModel(String name, String referenceModelName, Resource resource);
	void init(ASM asm, boolean step);

	void call(String operationName, EObject source);
	void call(String operationName, EObject source, Object arguments[]);

	public void setModels(Map<String, IModel> models);
}
