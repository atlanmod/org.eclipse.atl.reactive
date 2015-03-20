package org.eclipse.m2m.atl.reactive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.core.service.LauncherService;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMXMLReader;
import org.eclipse.m2m.atl.engine.emfvm.adapter.EMFModelAdapter;
import org.eclipse.m2m.atl.engine.emfvm.adapter.IModelAdapter;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.ITool;
import org.eclipse.m2m.atl.reactive.model.EMFLazyModel;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;

//TODO Add arguments to choose between transformation modes

public class ReactiveTransformationLauncher extends EMFVMLauncher {

	private boolean handleCustomNotification = true;
	private boolean isSerializable = false;
	public static final String LAUNCHER_NAME = "Reactive VM"; //$NON-NLS-1$
	public static final String MODEL_FACTORY_NAME = "EMFLazy"; //$NON-NLS-1$
	private Resource sourceResource;
	private Resource targetResource;
	private ILazyTransformation transformation = new EMFVMLazyTransformation();
	private IModel sourceModel = null;
	private IModel targetModel = null;

	private TransformationLogger logger;

	private EObject initialSourceElement;
	private LazyModelDynamicEObjectImpl initialTargetElement;

	public ReactiveTransformationLauncher() {
		super();
		this.isSerializable = false;
		this.logger = new TransformationLogger();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addInModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addInModel(IModel model, String name, String referenceModelName) {
		model.setIsTarget(false);
		if (this.sourceModel == null)
			this.sourceModel = model;
		addModel(model, name, referenceModelName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addOutModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addOutModel(IModel model, String name, String referenceModelName) {
		model.setIsTarget(true);
		if (this.targetModel == null)
			this.targetModel = model;
		addModel(model, name, referenceModelName);
	}

	/**
	 * Launches the transformation with preloaded modules.
	 * 
	 * @param tools
	 *            the execution tools
	 * @param monitor
	 *            the progression monitor
	 * @param options
	 *            the launching options
	 * @param modules
	 *            the transformation modules
	 * @return the execution result
	 */
	protected Object internalLaunch(ITool[] tools,
			final IProgressMonitor monitor, final Map<String, Object> options,
			Object... modules) {
		List<ASM> superimpose = new ArrayList<ASM>();
		ASM mainModule = getASMFromObject(modules[0]);
		for (int i = 1; i < modules.length; i++) {
			superimpose.add(getASMFromObject(modules[i]));
		}
		IModelAdapter modelAdapter;

		modelAdapter = new EMFModelAdapter();

		modelAdapter.setAllowInterModelReferences(LauncherService
				.getBooleanOption(
						options.get("allowInterModelReferences"), false)); //$NON-NLS-1$ 	
		// return mainModule.run(tools, models, libraries, superimpose, options,
		// monitor, modelAdapter);
		transformation.setModels(models);
		transformation.init(mainModule, false);

		initialSourceElement = ((EMFModel) sourceModel).getResource()
				.getContents().get(0);

		// TODO Check setBusy
		((EMFVMLazyTransformation) getTransformation()).setBusy(true);
		getTransformation().call("elementCreated", initialSourceElement);
		((EMFVMLazyTransformation) getTransformation()).setBusy(false);

		initialTargetElement = (LazyModelDynamicEObjectImpl) ((EMFLazyModel) targetModel)
				.getResource().getContents().get(0);

		// HERE WE START WORKING WITH THE MODELS

		// Add target model adapter
		if (this.isSerializable()) {
			getInitialTargetElement().eAdapters().add(
					new StandardAndSaveTargetAdapter(
							(EMFVMLazyTransformation) transformation,
							initialTargetElement));
		} else {
			StandardTargetAdapter sta = new StandardTargetAdapter(
					(EMFVMLazyTransformation) transformation);
			sta.setHandleCustomNotification(false);
			getInitialTargetElement().eAdapters().add(sta);
			sta.setHandleCustomNotification(true);
		}
		// Add source model adapter
		getInitialSourceElement().eAdapters().add(
				new StandardSourceAdapter(
						(EMFVMLazyTransformation) transformation));
		
//		Display.getDefault().syncExec(new Runnable() {
//			public void run() {
//				try {
//					PlatformUI.getWorkbench().getWorkbenchWindows()[0]
//							.getPages()[0]
//							.openEditor(new URIEditorInput(
//									((EMFModel) sourceModel).getResource()
//											.getURI()),
//									"org.eclipse.emf.ecore.presentation.ReflectiveEditorID");
//				} catch (PartInitException e) {
//					e.printStackTrace();
//				}
//			}
//		});

		return null;
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

		transformation.init(
				new ASMXMLReader().read(new BufferedInputStream(is)), false);

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

		transformation.init(
				new ASMXMLReader().read(new BufferedInputStream(is)), false);

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
		//getInitialSourceElement().eAdapters().add(
		//		new StandardSourceAdapter(
		//				(EMFVMLazyTransformation) getTransformation()));
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

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#getDefaultModelFactoryName()
	 */
	public String getDefaultModelFactoryName() {
		return MODEL_FACTORY_NAME;
	}

}
