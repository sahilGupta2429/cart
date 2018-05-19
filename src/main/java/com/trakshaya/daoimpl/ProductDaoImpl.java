package com.trakshaya.daoimpl;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.trakshaya.dao.ProductDAO;
import com.trakshaya.entity.Product;
import com.trakshaya.model.PaginationResult;
import com.trakshaya.model.ProductInfo;

@Transactional
public class ProductDaoImpl implements ProductDAO{

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public Product findProduct(String code) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Product.class);
		crit.add(Restrictions.eqOrIsNull("code", code));
		return (Product)crit.uniqueResult();
	}

	@Override
	public ProductInfo findProductInfo(String code) {
		Product product = this.findProduct(code);
		if(product==null) {
			return null;
		}
		return new ProductInfo(product.getCode(), product.getName(),
				product.getPrice());
	}

	@Override
	public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage) {
		return queryProducts(page,maxResult,maxNavigationPage,null);
	}

	@Override
	public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
			String likeName) {
		
		String sql = " Select new " + ProductInfo.class.getName()
				+ " (p.code, p.name, p.price)" + " from"
				+ Product.class.getName() + " p";
		if(likeName!=null && likeName.length()>0) {
			sql += " where lower(p.name) like : likeName ";
		}
		
		sql += " order by p.createDate desc";
		
		Session session = sessionFactory.getCurrentSession();
		
		Query query = session.createQuery(sql);
		if (likeName != null && likeName.length() > 0) {
	       query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
	    }
		
		return new PaginationResult<ProductInfo>(query,page,maxResult,maxNavigationPage);
	}

	@Override
	public void save(ProductInfo productInfo) {

		String code = productInfo.getCode();
		Product product = null;
		
		boolean isNew = false;
		
		if(code != null) {
			product = this.findProduct(code);
		}
		
		if(product == null) {
			isNew = true;
			product = new Product();
			product.setCreateDate(new Date());
		}
		
		product.setCode(code);
		product.setName(productInfo.getName());
		product.setPrice(productInfo.getPrice());
		
		if(productInfo.getFileData()!= null) {
			byte[] image = productInfo.getFileData().getBytes();
			if(image!=null && image.length > 0) {
				product.setImage(image);
			}
		}
		
		if(isNew) {
			this.sessionFactory.getCurrentSession().persist(product);
		}
		
		this.sessionFactory.getCurrentSession().flush();
	}
	
}
