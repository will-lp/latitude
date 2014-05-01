package org.latitude.test.model

class Invoice {

	List<Product> products
	
	Integer number
	
	@Column("selling_date")
	Date sellingDate
	
}
