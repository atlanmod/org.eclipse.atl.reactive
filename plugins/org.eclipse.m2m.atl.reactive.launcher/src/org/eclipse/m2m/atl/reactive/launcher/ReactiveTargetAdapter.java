package org.eclipse.m2m.atl.reactive.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.m2m.atl.reactive.EMFVMLazyTransformation;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;
import org.eclipse.m2m.atl.reactive.model.LazyModelNotification;

/**
 * Target Reactive Adapter handles notification events produced by Lazy Model
 * {@link LazyModelDynamicEObjectImpl} elements, such as GET and FLAG_CHANGE events. Then, a given running
 * reactive transformation is used to react to such events, namely, launching the needed rules and bindings to
 * get the target element.
 * 
 * @author Salvador Martínez
 */
public class ReactiveTargetAdapter extends EContentAdapter {

	EMFVMLazyTransformation transformation;

	private boolean handleCustomNotification = true;

	private Map<String, Object> saveOptions = new HashMap<String, Object>();

	String updatedXMILocation;

	XMLResource.URIHandler uriHandler = new XMLResource.URIHandler() {

		public URI deresolve(URI uri) {
			// URI u = URI.createURI("");
			URI u = URI.createURI('#' + uri.fragment());
			return u;
		}

		public URI resolve(URI uri) {
			return null;
		}

		public void setBaseURI(URI uri) {
		}
	};

	public ReactiveTargetAdapter(EMFVMLazyTransformation transformation, String updatedXMILocation, EObject root) {
		super();
		this.transformation = transformation;
		saveOptions.put(XMLResource.OPTION_URI_HANDLER, uriHandler);
		this.updatedXMILocation = updatedXMILocation;
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		int eventType = notification.getEventType();
		LazyModelDynamicEObjectImpl notifier = null;
		switch (eventType) {
			case LazyModelNotification.SET:
				if (isHandleCustomNotification()) {
					// 4.1 Save Model: <TransformationName>-updated.xmi
					notifier = (LazyModelDynamicEObjectImpl)notification.getNotifier();
					setHandleCustomNotification(false);
					try {
						notifier.eResource().save(new FileOutputStream(new File(updatedXMILocation)),
								saveOptions);
					} catch (IOException e) {
						e.printStackTrace();
					}
					setHandleCustomNotification(true);
				}

				break;
			default:
				break;
		}
	}

	public void setHandleCustomNotification(boolean handleCustomNotification) {
		this.handleCustomNotification = handleCustomNotification;
	}

	public boolean isHandleCustomNotification() {
		return handleCustomNotification;
	}
}
