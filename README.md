Latitude
========

A Groovy lib for referenceable properties.

Latitude intercepts an object `getProperty` method. You can create a property path, by chaining getters, and get a string path from it:


```groovy
import org.latitude.Latitude

Customer customer = Latitude.forClass Customer
assert customer instanceof Customer
assert customer.name == "name"
```

(The model classes - Customer, Person, City - used in the tests can be found [here](https://github.com/will-lp/latitude/tree/master/test/org/latitude/test/model))


The resulting path can be type checked and customized for use within queries or places where field references are needed. An IDE can auto-complete, refactor, find usages and `@TypeChecked` can point mistakes.


```groovy
Customer customer = Latitude.forClass Customer, prefix: 'c'
assert customer.city.name == "c.city.name"
```

An example using it to ignore and reference a field (the cast may be needed in some IDEs like eclipse):

```groovy
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

public <T> T copyFields( source, T destiny, ignore ) {
	source.properties.each { name, value ->
		if (destiny.hasProperty(name) && 
			!(name in (ignore + ['class', 'metaClass']))) {
			destiny[name] = value
		}
	}
	destiny
}
```

An example using it to type check the model fields in a query:


```groovy
def person = (Person) Latitude.forClass (Person, prefix: 'person')
def sql = "select $person.name from Person person where $person.surname = 'doe'"
assert sql == "select person.name from Person person where person.surname = 'doe'"
```


It can also intercept getters on an already created object:

```groovy
def myString = "myString"
Latitude.intercept myString
assert myString.black.texas.gold == "black.texas.gold"
```


It relies on MOP interception, so it won't work with @CompileStatic.


## Getting and setting property paths in objects


Latitude also comes with some helper methods to get and set a chain of properties.

### Getters


A simple getter:

```groovy
def customer = new Customer(
	name: 'john', 
	city: new City(name: "Ottawa",
		state: new State(name: "Ontario")))

Customer meta = Latitude.forClass Customer
assert Latitude.getChainedProperty(customer, meta.city.state.name) == "Ontario"
```


Using a getter whilst using Latitude as a category:


```groovy
use(Latitude) {
	def c = (Customer)Customer.forClass()
	def dob = Date.parse('yyyy-MM-dd', '1972-08-30')
	def customer = new Customer(dob: dob)
	
	assert customer.getChainedProperty(c.dob) == dob
}
```

A small DSL to get the property:


```groovy
Customer meta = Latitude.forClass Customer
def customer = new Customer(city: new City(name: "Rio de Janeiro"))
def cityName = Latitude.getChainedProperty meta.city.name from customer

assert cityName == "Rio de Janeiro"
```


### Setter


The standard `Latitude#setChainedProperty` has three parameters, and an optional fourth one to specify a character separator:

```groovy
Customer c = Latitude.forClass Customer
def customer = new Customer(city: new City(state: new State()))
Latitude.setChainedProperty customer, c.city.state.name, "Volvograd"
assert customer.city.state.name == "Volvograd"
```

With a small DSL:

```groovy
Customer c = Latitude.forClass Customer
def customer = new Customer(city: new City(state: new State()))
Latitude.setChainedProperty c.city.state.name to customer with "Toyokawa"

assert customer.city.state.name == "Toyokawa"
```

-----

Future work: creating an AST to generate classes with the meta-properties. These shall work under @CompileStatic.
