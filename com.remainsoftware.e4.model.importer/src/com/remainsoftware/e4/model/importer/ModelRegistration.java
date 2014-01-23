package com.remainsoftware.e4.model.importer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

public class ModelRegistration implements IStartup {
	ExecutorService threadPool = Executors.newFixedThreadPool(10);

	@Override
	public void earlyStartup() {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {

				IExtensionRegistry registry = Activator.getService(IExtensionRegistry.class);

				IConfigurationElement[] elements = registry.getConfigurationElementsFor(Activator
						.getContext().getBundle().getSymbolicName()
						+ ".modelimport");
				for (IConfigurationElement element : elements) {
					String uri = element.getAttribute("modelURI");
					String id = element.getAttribute("modelId");
					String reParent = element.getAttribute("targetId");
					String relationship = element.getAttribute("relationship");
					MUIElement muiElement = ApplicationModelUtil.loadModel(uri, id, registry);
					IEclipseContext serviceContext = (IEclipseContext) PlatformUI.getWorkbench()
							.getService(IEclipseContext.class);
					EModelService modelService = serviceContext.get(EModelService.class);
					MApplication application = serviceContext.get(MApplication.class);
					MUIElement parentElement = modelService.find(reParent, application);
					if (relationship.equals("sibling"))
						parentElement = parentElement.getParent();

					/*
					 * The addition below is just a quick hack and needs more
					 * work. For now it is enough to, for example, add a part to
					 * a partstack
					 */
					if (parentElement instanceof MElementContainer) {
						MElementContainer cont = (MElementContainer) parentElement;
						cont.getChildren().add(muiElement);
					}
				}
			}
		});
	}

}
