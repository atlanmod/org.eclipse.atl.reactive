# org.eclipse.atl.reactive

Reactive engine for the ATL transformation language. In a model-driven application environment it works by activating only the
strictly needed computation in response to updates or requests of model elements. Computation is updated when necessary, in an autonomous and optimized way by using incrementality and lazy evaluation.

Installation:

-After cloning the repository, the plugins in /plugins need to be imported into a Eclipse IDE workspace. 

Requirements:

-EMF SDK
-EMF Compare
-EMF Transaction
-Antlr eclipse plugin (3.0.0 or newer)

Testing:

In the /test folder the project org.eclipse.m2m.atl.reactive.test contains a set of programmatic examples demonstrating:

-Lazy computation and incremental propagation (through invalidation).
-A reactive transformation chain. From ClassDiagram to Relational to XML.
-Higher-order reactivity (modifications to the transformation itself).
-A transformation producing an infinite output model.
