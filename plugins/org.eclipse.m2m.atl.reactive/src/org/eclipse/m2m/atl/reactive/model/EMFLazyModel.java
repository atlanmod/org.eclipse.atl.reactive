package org.eclipse.m2m.atl.reactive.model;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;

public class EMFLazyModel extends EMFModel{
	
	private LazyModelFactory lazyModelFactory;
	private EMFModelFactory modelFactory;

	public EMFLazyModel(EMFReferenceModel referenceModel, EMFModelFactory mf) {
		super(referenceModel, mf);
		if (getResource()==null) {
			ResourceSet resourceSet = new ResourceSetImpl();
			URI uri = URI.createURI("temp.xmi");
			setResource(resourceSet.createResource(uri));
		}
	}
	
	/**
	 * We override the creation of new elements to use a factory producing lazy EObjects
	 */
	@Override
	public Object newElement(Object metaElement) {
		Resource mainResource = getResource();
		if (mainResource == null) {
			mainResource = modelFactory.getResourceSet().createResource(URI.createURI("new-model")); //$NON-NLS-1$
			// TODO [Resource.Factory issues] use the correct factory
			// MAIN ISSUE HERE...
			// resource must be created within the model creation
			setResource(mainResource);
			EClass ec = (EClass)metaElement;
			lazyModelFactory = new LazyModelFactory();
			ec.getEPackage().setEFactoryInstance(lazyModelFactory);
		}
		
		EClass ec = (EClass)metaElement;
		
		if (ec.getEPackage().getEFactoryInstance().getClass().equals(EFactoryImpl.class)){
			lazyModelFactory = new LazyModelFactory();
			ec.getEPackage().setEFactoryInstance(lazyModelFactory);
		}
		
		EObject ret = null;
		ret = ec.getEPackage().getEFactoryInstance().create(ec);
		//TODO:
		//For now, we are going to hack here our new model factory.
		//We will see later a better way to do this 
		mainResource.getContents().add(ret);
		return ret;
	}
	
}
