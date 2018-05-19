package com.trakshaya.daoimpl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.trakshaya.dao.AccountDAO;
import com.trakshaya.entity.Account;

@Transactional	
public class AccountDaoImpl implements AccountDAO{

	@Autowired
	private SessionFactory  sessionFactory;

	@Override
	public Account findAccount(String userName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Account.class);
		crit.add(Restrictions.eqOrIsNull("userName", userName));
		return (Account) crit.uniqueResult();
	}
}
