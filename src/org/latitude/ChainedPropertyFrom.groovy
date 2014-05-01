package org.latitude

class ChainedPropertyFrom {
	def path
	def from(obj) {
		Latitude.getChainedProperty(obj, path)
	}
}
