package org.latitude.test

import groovy.transform.TypeChecked

import org.latitude.Latitude
import org.latitude.test.model.Child
import org.latitude.test.model.City
import org.latitude.test.model.Customer
import org.latitude.test.model.Foo
import org.latitude.test.model.Invoice
import org.latitude.test.model.Person
import org.latitude.test.model.ProductCategory
import org.latitude.test.model.State

class DynamicPropertyTest extends GroovyTestCase {

	void testBasic() {
		Customer customer = Latitude.forClass Customer
		assert customer instanceof Customer
		assert customer.name == "name"
	}
	
	
	@TypeChecked
	void testChained() {
		def customer = (Customer) Latitude.forClass (Customer)
		assert customer instanceof Customer
		assert customer.city.name == "city.name"
	}
	
	
	void testChainedWithPrefix() {
		Customer customer = Latitude.forClass Customer, prefix: 'c'
		assert customer.city.name == "c.city.name"
	}
	
	
	void testWithPrefix() {
		Customer customer = Latitude.forClass Customer, prefix: "customer"
		assert customer.name == "customer.name"
	}
	
	
	void testGetChainedPropertyPath() {
		Customer meta = Latitude.forClass Customer
		
		def customer = new Customer(name: 'john', 
			city: new City(name: "Ottawa",
				state: new State(name: "Ontario")))
		
		assert Latitude.getChainedProperty(customer, meta.city.state.name) == "Ontario"
	}
	
	
	void testGetChainedPropertyPathDsl() {
		Customer meta = Latitude.forClass Customer
		def customer = new Customer(city: new City(name: "Rio de Janeiro"))
		def cityName = Latitude.getChainedProperty meta.city.name from customer
		
		assert cityName == "Rio de Janeiro"
	}
	
	
	void testCustomSeparator() {
		Customer cust = Latitude.forClass Customer, separator: "/"
		assert cust.city.name == "city/name"
	}
	
	void testCustomSeparatorWithPrefix() {
		Invoice i = Latitude.forClass(Invoice, prefix: "invoice", separator: "\\")
		
		assert i.products.name == /invoice\products\name/
	}
	
	void testCustomProcessor() {
		ProductCategory category = Latitude.forClass ProductCategory, prefix: 'product_category', 
			onGet : { String p -> 
				def array = p.split("(?=\\p{Lu})")
				assert array.length == 2
				array*.toLowerCase().join "_"
			}
		
		assert category.priceType == "product_category.price_type" 
	}
	
	void testList() {
		Invoice invoice = Latitude.forClass Invoice, prefix: 'invoice'
		
		assert invoice.products.productCategory.name == "invoice.products.productCategory.name"
	}
	
	void testSetChainedProperty() {
		Customer c = Latitude.forClass Customer
		def customer = new Customer(city: new City(state: new State()))
		
		Latitude.setChainedProperty customer, c.city.state.name, "Volvograd"
		
		assert customer.city.state.name == "Volvograd"
	}
	
	void testSetChainedPropertyDsl() {
		Customer c = Latitude.forClass Customer
		def customer = new Customer(city: new City(state: new State()))
		
		Latitude.setChainedProperty c.city.state.name to customer with "Toyokawa"
		
		assert customer.city.state.name == "Toyokawa"
	}
	
	
	void testInterceptSomethingIAlreadyHave() {
		def myString = "chicken"
		Latitude.intercept myString
		assert myString.black.texas.gold == "black.texas.gold"
	}
	
	
	void testSetterCategory() {
		use(Latitude) {
			Customer c = Customer.forClass()
			
			def customer = new Customer(city: new City())
			
			customer.setChainedProperty(c.city.name, "Buôn Ma Thuột")
			
			assert customer.city.name == "Buôn Ma Thuột"
		}
	}
	
	
	void testGetterCategory() {
		use(Latitude) {
			def c = (Customer)Customer.forClass()
			
			def dob = Date.parse('yyyy-MM-dd', '1972-08-30')
			
			def customer = new Customer(dob: dob)
			
			assert customer.getChainedProperty(c.dob) == dob
		}
	}
	
	
	void testGetterCategoryDsl() {
		use(Latitude) {
			def meta = (Customer) Customer.forClass()
			def customer = new Customer(city: new City(name: "Ahmednagar"))
			assert meta.city.name.getChainedProperty().from(customer) == 'Ahmednagar'
		}
	}
	
	
	public <T> T copyFields( source, T destiny, ignore ) {
		source.properties.each { name, value ->
			if (destiny.hasProperty(name) && 
				!(name in (ignore + ['class', 'metaClass']))) {
				destiny[name] = value
			}
		}
		destiny
	}
	
	@TypeChecked
	void testSimpleCopyAndIgnoreFields() {
		def person = new Person(
			name: 'john', 
			surname: 'doe', 
			favoriteThing: 'monster trucks')
		
		def meta = (Person) Latitude.forClass(Person)
		
		def newPerson = copyFields(person, new Person(), [meta.surname])
		
		assert newPerson.name == 'john'
		assert newPerson.surname == null
		assert newPerson[meta.favoriteThing] == 'monster trucks'
	}
	
	
	@TypeChecked
	void testCopyAndIgnoreFields() {
		
		def dob = Date.parse("yyyy-MM-dd", '1965-10-01')
		
		def source = new Customer(
			cc       : 9, 
			city     : new City(name: "Egilsstaðir"), 
			dob      : dob, 
			name     : 'john doe', 
			children : [new Child(name: 'john doe jr')])
		
		def meta = (Customer) Latitude.forClass(Customer)
		
		def destiny = (Customer) copyFields( source, 
				new Customer(), 
				[meta.dob, meta.children] )
		
		assert destiny.cc == 9
		assert destiny.city.name == "Egilsstaðir"
		assert destiny.dob == null
		assert destiny.children == null
		assert destiny.name == 'john doe'
	}
	
	
	@TypeChecked
	void testTypeCheckedSql() {
		
		def person = (Person) Latitude.forClass (Person, prefix: 'person')
		
		def sql = "select $person.name from Person person where $person.surname = 'doe'"
		
		assert sql == "select person.name from Person person where person.surname = 'doe'"
	}
	
	
	void testMap() {
		
		def meta = (Customer)Latitude.forClass(Customer)
		def path = meta.city.state.name
		def map = [city : [state : [name : 'Koh Kong']]]
		
		def value = Latitude.getChainedProperty(map, path)
		
		assert value == 'Koh Kong'
	}
	
	void testFoo() {
		
		def metaFoo = Latitude.forClass(Foo)
		def foo = new Foo(bar: 'john doe')
		assert Latitude.getChainedProperty(foo, metaFoo.bar) == 'john doe'
	}
	
	void testCategoryFoo() {
		use(Latitude) {
			def metaFoo = Foo.forClass()
			def foo = new Foo(bar: "baz baz")
			assert foo.getChainedProperty(metaFoo.bar) == "baz baz"
		}
	}
	
	
	void testCategoryWithParams() {
		use(Latitude) {
			def metaFoo = Foo.forClass(separator: "/", prefix: "public/foo")
			assert metaFoo.bar.baz == "public/foo/bar/baz"
		}
	}
	
	void testCategoryInstance() {
		use(Latitude) {
			def meta = "".intercept(prefix: "combo")
			assert meta.echo == "combo.echo"
		}
	}
	
	
	// FIXME: This should have worked
//	void testToString() {
//		def customer = Latitude.forClass Customer, prefix: 'customer'
//		assert customer.toString() == 'customer'
//		assert customer.name == 'customer.name'
//	}
	
}


