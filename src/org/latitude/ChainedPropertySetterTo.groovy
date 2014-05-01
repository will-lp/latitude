package org.latitude

class ChainedPropertySetterTo {

	def path
	
	ChainedPropertySetterWith to(target) {
		new ChainedPropertySetterWith(path: path, target: target)
	}
	
}
