/*******************************************************************************
 * Copyright (c) 2008 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.m2m.atl.core.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.m2m.atl.ATLLogger;
import org.eclipse.m2m.atl.ConsoleStreamHandler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The core ui plugin provides a way to launch transformations using the core api.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class ATLCoreUIPlugin extends AbstractUIPlugin {
	/** The shared instance. */
	private static ATLCoreUIPlugin plugin;

	private static MessageConsole console;

	private static Handler handler;

	/**
	 * Creates a new {@link ATLCoreUIPlugin}.
	 */
	public ATLCoreUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static ATLCoreUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * This method returns an image from the path.
	 * 
	 * @param path
	 *            the image path
	 * @return the created image
	 */
	public static Image createImage(String path) {
		try {
			URL baseUrl = plugin.getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(baseUrl, path);
			return ImageDescriptor.createFromURL(url).createImage();
		} catch (MalformedURLException e) {
			ATLLogger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		console = findConsole("ATL"); //$NON-NLS-1$
		handler = new ConsoleStreamHandler(console.newMessageStream());
		ATLLogger.getLogger().addHandler(handler);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		ATLLogger.getLogger().removeHandler(handler);
		super.stop(context);
	}

	private MessageConsole findConsole(String name) {
		IConsoleManager consoleMgr = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] existing = consoleMgr.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole)existing[i];
			}
		}
		// no console found, so create a new one
		String pluginDir = getBundle().getEntry("/").toString(); //$NON-NLS-1$
		ImageDescriptor imageDescriptor = null;
		try {
			imageDescriptor = ImageDescriptor.createFromURL(new URL(pluginDir + "icons/atl_logo.gif")); //$NON-NLS-1$
		} catch (MalformedURLException mfe) {
			imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		MessageConsole myConsole = new MessageConsole(name, imageDescriptor);
		consoleMgr.addConsoles(new IConsole[] {myConsole});
		return myConsole;
	}
}