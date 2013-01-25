package org.eclipse.m2m.atl.reactive;

import java.io.InputStream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public interface ILazyTransformation {
	
	void addReferenceModel(String name, Resource resource);
	void addSourceModel(String name, String referenceModelName, Resource resource);
	void addTargetModel(String name, String referenceModelName, Resource resource);
	void init(InputStream bytecode, boolean step);

	void call(String operationName, EObject source);
	void call(String operationName, EObject source, Object arguments[]);

}
