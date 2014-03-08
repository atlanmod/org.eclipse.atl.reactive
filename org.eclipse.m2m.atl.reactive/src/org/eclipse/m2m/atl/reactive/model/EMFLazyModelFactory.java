package org.eclipse.m2m.atl.reactive.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;

public class EMFLazyModelFactory extends EMFModelFactory {
	
	/** The model factory name which is also the extractor/injector name. */
	public static final String MODEL_FACTORY_NAME = "EMFLazy"; //$NON-NLS-1$
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.ModelFactory#newModel(org.eclipse.m2m.atl.core.IReferenceModel,
	 *      java.util.Map)
	 */
	@Override
	public IModel newModel(IReferenceModel referenceModel, Map<String, Object> options) {
		return newModel(referenceModel);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.ModelFactory#newModel(org.eclipse.m2m.atl.core.IReferenceModel)
	 */
	@Override
	public IModel newModel(IReferenceModel referenceModel) {
		return new EMFLazyModel((EMFReferenceModel)referenceModel, this);
	}

	/**
	 * Creates a new {@link EMFLazyModel} using the given uri.
	 * 
	 * @param referenceModel
	 *            the {@link IReferenceModel}
	 * @param uri
	 *            the model uri
	 * @return a new {@link IModel}
	 */
	public EMFLazyModel newModel(EMFReferenceModel referenceModel, String uri) {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(OPTION_URI, uri);
		return (EMFLazyModel)newModel(referenceModel, options);
	}


}
