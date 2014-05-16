package org.eclipse.m2m.atl.reactive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;
import org.eclipse.m2m.atl.reactive.model.LazyModelNotification;

/**
 * Target Reactive Adapter handles notification events produced by Lazy Model
 * {@link LazyModelDynamicEObjectImpl} elements, such as GET and FLAG_CHANGE
 * events. Then, a given running reactive transformation is used to react to
 * such events, namely, launching the needed rules and bindings to get the
 * target element.
 * 
 * @author Salvador Martínez
 * 
 */
public class StandardAndSaveTargetAdapter extends EContentAdapter {

	EMFVMLazyTransformation transformation;
	private boolean handleCustomNotification = true;
	private Map<String, Object> saveOptions = new HashMap<String, Object>();

	XMLResource.URIHandler uriHandler = new XMLResource.URIHandler() {

		@Override
		public URI deresolve(URI uri) {
			// URI u = URI.createURI("");
			URI u = URI.createURI('#' + uri.fragment());
			return u;
		}

		@Override
		public URI resolve(URI uri) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setBaseURI(URI uri) {
			// TODO Auto-generated method stub

		}
	};

	public StandardAndSaveTargetAdapter(EMFVMLazyTransformation transformation,
			EObject root) {
		super();
		this.transformation = transformation;
		saveOptions.put(XMLResource.OPTION_URI_HANDLER, uriHandler);
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		int eventType = notification.getEventType();
		LazyModelDynamicEObjectImpl notifier = null;
		switch (eventType) {
		case LazyModelNotification.GET:
			if (isHandleCustomNotification()) {
				EStructuralFeature feature = ((LazyModelNotification) notification)
						.getEstructuralFeature();
				notifier = (LazyModelDynamicEObjectImpl) ((LazyModelNotification) notification)
						.getNotifier();
				setHandleCustomNotification(false);
				if (!notifier.getFeaturesFlagMapElement(feature.getName())) {
					transformation.getBinding(
							((LazyModelNotification) notification).getValue(),
							notifier, feature);
					notifier.setFeaturesFlagMapElement(feature.getName(), true);
					// Here, we save the target resource so that the changes are
					// serialized
					try {
						notifier.eResource().save(saveOptions);
					} catch (IOException e) {
						e.printStackTrace();
					}
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
