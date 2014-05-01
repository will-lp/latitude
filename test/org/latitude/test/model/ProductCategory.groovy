package org.latitude.test.model

class ProductCategory {

	List<Product> product
	
	@Column("price_type")
	BigDecimal priceType
	
}
