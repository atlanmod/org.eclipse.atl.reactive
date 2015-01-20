package org.eclipse.m2m.atl.reactive.test;

import java.io.FileNotFoundException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.m2m.atl.reactive.EMFVMLazyTransformation;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;
import org.eclipse.m2m.atl.reactive.FlagChangeForwardingAdapter;
import org.eclipse.m2m.atl.reactive.ReactiveTransformationLauncher;

public class Class2Relational2XMLTest {

	ReactiveTransformationLauncher class2relational;
	ReactiveTransformationLauncher relational2xml;

	@SuppressWarnings("unchecked")
	public void run() {

		// Initial elements
		EObject classRoot = class2relational.getInitialSourceElement();
		LazyModelDynamicEObjectImpl relationalRoot = class2relational
				.getInitialTargetElement();
		LazyModelDynamicEObjectImpl xmlRoot = relational2xml
				.getInitialTargetElement();

		// We add here the adapter to forward the invalidation of properties
		relationalRoot.eAdapters().add(
				new FlagChangeForwardingAdapter(
						(EMFVMLazyTransformation) relational2xml
								.getTransformation()));

		// We get on-demand the schemas of a system and then the xml elements
		// that will represent them in a document
		System.out
				.println("The lazily-computed top-level elements of the XML model are:");
		EList<EObject> el = (EList<EObject>) xmlRoot.eGet(xmlRoot.eClass()
				.getEStructuralFeature("children"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}

		// Now, we add a package to the class diagram. This invalidates the
		// corresponding features of the relational and the xml model so that
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

		// We get on-demand the schemas of a system and then the xml elements
		// that
		// will represent them in a document
		System.out
				.println("After adding another package to the Class model, the top-level elements of the XML model are:");
		el = (EList<EObject>) xmlRoot.eGet(xmlRoot.eClass()
				.getEStructuralFeature("children"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Class2Relational2XMLTest t = new Class2Relational2XMLTest();

			t.class2relational = new ReactiveTransformationLauncher();

			t.class2relational.initialize(
					"data/ClassDiagram/ClassDiagram.ecore", "ClassDiagram",
					"data/Relational/Relational.ecore", "Relational",
					"data/ClassDiagram/Sample-ClassDiagram.xmi",
					"data/Relational/Sample-Relational.xmi",
					"data/ClassDiagram2Relational/ClassDiagram2Relational.asm");

			// Here the path for the relational model of st2 has to be the same
			// path of the out model of st
			t.relational2xml = new ReactiveTransformationLauncher();

			t.relational2xml.setSourceResource(t.class2relational
					.getTargetResource());
			t.relational2xml.initialize("data/Relational/Relational.ecore",
					"Relational", "data/XML/xml.ecore", "xml",
					"data/Relational/Sample-Relational.xmi",
					"data/XML/Sample-XML.xmi",
					"data/Relational2XML/Relational2XML.asm",
					((EMFVMLazyTransformation) t.class2relational
							.getTransformation())
							.getReferenceModel("Relational"),
					((EMFVMLazyTransformation) t.class2relational
							.getTransformation()).getTargetModel("OUT"));

			t.run();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
