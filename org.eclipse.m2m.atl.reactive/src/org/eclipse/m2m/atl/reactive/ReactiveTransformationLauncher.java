package org.eclipse.m2m.atl.reactive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;

//TODO Add arguments to choose between transformation modes

public class ReactiveTransformationLauncher {

	private boolean handleCustomNotification = true;
	private boolean isSerializable = false;
	protected String name;
	private Resource sourceResource;
	private Resource targetResource;
	private ILazyTransformation transformation = new EMFVMLazyTransformation();

	private TransformationLogger logger;

	private EObject initialSourceElement;
	private LazyModelDynamicEObjectImpl initialTargetElement;

	public ReactiveTransformationLauncher(boolean serialize) {
		this("One-to-one transformation test", serialize);
	}

	public ReactiveTransformationLauncher(String name, boolean serialize) {
		this.name = name;
		this.isSerializable = serialize;
		this.logger = new TransformationLogger();
	}

	public void initialize(String sourceMetamodelPath,
			String sourceMetamodelName, String targetMetamodelPath,
			String targetMetamodelName, String sourceModelPath,
			String targetModelPath, String asmPath)
			throws FileNotFoundException {

		ResourceSet resourceSet = new ResourceSetImpl();

		// Registering source metamodel
		File MM1 = new File(sourceMetamodelPath);
		URI fileUri = URI.createFileURI(MM1.getAbsolutePath());
		Resource MMResource = resourceSet.getResource(fileUri, true);
		ReactiveTransformationLauncher
				.init(MMResource.getURI().toString(), MM1);
		Resource sourceReferenceResource = resourceSet.getResource(
				URI.createURI(sourceMetamodelName), false);

		// Registering target metamodel
		File MM2 = new File(targetMetamodelPath);
		fileUri = URI.createFileURI(MM1.getAbsolutePath());
		Resource MM2Resource = resourceSet.getResource(fileUri, true);
		ReactiveTransformationLauncher.init(MM2Resource.getURI().toString(),
				MM2);

		// Open source model
		fileUri = URI
				.createFileURI(new File(sourceModelPath).getAbsolutePath());
		if (sourceResource == null) {
			sourceResource = resourceSet.getResource(fileUri, true);
		}
		// sourceResource = resourceSet.getResource(fileUri, true);

		// Open target model
		URI uri = URI.createURI(targetModelPath);
		targetResource = resourceSet.createResource(uri);

		// Fill transformation
		transformation.addReferenceModel(sourceMetamodelName,
				sourceReferenceResource);
		transformation
				.addSourceModel("IN", sourceMetamodelName, sourceResource);
		transformation.addReferenceModel(targetMetamodelName, resourceSet
				.getResource(URI.createURI(targetMetamodelName), false));
		transformation.addTargetModel("OUT", targetMetamodelName,
				targetResource);

		File asmFile = new File(asmPath);
		InputStream is = new FileInputStream(asmFile);

		transformation.init(is, true);

		initialSourceElement = getSourceResource().getContents().get(0);

		// TODO Check setBusy
		((EMFVMLazyTransformation) getTransformation()).setBusy(true);
		getTransformation().call("elementCreated", initialSourceElement);
		((EMFVMLazyTransformation) getTransformation()).setBusy(false);

		initialTargetElement = (LazyModelDynamicEObjectImpl) getTargetResource()
				.getContents().get(0);

		// HERE WE START WORKING WITH THE MODELS

		// Add target model adapter
		if (this.isSerializable()) {
			getInitialTargetElement().eAdapters().add(
					new StandardAndSaveTargetAdapter(
							(EMFVMLazyTransformation) getTransformation(),
							initialTargetElement));
		} else {
			StandardTargetAdapter sta = new StandardTargetAdapter(
					(EMFVMLazyTransformation) getTransformation());
			sta.setHandleCustomNotification(false);
			getInitialTargetElement().eAdapters().add(sta);
			sta.setHandleCustomNotification(true);
		}
		// Add source model adapter
		getInitialSourceElement().eAdapters().add(
				new StandardSourceAdapter(
						(EMFVMLazyTransformation) getTransformation()));
	}

