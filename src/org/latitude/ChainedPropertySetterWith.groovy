package org.latitude

class ChainedPropertySetterWith {

	def target
	def path
	
	def with(value) {
		Latitude.setChainedProperty(target, path, value)
	}
	
}
