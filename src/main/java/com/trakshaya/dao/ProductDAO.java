package com.trakshaya.dao;

import com.trakshaya.entity.Product;
import com.trakshaya.model.PaginationResult;
import com.trakshaya.model.ProductInfo;


public interface ProductDAO {

	public Product findProduct(String code);
	
	public ProductInfo findProductInfo(String code);
	
	public PaginationResult<ProductInfo> queryProducts(int page,
			int maxResult, int maxNavigationPage);;
			
	public PaginationResult<ProductInfo> queryProducts(int page,
			int maxResult, int maxNavigationPage,String likeName);
	
	public void save(ProductInfo productInfo);
	
}

