package org.eclipse.m2m.atl.reactive;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;

public class ReactiveLauncher extends EMFVMLauncher {
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher#launch(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor, java.util.Map, java.lang.Object[])
	 */
	@Override
	public Object launch(String mode, IProgressMonitor monitor, Map<String, Object> options,
			Object... modules) {
		
		return null;
		
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher#getName()
	 */
	@Override
	public String getName() {
		return "Reactive " + super.getName();
	}
}
