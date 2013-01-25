package org.eclipse.m2m.atl.reactive;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;
import org.eclipse.m2m.atl.reactive.model.LazyModelNotification;

public class FlagChangeForwardingAdapter extends EContentAdapter {

	EMFVMLazyTransformation transformation;
	private boolean handleCustomNotification = true;

	public FlagChangeForwardingAdapter(EMFVMLazyTransformation transformation) {
		super();
		this.transformation = transformation;
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		int eventType = notification.getEventType();
		LazyModelDynamicEObjectImpl notifier = null;
		switch (eventType) {
		case LazyModelNotification.FLAG_CHANGE:
			if (isHandleCustomNotification()) {
				boolean newValue = ((LazyModelNotification) notification)
						.getNewBooleanValue();
				if (newValue == false) {
					notifier = (LazyModelDynamicEObjectImpl) ((LazyModelNotification) notification)
							.getNotifier();
					// transformation.setBusy(false);
					// Here in a chain, we will have to forward the flag
					// invalidation. We need then to
					// the other transformation and not this
					transformation.propertyChanged(notifier,
							((LazyModelNotification) notification)
									.getFeatureName());
					// transformation.setBusy(true);
				}
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