	/**
	 * Initialize version for chaining. Here we pass the already loaded EMFModel
	 * for the source.
	 */
	public void initialize(String sourceMetamodelPath,
			String sourceMetamodelName, String targetMetamodelPath,
			String targetMetamodelName, String sourceModelPath,
			String targetModelPath, String asmPath,
			EMFReferenceModel sourceReferenceModel, EMFModel sourceModel)
			throws FileNotFoundException {

		ResourceSet resourceSet = new ResourceSetImpl();

		// Registering source metamodel
		File MM1 = new File(sourceMetamodelPath);
		URI fileUri = URI.createFileURI(MM1.getAbsolutePath());
		Resource MMResource = resourceSet.getResource(fileUri, true);
		ReactiveTransformationLauncher
				.init(MMResource.getURI().toString(), MM1);

		// Registering target metamodel
		File MM2 = new File(targetMetamodelPath);
		fileUri = URI.createFileURI(MM2.getAbsolutePath());
		Resource MM2Resource = resourceSet.getResource(fileUri, true);
		ReactiveTransformationLauncher.init(MM2Resource.getURI().toString(),
				MM2);

		// Open source model
		fileUri = URI
				.createFileURI(new File(sourceModelPath).getAbsolutePath());
		if (sourceResource == null) {
			sourceResource = resourceSet.getResource(fileUri, true);
		}
		// sourceResource = resourceSet.getResource(fileUri, true);

		// Open target model
		URI uri = URI.createURI(targetModelPath);
		targetResource = resourceSet.createResource(uri);

		// Fill transformation
		// transformation.addReferenceModel(sourceMetamodelName,
		// sourceReferenceResource);

		((EMFVMLazyTransformation) transformation).addLoadedReferenceModel(
				sourceMetamodelName, sourceReferenceModel);

		((EMFVMLazyTransformation) transformation).addSourceLoadedModel("IN",
				sourceModel);

		transformation.addReferenceModel(targetMetamodelName, MM2Resource);
		transformation.addTargetModel("OUT", targetMetamodelName,
				targetResource);

		File asmFile = new File(asmPath);
		InputStream is = new FileInputStream(asmFile);

		transformation.init(is, false);

		initialSourceElement = getSourceResource().getContents().get(0);

		// TODO Check setBusy
		((EMFVMLazyTransformation) getTransformation()).setBusy(true);
		getTransformation().call("elementCreated", initialSourceElement);
		((EMFVMLazyTransformation) getTransformation()).setBusy(false);

		initialTargetElement = (LazyModelDynamicEObjectImpl) getTargetResource()
				.getContents().get(0);

		// HERE WE START WORKING WITH THE MODELS

		// Add target model adapter
		if (this.isSerializable()) {
			getInitialTargetElement().eAdapters().add(
					new StandardAndSaveTargetAdapter(
							(EMFVMLazyTransformation) getTransformation(),
							initialTargetElement));
		} else {
			getInitialTargetElement().eAdapters().add(
					new StandardTargetAdapter(
							(EMFVMLazyTransformation) getTransformation()));
		}
		// Add source model adapter
		getInitialSourceElement().eAdapters().add(
				new StandardSourceAdapter(
						(EMFVMLazyTransformation) getTransformation()));
	}

	private static void init(String metamodelURI, File file) {
		Resource.Factory myEcoreFactory = new EcoreResourceFactoryImpl();
		Resource mmExtent = myEcoreFactory.createResource(URI
				.createURI(metamodelURI));
		try {
			mmExtent.load(new FileInputStream(file), Collections.EMPTY_MAP);
		} catch (IOException e) {
			System.out.println("Failing");
			e.printStackTrace();
		}

		// Gets all packages from the MM, sets their nsURI and puts them in the
		// registry.
		for (Iterator<EObject> it = getElementsByType(mmExtent, "EPackage")
				.iterator(); it.hasNext();) {
			EPackage p = (EPackage) it.next();
			String nsURI = p.getNsURI();
			if (nsURI == null) {
				nsURI = p.getName();
				p.setNsURI(nsURI);
			}
			EPackage.Registry.INSTANCE.put(nsURI, p);
		}

		// For all the EDataTypes sets instanceClassName = name if it was null.
		for (Iterator<EObject> it = getElementsByType(mmExtent, "EDataType")
				.iterator(); it.hasNext();) {
			EObject eo = it.next();
			EStructuralFeature sf = eo.eClass().getEStructuralFeature("name");
			EStructuralFeature isf = eo.eClass().getEStructuralFeature(
					"instanceClassName");
			String tname = (String) eo.eGet(sf);
			String icn = (String) eo.eGet(isf);
			if (icn == null) {
				if (tname.equals("Boolean")) {
					icn = "boolean";
				} else if (tname.equals("Double") || tname.equals("Real")) {
					icn = "java.lang.Double";
				} else if (tname.equals("Float")) {
					icn = "java.lang.Float";
				} else if (tname.equals("Integer")) {
					icn = "java.lang.Integer";
				} else if (tname.equals("String")) {
					icn = "java.lang.String";
				}
				if (icn != null) {
					sf = eo.eClass().getEStructuralFeature("instanceClassName");
					eo.eSet(sf, icn);
				}
			}
		}
	}

	private static Set<EObject> getElementsByType(Resource extent, String type) {
		Set<EObject> ret = new HashSet<EObject>();
		for (Iterator<?> i = extent.getAllContents(); i.hasNext();) {
			EObject eo = (EObject) i.next();
			if (eo.eClass().getName().equals(type)) {
				ret.add(eo);
			}
		}
		return ret;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public TransformationLogger getLogger() {
		return logger;
	}

	public void setLogger(TransformationLogger logger) {
		this.logger = logger;
	}

	public Resource getSourceResource() {
		return sourceResource;
	}

	public void setSourceResource(Resource sourceResource) {
		this.sourceResource = sourceResource;
	}

	public Resource getTargetResource() {
		return targetResource;
	}

	public ILazyTransformation getTransformation() {
		return transformation;
	}

	public EObject getInitialSourceElement() {
		return initialSourceElement;
	}

	public LazyModelDynamicEObjectImpl getInitialTargetElement() {
		return initialTargetElement;
	}

	public void setHandleCustomNotification(boolean handleCustomNotification) {
		this.handleCustomNotification = handleCustomNotification;
	}

	public boolean isHandleCustomNotification() {
		return handleCustomNotification;
	}

	public boolean isSerializable() {
		return isSerializable;
	}

}
