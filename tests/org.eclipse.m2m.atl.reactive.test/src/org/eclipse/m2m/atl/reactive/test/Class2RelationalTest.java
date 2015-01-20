package org.eclipse.m2m.atl.reactive.test;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.reactive.ReactiveTransformationLauncher;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;

public class Class2RelationalTest {
	
	private static final String ENCODING_PROPERTY = "file.encoding"; //$NON-NLS-1$

	ReactiveTransformationLauncher class2relational;

	@SuppressWarnings("unchecked")
	public void run() {
		
	
		// Initial elements
		EObject classRoot = class2relational.getInitialSourceElement();
		LazyModelDynamicEObjectImpl relationalRoot = class2relational
				.getInitialTargetElement();

		// We get on-demand the schemas of a system
		System.out
				.println("The lazily-computed top-level elements of the Relational model are:");
		EList<EObject> el = (EList<EObject>) relationalRoot.eContents();
		//EList<EObject> el = (EList<EObject>) relationalRoot.eGet(relationalRoot
				//.eClass().getEStructuralFeature("schemas"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}

		// Now, we add a package to the class diagram. This invalidates the
		// corresponding features of the relational model so that
		// they will be read again in the next access
		EStructuralFeature packages = classRoot.eClass().getEStructuralFeature(
				"packages");
		EFactory f = classRoot.eClass().getEPackage().getEFactoryInstance();
		EClass packageClass = (EClass) classRoot.eClass().getEPackage()
				.getEClassifier("Package");

		EObject newPackage = f.create(packageClass);
		newPackage
				.eSet(newPackage.eClass().getEStructuralFeature("name"), "p2");
		((EList<EObject>) classRoot.eGet(packages)).add(newPackage);

		// We get again on-demand the schemas of a system
		System.out
				.println("After adding another package to the Class model, the top-level elements of the Relational model are:");
		el = (EList<EObject>) relationalRoot.eContents();
		//el = (EList<EObject>) relationalRoot.eGet(relationalRoot.eClass()
		//		.getEStructuralFeature("schemas"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
		
		try {
			relationalRoot.eResource().save(null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void save(EObject root, String path) throws IOException {
		final URI modelURI = URI.createURI(path);
		ResourceSet resourceSet = root.eResource().getResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		final Resource newModelResource = resourceSet.createResource(modelURI);
		newModelResource.getContents().add(root);
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
		newModelResource.save(options);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Class2RelationalTest t = new Class2RelationalTest();

			t.class2relational = new ReactiveTransformationLauncher();

			t.class2relational.initialize(
					"data/ClassDiagram/ClassDiagram.ecore", "ClassDiagram",
					"data/Relational/Relational.ecore", "Relational",
					"data/ClassDiagram/Sample-ClassDiagram.xmi",
					"data/Relational/Sample-Relational.xmi",
					"data/ClassDiagram2Relational/ClassDiagram2Relational.asm");

			t.run();
			//es.shutdown();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
