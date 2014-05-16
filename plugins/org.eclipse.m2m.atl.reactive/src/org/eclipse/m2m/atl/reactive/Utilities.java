package org.eclipse.m2m.atl.reactive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class Utilities {

	public static void init(String metamodelURI, File file) {
		Resource.Factory myEcoreFactory = new EcoreResourceFactoryImpl();
		Resource mmExtent = myEcoreFactory.createResource(URI
				.createURI(metamodelURI));
		try {
			mmExtent.load(new FileInputStream(file), Collections.EMPTY_MAP);
		} catch (IOException e) {
			// MessageDialog.openError(shell,
			//	Messages.getString("RegisterMetamodel.REGISTER_FAIL"), e.getMessage()); //$NON-NLS-1$
			System.out.println("Failing");
			e.printStackTrace();
		}

		// Gets all packages from the MM, sets their nsURI and puts them in the
		// registry.
		for (Iterator<EObject> it = getElementsByType(mmExtent, "EPackage").iterator(); it.hasNext();) { //$NON-NLS-1$
			EPackage p = (EPackage) it.next();
			String nsURI = p.getNsURI();
			if (nsURI == null) {
				nsURI = p.getName();
				p.setNsURI(nsURI);
			}
			EPackage.Registry.INSTANCE.put(nsURI, p);
		}

		// For all the EDataTypes sets instanceClassName = name if it was null.
		for (Iterator<EObject> it = getElementsByType(mmExtent, "EDataType").iterator(); it.hasNext();) { //$NON-NLS-1$
			EObject eo = it.next();
			EStructuralFeature sf = eo.eClass().getEStructuralFeature("name"); //$NON-NLS-1$
			EStructuralFeature isf = eo.eClass().getEStructuralFeature(
					"instanceClassName"); //$NON-NLS-1$
			String tname = (String) eo.eGet(sf);
			String icn = (String) eo.eGet(isf);
			if (icn == null) {
				if (tname.equals("Boolean")) { //$NON-NLS-1$
					icn = "boolean"; //$NON-NLS-1$
				} else if (tname.equals("Double") || tname.equals("Real")) { //$NON-NLS-1$ //$NON-NLS-2$
					icn = "java.lang.Double"; //$NON-NLS-1$
				} else if (tname.equals("Float")) { //$NON-NLS-1$
					icn = "java.lang.Float"; //$NON-NLS-1$
				} else if (tname.equals("Integer")) { //$NON-NLS-1$
					icn = "java.lang.Integer"; //$NON-NLS-1$
				} else if (tname.equals("String")) { //$NON-NLS-1$
					icn = "java.lang.String"; //$NON-NLS-1$
				}
				if (icn != null) {
					sf = eo.eClass().getEStructuralFeature("instanceClassName"); //$NON-NLS-1$
					eo.eSet(sf, icn);
				}
			}
		}
	}

	/**
	 * @param extent
	 * @param type
	 * @return set of Objects of that type (ex: EPackage, EDataType)
	 */
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

}
