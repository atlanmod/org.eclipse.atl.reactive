# org.eclipse.atl.reactive

Reactive engine for the ATL transformation language. In a model-driven application environment it works by activating only the strictly needed computation in response to updates or requests of model elements. Computation is updated when necessary, in an autonomous and optimized way by using incrementality and lazy evaluation.

Requirements:

In order to import and compile the source code, and Eclipse IDE with the following Eclipe plugins is required.

-EMF SDK
-EMF Compare
-EMF Transaction

In order to Install these plugins, go to the menu Help->Install new software or to the Eclipse MarketPlace

Additionally, the Antlr runtime eclipse plugin (3.0.0 or newer) is required: https://github.com/antlr/website-antlr3/blob/gh-pages/download/antlr-3.0.tar.gz

Installation:

-After cloning the repository, the plugins in /plugins need to be imported into a Eclipse IDE workspace. RightClick-> Import-> Existing Projects Into Workspaces. 

Testing:

From the previous workspace, launch a new Eclipse Application (runAs->Eclipse Application). A new Eclipse IDE will open. Reactive ATL is ready to be used programatically.

In the /test folder the project org.eclipse.m2m.atl.reactive.test contains a set of programmatic examples demonstrating:

-Lazy computation and incremental propagation (through invalidation).
-A reactive transformation chain. From ClassDiagram to Relational to XML.
-Higher-order reactivity (modifications to the transformation itself).
-A transformation producing an infinite output model.

In order to import these tests, RightClick->Inport->Existing Projects Into Workspace -> Select the test plugin


