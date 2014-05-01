package org.latitude.test.model

class Product {

	@Column("product_category")
	ProductCategory productCategory
	
	String name
	
	BigDecimal price
	
}
