package org.eclipse.m2m.atl.reactive;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
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
public class StandardTargetAdapter extends EContentAdapter {

	EMFVMLazyTransformation transformation;
	private boolean handleCustomNotification = true;
	

	public StandardTargetAdapter(EMFVMLazyTransformation transformation) {
		super();
		this.transformation = transformation;
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
