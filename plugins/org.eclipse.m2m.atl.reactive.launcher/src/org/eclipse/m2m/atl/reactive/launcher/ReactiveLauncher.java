package org.eclipse.m2m.atl.reactive.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.m2m.atl.reactive.EMFVMLazyTransformation;
import org.eclipse.m2m.atl.reactive.IncrementalEagerProgapationAdapter;
import org.eclipse.m2m.atl.reactive.ReactiveTransformationLauncher;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;
import org.eclipse.m2m.atl.reactive.model.LazyModelFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 */
public class ReactiveLauncher extends EMFVMLauncher {
	ReactiveTransformationLauncher launcher = new ReactiveTransformationLauncher();

	private EMFModel sourceModel;

	private Resource sourceResource;

	private String sourceMetaModelName;

	private EMFModel targetModel;

	private Resource targetResource;

	private String targetMetaModelName;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.launch.ILauncher#addInModel(org.eclipse.m2m.atl.core.IModel,
	 *      java.lang.String, java.lang.String)
	 */
	public void addInModel(IModel model, String name, String referenceModelName) {
		model.setIsTarget(false);
		if (this.sourceModel == null) {
			this.sourceModel = (EMFModel)model;
			this.sourceResource = sourceModel.getResource();
		}
		if (this.sourceMetaModelName == null)
			this.sourceMetaModelName = referenceModelName;
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
		if (this.targetModel == null) {
			this.targetModel = (EMFModel)model;
			this.targetResource = targetModel.getResource();
		}
		if (this.targetMetaModelName == null)
			this.targetMetaModelName = referenceModelName;
		addModel(model, name, referenceModelName);
	}

	private void registerPackage(EPackage p) {
		System.out.println(p);
		String nsURI = p.getNsURI();
		if (nsURI == null) {
			nsURI = p.getName();
			p.setNsURI(nsURI);
		}
		EPackage.Registry.INSTANCE.put(nsURI, p);
	}

	private Set<EObject> getElementsByType(Resource extent, String type) {
		Set<EObject> ret = new HashSet<EObject>();
		for (Iterator<?> i = extent.getAllContents(); i.hasNext();) {
			EObject eo = (EObject)i.next();
			if (eo.eClass().getName().equals(type)) {
				ret.add(eo);
			}
		}
		return ret;
	}

	public Object launch(final String mode, final IProgressMonitor monitor,
			final Map<String, Object> options, final Object... modules) {
		try {
			// PATHS
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			File workspaceDirectory = workspace.getRoot().getLocation().toFile();
			
			String directoryPathname = workspaceDirectory.getAbsolutePath();

			String atlFilePath = workspaceDirectory.getAbsolutePath() + options.get("ATL_FILE").toString();
			String asmFilePath = atlFilePath.substring(0, atlFilePath.length() - 4) + ".asm";

			String targetUpdatedXMIFilePath = atlFilePath.substring(0, atlFilePath.length() - 4) + "-updated.xmi";
			String targetTargetXMIFilePath = atlFilePath.substring(0, atlFilePath.length() - 4) + "-target.xmi";
		
			// registering lazy model factory
			TreeIterator<EObject> ti = targetModel.getReferenceModel().getResource().getAllContents();
			EObject current;

			while (ti.hasNext()) {
				current = ti.next();
				if (current instanceof EPackage)
					((EPackage)current).setEFactoryInstance(new LazyModelFactory());
			}

			for (Iterator<EObject> it = getElementsByType(sourceModel.getReferenceModel().getResource(),
					"EPackage").iterator(); it.hasNext();) { //$NON-NLS-1$
				EPackage p = (EPackage)it.next();
				registerPackage(p);
			}
			
			for (Iterator<EObject> it = getElementsByType(targetModel.getReferenceModel().getResource(),
					"EPackage").iterator(); it.hasNext();) { //$NON-NLS-1$
				EPackage p = (EPackage)it.next();
				registerPackage(p);
			}
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						String atlFileRelativePath = options.get("ATL_FILE").toString();
						String targetXMIFilePath = "platform:/resource"
								+ atlFileRelativePath.substring(0, atlFileRelativePath.length() - 4)
								+ "-target.xmi";
						URI uri = URI.createURI(targetXMIFilePath);
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
						IEditorPart sourceEditor = IDE.openEditor(page, new URIEditorInput(
								((EMFModel)sourceModel).getResource().getURI()),
								"org.eclipse.emf.ecore.presentation.ReflectiveEditorID");
						IEditorPart targetEditor = IDE.openEditor(page, new URIEditorInput(uri),
								"org.eclipse.emf.ecore.presentation.ReflectiveEditorID");

						// extracting resources from editors
						sourceResource = ((EcoreEditor)sourceEditor).getEditingDomain().getResourceSet()
								.getResources().get(0);
						targetResource = ((EcoreEditor)targetEditor).getEditingDomain().getResourceSet()
								.getResources().get(0);
						sourceModel.setResource(sourceResource);
						targetModel.setResource(targetResource);

					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
			
			launcher.initialize(this.sourceMetaModelName, this.targetMetaModelName, sourceModel, targetModel, asmFilePath);
			traverseContainment(launcher.getInitialTargetElement());

			// Add the listener to the target model

			IncrementalEagerProgapationAdapter inc = new IncrementalEagerProgapationAdapter(
					(EMFVMLazyTransformation)launcher.getTransformation());
			inc.setHandleCustomNotification(false);
			launcher.getInitialTargetElement().eAdapters().add(inc);
			inc.setHandleCustomNotification(true);

			launcher.getInitialTargetElement()
					.eAdapters()
					.add(new ReactiveTargetAdapter((EMFVMLazyTransformation)launcher.getTransformation(), targetUpdatedXMIFilePath,
							launcher.getInitialTargetElement()));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void traverseContainment(LazyModelDynamicEObjectImpl e) {
		System.out.println("Instance of " + e.eClass().getName());
		for (EStructuralFeature a : e.eClass().getEAllStructuralFeatures()) {
			System.out.println(a.getName() + ":" + e.eGet(a));
		}
		for (EObject o : e.eContents())
			traverseContainment((LazyModelDynamicEObjectImpl)o);
	}

}
