package org.latitude


/**
 * The main API methods to intercept properties on objects.
 * 
 * Latitude works mainly using {@code getProperty} and {@code setProperty}, 
 * so it will work with @TypeChecked, but now with @CompileStatic,
 * since the method calls need to go through the MOP.
 * 
 * @author will
 *
 */
class Latitude {
	
	/**
	 * Instantiate an object from {@code clazz} to intercept
	 * its methods.
	 * 
	 * Allows a simpler usage of Latitude as a Category class:
	 * <pre>
	 * {@code
	 * 	use(Latitude) {
	 * 		def metaFoo = Foo.forClass()
	 * 		def foo = new Foo(bar: "baz baz")
	 * 		assert foo.getChainedProperty(metaFoo.bar) == "baz baz"
	 * 	}
	 * }
	 * </pre>
	 * 
	 * {@link org.latitude.Latitude#intercept(Object, Map)}
	 * 
	 * @param clazz
	 * @param params
	 * @return
	 */
	static <T> T forClass(Class<T> clazz, Map params) {
		intercept params, clazz.newInstance()
	}
	
	
	/**
	 * Instantiate an object from {@code clazz} to intercept its 
	 * getters.
	 * 
	 * <pre>
	 * {@code
	 * Customer cust = Latitude.forClass Customer, separator: "/"
	 * assert cust.city.name == "city/name"
	 * }
	 * </pre>
	 * 
	 * @param clazz
	 * @return
	 */
	static <T> T forClass(Map params = [:], Class<T> clazz) {
		intercept(clazz.newInstance(), params)
	}
	
	
	/**
	 * Instantiate an object from {@code clazz} and intercept
	 * its getters.
	 * 
	 * Allows a simpler usage of Latitude as a Category class:
	 * <pre>
	 * {@code
	 * 	use(Latitude) {
	 * 		def meta = "".intercept(prefix: "combo")
	 * 		assert meta.echo == "combo.echo"
	 * 	}
	 * }
	 * </pre>
	 * 
	 * {@link org.latitude.Latitude#intercept(Object, Map)}
	 * 
	 * @param instance
	 * @return
	 */
	static <T> T intercept(Map params = [:], T instance) {
		intercept instance, params
	}
	
	
	/**
	 * Intercept all calls to {@code getProperty} on the instance
	 * object. A map of params may be passed to further customize
	 * the interception.
	 * 
	 * @param instance
	 * @param params map. Currently it allows the following keys:
	 * - separator (String): a separator to the resulting property 
	 * chain string. Defaults to dot (".")
	 * - prefix (String): the prefix to the chain. May be useful when 
	 * building a SQL query. Defaults to empty string.
	 * - onGet (Closure): a closure which will be invoked upon each
	 * {@code getProperty}.
	 * 
	 * @return an object with {@code getProperty} intercepted to
	 * build a property chain. Each {@code getProperty} method call 
	 * return will also be intercepted. 
	 */
	static <T> T intercept(T instance, Map params) {
		
		def separator = params.separator ?: "."
		def prefix = params.prefix ? (params.prefix + separator) : ""
		def onGet = params.onGet ?: { it }
		
		
		
		/*
		 * Had to declare both properties here since mixin
		 * doesn't accept an instance object, which was needed
		 * for a custom separator
		 */
		def propertyGetter
		propertyGetter = { String subProperty ->
			def newProperty = onGet delegate + separator + subProperty
			newProperty.metaClass.getProperty = propertyGetter
			newProperty
		}
		
		instance.metaClass {
			getProperty = { String p ->
				def property = onGet prefix + p
				property.metaClass.getProperty = propertyGetter
				property
			}
			toString = { prefix }
		}
		
		instance
	}
	
	
	/**
	 * Obtains a property from a chain of properties in an
	 * object.
	 * 
	 * A custom separator may be provided.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * {@code
	 * def metaFoo = Latitude.forClass(Foo)
	 * def foo = new Foo(bar: 'john doe')
	 * assert Latitude.getChainedProperty(foo, metaFoo.bar) == 'john doe'
	 * }
	 * </pre>
	 * 
	 * @param obj
	 * @param path
	 * @return
	 */
	static getChainedProperty(obj, path, separator=".") {
		def current = obj 
		path.tokenize(separator).each {
			current = current[it]
		}
		current
	}
	
	
	/**
	 * Get a property from a chain of properties from an object
	 * 
	 * Allows a simple DSL in the form:
	 * <pre>
	 * {@code
	 * Latitude.getChainedProperty foo.bar from object
	 * }
	 * </pre>
	 * 
	 * @param path
	 * @return
	 */
	static ChainedPropertyFrom getChainedProperty(path) {
		new ChainedPropertyFrom(path: path)
	}
	
	
	/**
	 * Sets a chained property into an object
	 * 
	 * <pre>
	 * {@code
	 * Customer c = Latitude.forClass Customer
	 * def customer = new Customer(city: new City(state: new State()))
	 * Latitude.setChainedProperty customer, c.city.state.name, "Volvograd"
	 * assert customer.city.state.name == "Volvograd"
	 * }
	 * </pre>
	 * 
	 * @param obj
	 * @param path
	 * @param value
	 * @return
	 */
	static setChainedProperty(obj, path, value, separator=".") {
		def current = obj
		def splitPath = path.tokenize separator
		splitPath[0..-2].each { current = current[it] }
		current[splitPath[-1]] = value
	}
	
	
	/**
	 * Allows a DSL to set properties in an object from a chain
	 * of properties
	 * 
	 * <pre>
	 * {@code
	 * Customer c = Latitude.forClass Customer
	 * def customer = new Customer(city: new City(state: new State()))
	 * Latitude.setChainedProperty c.city.state.name to customer with "Toyokawa"
	 * assert customer.city.state.name == "Toyokawa"
	 * }
	 * </pre>
	 * 
	 * @param path
	 * @return
	 */
	static ChainedPropertySetterTo setChainedProperty(path) {
		new ChainedPropertySetterTo(path: path)
	}
	
}
