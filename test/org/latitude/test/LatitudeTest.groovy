package org.latitude.test;

import groovy.util.GroovyTestCase;

class LatitudeTest extends GroovyTestCase {

	void testBasic() {
		assertScript '''
			import org.latitude.Meta as M

			class Foo {
				@M Foo meta
				String bar
			}
			
			def m = Foo.meta
			assert m.bar in MetaProperty
			assert m.bar.name == "bar"
		'''
	}
	
	
	void testTyped() {
		assertScript '''
			import groovy.transform.TypeChecked as T
			import org.latitude.Meta

			class Foo {
				@Meta Foo meta

				Bar bar
			}
			
			@T class Bar {
				void main() {
					def meta = Foo.meta
					assert meta.bar.name == 'bar'
					assert meta.bar.type == Bar
					assert !Foo.metaClass.properties.find { it.name == 'meta' } 
					assert Foo.metaClass.methods.find { it.name == 'getMeta' }
				}
			}

			new Bar().main()
			
		'''
	}
	
	
	void testInnerClass() {
		assertScript '''
			import org.latitude.Meta as M

			class Person {
				@M String name
			}

			def innerCs = Person.class.declaredClasses
			def inner = innerCs[0]
			assert inner
			assert inner.name == 'Person$Meta'
		'''
	}
	
	
	void testVisibility() {
		assertScript '''
			import org.latitude.Meta
			
			class Foo {
				@Meta Foo meta
				public String imPublic
				private String imPrivate
				protected String imProtected
				@groovy.transform.PackageScope String imPackage
				String imProperty
			}

			def m = Foo.meta
			assert m.imPublic

			['imPrivate', 'imProtected', 'imPackage'].every {
				assert !m.respondsTo(it)
			}
			
			assert m.imProperty
		'''
	}
	
	
	void testHasStaticMethod() {
		assertScript '''
			import org.latitude.Meta
			class Foo {
				@Meta Foo chicken
			}

			assert Foo.metaClass.methods.find { 
				it.name == "getChicken" && 
				it.isStatic()
			}
		'''
	}
	
	
	void testStaticMethodReturnsNewMetaClass() {
		assertScript '''
			import org.latitude.Meta
			class Customer {
				@Meta Customer metaCustomer
				String name
			}

			assert Customer.metaClass.methods.find {
				it.name == 'getMetaCustomer' &&
				it.static &&
				it.returnType == Customer.Customer$Meta
			}
		'''
	}
	
	
	void testInnerClassHasMethods() {
		assertScript '''
			import org.latitude.Meta

			class Receipt {
				@Meta Receipt meta
				BigDecimal value
			}
			
			assert Receipt.meta
			
			//assert Receipt.Receipt$Meta

			def meta = Receipt.meta
			
			meta.metaClass.methods.each { println it }
			
			assert GroovyObject in meta.getClass().interfaces

			assert meta.metaClass.methods.any { 
				it.name == 'getValue' &&
				it.returnType == MetaProperty
			}
		'''
	}
	
	
}
