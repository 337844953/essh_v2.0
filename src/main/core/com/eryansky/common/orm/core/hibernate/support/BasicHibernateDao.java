/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eryansky.common.orm.core.hibernate.support;

import com.eryansky.common.orm.core.hibernate.interceptor.SecurityCodeInterceptor;
import com.eryansky.common.orm.core.hibernate.interceptor.StateDeleteInterceptor;
import com.eryansky.common.orm.core.hibernate.interceptor.TreeEntityInterceptor;
import com.eryansky.common.orm.interceptor.OrmDeleteInterceptor;
import com.eryansky.common.orm.interceptor.OrmInsertInterceptor;
import com.eryansky.common.orm.interceptor.OrmSaveInterceptor;
import com.eryansky.common.orm.interceptor.OrmUpdateInterceptor;
import com.eryansky.common.utils.reflection.ReflectionUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.internal.AbstractQueryImpl;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Hibernate基础类,包含对Hibernate的CURD和其他Hibernate操作
 * 
 * @author maurice
 *
 * @param <T> ROM对象
 * @param <ID> ORM主键ID类型
 */
@SuppressWarnings("unchecked")
public class BasicHibernateDao<T,ID extends Serializable> {
	
	protected SessionFactory sessionFactory;

	protected Class<T> entityClass;
	
	protected final String DEFAULT_ALIAS = "X";
	
	//当删除对象时的拦截器
	private List<OrmDeleteInterceptor<T, BasicHibernateDao<T, ID>>> deleteInterceptors;
	//当保存或更新对象时的拦截器
	private List<OrmSaveInterceptor<T, BasicHibernateDao<T, ID>>> saveInterceptors;
	//当插入对象时的拦截器
	private List<OrmInsertInterceptor<T, BasicHibernateDao<T, ID>>> insertInterceptors;
	//当更新对象时的拦截器
	private List<OrmUpdateInterceptor<T, BasicHibernateDao<T, ID>>> updateInterceptors;
	
	
	/**
	 * 构造方法
	 */
	public BasicHibernateDao() {
		entityClass = ReflectionUtils.getClassGenricType(getClass());
		installInterceptors();
	}

	/**
	 * 构造方法
	 * 
	 * @param entityClass orm实体类型class
	 */
	public BasicHibernateDao(Class<T> entityClass) {
		this.entityClass = entityClass;
		installInterceptors();
	}
	
	/**
	 * 获取删除拦截器集合
	 * 
	 * @return List
	 */
	public List<OrmDeleteInterceptor<T, BasicHibernateDao<T, ID>>> getDeleteInterceptors() {
		return deleteInterceptors;
	}

	/**
	 * 设置删除拦截器集合
	 * 
	 * @param deleteInterceptors 删除拦截器集合
	 */
	public void setDeleteInterceptors(List<OrmDeleteInterceptor<T, BasicHibernateDao<T, ID>>> deleteInterceptors) {
		this.deleteInterceptors = deleteInterceptors;
	}

	/**
	 * 获取保存或更新拦截器集合
	 * 
	 * @return List
	 */
	public List<OrmSaveInterceptor<T, BasicHibernateDao<T, ID>>> getSaveInterceptors() {
		return saveInterceptors;
	}

	/**
	 * 设置保存或更新拦截器集合
	 * 
	 * @param saveInterceptors 保存或更新拦截器集合
	 */
	public void setSaveInterceptors(List<OrmSaveInterceptor<T, BasicHibernateDao<T, ID>>> saveInterceptors) {
		this.saveInterceptors = saveInterceptors;
	}

	/**
	 * 获取插入拦截器集合
	 * 
	 * @return List
	 */
	public List<OrmInsertInterceptor<T, BasicHibernateDao<T, ID>>> getInsertInterceptors() {
		return insertInterceptors;
	}

	/**
	 * 设置插入拦截器集合
	 * 
	 * @param insertInterceptors 插入拦截器集合
	 */
	public void setInsertInterceptors(List<OrmInsertInterceptor<T, BasicHibernateDao<T, ID>>> insertInterceptors) {
		this.insertInterceptors = insertInterceptors;
	}

	/**
	 * 获取更新拦截器集合
	 * 
	 * @return List
	 */
	public List<OrmUpdateInterceptor<T, BasicHibernateDao<T, ID>>> getUpdateInterceptors() {
		return updateInterceptors;
	}

	/**
	 * 设置更新拦截器集合
	 * 
	 * @param updateInterceptors 更新拦截器集合
	 */
	public void setUpdateInterceptors(List<OrmUpdateInterceptor<T, BasicHibernateDao<T, ID>>> updateInterceptors) {
		this.updateInterceptors = updateInterceptors;
	}

	/**
	 * 初始化所有拦截器
	 */
	private void installInterceptors() {
		
		//----初始化删除需要的所有拦截器----//
		deleteInterceptors = new ArrayList<OrmDeleteInterceptor<T,BasicHibernateDao<T,ID>>>();
		deleteInterceptors.add(new StateDeleteInterceptor<T, ID>());
		deleteInterceptors.add(new TreeEntityInterceptor<T, ID>());
		
		//----初始化保存或更新需要的所有拦截器----//
		saveInterceptors = new ArrayList<OrmSaveInterceptor<T,BasicHibernateDao<T,ID>>>();
		saveInterceptors.add(new TreeEntityInterceptor<T, ID>());
		saveInterceptors.add(new SecurityCodeInterceptor<T, ID>());
		
		//----初始化插入需要的所有拦截器----//
		updateInterceptors = new ArrayList<OrmUpdateInterceptor<T,BasicHibernateDao<T,ID>>>();
		updateInterceptors.add(new TreeEntityInterceptor<T, ID>());
		updateInterceptors.add(new SecurityCodeInterceptor<T, ID>());
		
		//----初始化更新需要的所有拦截器----//
		insertInterceptors = new ArrayList<OrmInsertInterceptor<T,BasicHibernateDao<T,ID>>>();
		insertInterceptors.add(new TreeEntityInterceptor<T, ID>());
		insertInterceptors.add(new SecurityCodeInterceptor<T, ID>());
	}

	/**
	 * 设置Hibernate sessionFactory
	 * 
	 * @param sessionFactory
	 */
	@Autowired(required = false)
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 获取Hibernate SessionFactory
	 * 
	 * @return {@link org.hibernate.SessionFactory}
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * 取得当前Session.
	 *
	 * @return {@link org.hibernate.Session}
	 */
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * 新增对象.
	 *
	 * @param entity orm实体
	 */
	public void insert(T entity) {

		for (OrmInsertInterceptor<T,BasicHibernateDao<T, ID>> interceptor : insertInterceptors) {
			if (!interceptor.onInsert(entity, this)) {
				return ;
			}
		}

		getSession().save(entity);

		Serializable id = ReflectionUtils.invokeGetter(entity, getIdName());

		for (OrmInsertInterceptor<T,BasicHibernateDao<T, ID>> interceptor : insertInterceptors) {
			interceptor.onPostInsert(entity, this, id);
		}
	}

	/**
	 * 批量新增对象
	 *
	 * @param list orm实体集合
	 */
	public void insertAll(List<T> list) {

		if (CollectionUtils.isEmpty(list)) {
			return ;
		}

		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			insert(it.next());
		}

	}

	/**
	 * 更新对象
	 * @param entity orm实体
	 */
	public void update(T entity) {
		Serializable id = ReflectionUtils.invokeGetter(entity, getIdName());

		for (OrmUpdateInterceptor<T,BasicHibernateDao<T, ID>> interceptor : updateInterceptors) {
			if (!interceptor.onUpdate(entity, this,id)) {
				return ;
			}
		}

		getSession().update(entity);

		for (OrmUpdateInterceptor<T,BasicHibernateDao<T, ID>> interceptor : updateInterceptors) {

			interceptor.onPostUpdate(entity, this,id);
		}
	}

	/**
	 * 批量更新对象
	 * @param list orm实体集合
	 */
	public void updateAll(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return ;
		}
		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			update(it.next());
		}
	}

	/**
	 * 新增或修改对象
	 *
	 * @param entity orm实体
	 */
	public void save(T entity) {
		Serializable id = ReflectionUtils.invokeGetter(entity, getIdName());

		for (OrmSaveInterceptor<T,BasicHibernateDao<T, ID>> interceptor : saveInterceptors) {
			if (!interceptor.onSave(entity, this, id)) {
				return ;
			}
		}

		getSession().saveOrUpdate(entity);

		for (OrmSaveInterceptor<T,BasicHibernateDao<T, ID>> interceptor : saveInterceptors) {
			interceptor.onPostSave(entity, this, id);
		}

	}

	/**
	 * 保存或更新全部对象
	 *
	 * @param list orm实体集合
	 */
	public void saveAll(List<T> list) {

		if (CollectionUtils.isEmpty(list)) {
			return ;
		}
		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			save(it.next());
		}
	}

	/**
	 * 删除对象.
	 *
	 * @param entity 对象必须是session中的对象或含ID属性的transient对象.
	 */
	public void delete(T entity) {

		for (OrmDeleteInterceptor<T,BasicHibernateDao<T, ID>> interceptor : deleteInterceptors) {
			ID id = (ID)ReflectionUtils.invokeGetter(entity, getIdName());
			if (!interceptor.onDelete(id, entity, this)) {
				return ;
			}
		}

		getSession().delete(entity);

		for (OrmDeleteInterceptor<T,BasicHibernateDao<T, ID>> interceptor : deleteInterceptors) {
			ID id = (ID)ReflectionUtils.invokeGetter(entity, getIdName());
			interceptor.onPostDelete(id, entity, this);
		}

	}

	/**
	 * 按ID删除对象.
	 *
	 * @param id 主键ID
	 */
	public void delete(ID id) {
		delete(get(id));
	}

	/**
	 * 按ID批量删除对象
	 *
	 * @param ids 主键ID集合
	 */
	public void deleteAll(List<ID> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return ;
		}
		for (Iterator<ID> it = ids.iterator(); it.hasNext();) {
			delete(it.next());
		}

	}

	/**
	 * 按orm实体集合删除对象
	 * @param list
	 */
	public void deleteAllByEntities(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return ;
		}

		for (Iterator<T> it = list.iterator(); it.hasNext();) {
			delete(it.next());
		}
	}


	/**
	 * 按ID获取对象实体.如果找不到对象或者id为null值时，返回null,参考{@link org.hibernate.Session#get(Class, java.io.Serializable)}
	 *
	 * @see org.hibernate.Session#get(Class, java.io.Serializable)
	 *
	 * @param id 主键ID
	 *
	 */
	public T get(ID id) {

		if (id == null) {
			return null;
		}

		return (T) getSession().get(entityClass, id);
	}

	/**
	 * 按ID获取对象代理.如果id为null，返回null。参考{@link org.hibernate.Session#load(Class, java.io.Serializable)}
	 *
	 * @see org.hibernate.Session#load(Class, java.io.Serializable)
	 *
	 * @param id 主键ID
	 *
	 */
	public T load(ID id) {
		if (id == null) {
			return null;
		}

		return (T) getSession().load(entityClass, id);
	}

	/**
	 * 按ID列表获取对象列表.
	 *
	 * @param ids 主键ID集合
	 *
	 * @return List
	 */
	public List<T> get(Collection<ID> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		return createCriteria(Restrictions.in(getIdName(), ids)).list();
	}

	/**
	 * 按ID列表获取对象列表.
	 *
	 * @param ids 主键ID数据
	 *
	 * @return List
	 */
	public List<T> get(ID[] ids) {
		return createCriteria(Restrictions.in(getIdName(), ids)).list();
	}

	/**
	 * 取得对象的主键名.
	 *
	 * @return String
	 */
	public String getIdName() {
		ClassMetadata meta = sessionFactory.getClassMetadata(entityClass);
		return meta.getIdentifierPropertyName();
	}

	/**
	 * 获取实体名称
	 *
	 * @return String
	 */
	public String getEntityName() {
		ClassMetadata meta = sessionFactory.getClassMetadata(entityClass);
		return meta.getEntityName();
	}

	/**
	 * 通过HQL查询全部
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 与属性名方式的hql值
	 *
	 * @return List
	 */
	public <X> List<X> findByQuery(String queryOrNamedQuery ,Map<String,Object> values) {
		return createQuery(queryOrNamedQuery , values).list();
	}

	/**
	 * 通过HQL查询全部
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 可变长度的hql值
	 *
	 * @return List
	 */
	public <X> List<X> findByQuery(String queryOrNamedQuery,Object... values) {
		return createQuery(queryOrNamedQuery, values).list();
	}

	/**
	 * 通过hql查询单个orm实体
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 以属性名的hql值
	 *
	 * @return Object
	 */
	public <X> X findUniqueByQuery(String queryOrNamedQuery,Map<String, Object> values){
		return (X)createQuery(queryOrNamedQuery, values).uniqueResult();
	}

	/**
	 * 通过hql查询单个orm实体
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 可变长度的hql值
	 *
	 * @return Object
	 */
	public <X> X findUniqueByQuery(String queryOrNamedQuery ,Object... values){
		return (X)createQuery(queryOrNamedQuery, values).uniqueResult();
	}

	/**
	 * 获取全部对象
	 *
	 * @param orders 排序对象，不需要排序，可以不传
	 *
	 * @return List
	 */
	public List<T> getAll(Order...orders) {
		Criteria c = createCriteria();
		if(ArrayUtils.isNotEmpty(orders)) {
			setOrderToCriteria(c, orders);
		}
		return c.list();
	}

	/**
	 * 根据Criterion可变数组创建Criteria对象
	 *
	 * @param criterions 可变长度的Criterion数组
	 *
	 * @return @return {@link org.hibernate.Criteria}
	 */
	protected Criteria createCriteria(Criterion... criterions) {

		Criteria criteria = getSession().createCriteria(this.entityClass);

		for (Criterion criterion :criterions) {

			criteria.add(criterion);
		}
		return criteria;
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values
	 *            命名参数,按名称绑定.
	 *
	 * @return {@link org.hibernate.Query}
	 *
	 */
	protected Query createQuery( String queryOrNamedQuery, Map<String, ?> values) {
		Query query = createQuery(queryOrNamedQuery);
		if (values != null) {
			query.setProperties(values);
		}
		return query;
	}

	/**
	 * 根据hql创建Hibernate Query对象
	 *
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 *
	 * @return {@link org.hibernate.Query}
	 */
	protected Query createQuery(String queryOrNamedQuery, Object... values) {
		Assert.hasText(queryOrNamedQuery, "queryOrNamedQuery不能为空");

		SessionFactoryImpl factory = (SessionFactoryImpl) sessionFactory;
		NamedQueryDefinition nqd = factory.getNamedQuery( queryOrNamedQuery );
		Query query = null;

		if (nqd != null) {
			query = getSession().getNamedQuery(queryOrNamedQuery);
		} else {
			query = getSession().createQuery(queryOrNamedQuery);
		}

		setQueryValues(query, values);
		return query;
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象
	 *
	 * @param queryOrSqlQuery query 或者 NamedSQLQuery
	 * @param values
	 *            命名参数,按名称绑定.
	 *
	 * @return {@link org.hibernate.Query}
	 *
	 */
	protected SQLQuery createSQLQuery( String queryOrSqlQuery, Map<String, ?> values) {
		SQLQuery query = createSQLQuery(queryOrSqlQuery);
		if (values != null) {
			query.setProperties(values);
		}
		return query.addEntity(entityClass);
	}

	/**
	 * 根据查询SQL与参数列表创建SQLQuery对象
	 *
	 * @param queryOrNamedSQLQuery query 或者 NamedSQLQuery
	 * @param values 数量可变的参数,按顺序绑定.
	 *
	 * @return {@link org.hibernate.SQLQuery}
	 */
	protected SQLQuery createSQLQuery( String queryOrNamedSQLQuery,  Object... values) {
		Assert.hasText(queryOrNamedSQLQuery, "queryOrNamedSQLQuery不能为空");
		SessionFactoryImpl factory = (SessionFactoryImpl) sessionFactory;
		NamedSQLQueryDefinition nsqlqd = factory.getNamedSQLQuery( queryOrNamedSQLQuery );
		Query query = null;
		
		if (nsqlqd != null) {
			query = getSession().getNamedQuery(queryOrNamedSQLQuery);
		} else {
			query = getSession().createSQLQuery(queryOrNamedSQLQuery);
		}
		
		setQueryValues(query, values);
		SQLQuery sqlQuery = (SQLQuery)query;
		
		return sqlQuery.addEntity(entityClass);
	}
	
	/**
	 * 设置参数值到query的hql中
	 *
	 * @param query Hibernate Query
	 * @param values 参数值可变数组
	 */
	protected void setQueryValues(Query query ,Object... values) {
		if (ArrayUtils.isEmpty(values)) {
			return ;
		}
		AbstractQueryImpl impl = (AbstractQueryImpl) query;
		String[] params = impl.getNamedParameters();
		
		int methodParameterPosition = params.length - 1;
		
		if (impl.hasNamedParameters()) {
			for (String p : params) {
				Object o = values[methodParameterPosition--];
				query.setParameter(p, o);
			}
		} else {
			for (Integer i = 0; i < values.length; i++) {
				query.setParameter(i, values[i]);
			}
		}
	}
	
	/**
	 * 通过排序表达式向Criteria设置排序方式,
	 * @param criteria Criteria
	 * @param orders 排序表达式，规则为:属性名称_排序规则,如:property_asc或property_desc,可以支持多个属性排序，用逗号分割,如:"property1_asc,proerty2_desc",也可以"property"不加排序规则时默认是desc
	 */
	protected void setOrderToCriteria(Criteria criteria, Order...orders) {
		if (ArrayUtils.isEmpty(orders)) {
			return ;
		}
		for (Order o : orders) {
			criteria.addOrder(o);
		}
	}
	
	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.使用命名方式参数
	 * 
	 * <pre>
	 * 	from object o where o.property = :proprty and o.property = :proprty
	 * </pre>
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 * 
	 * @param queryString hql
	 * @param values 值
	 * 
	 * @return long
	 */
	protected Long countHqlResult(String queryString,  Map<String, ?> values) {
		String countHql = prepareCountHql(queryString);

		try {
			return (Long)createQuery(countHql, values).uniqueResult();
		} catch (Exception e) {
			throw new RuntimeException("hql不能自动计算总是:"+ countHql, e);
		}
	}
	
	/**
	 * 执行count查询获得本次Hql查询所能获得的对象总数.(使用jdbc方式参数)
	 * 
	 * <pre>
	 * 	from object o where o.property = ? and o.property = ?
	 * </pre>
	 * 
	 * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
	 * 
	 * @param queryString hql
	 * @param values 值
	 * 
	 * @return long
	 */
	protected Long countHqlResult(String queryString,  Object... values) {
		String countHql = prepareCountHql(queryString);

		try {
			Object result = createQuery(countHql, values).uniqueResult();
			return (Long)result;
		} catch (Exception e) {
			throw new RuntimeException("hql不能自动计算总数:"+ countHql, e);
		}
	}
	
	/**
	 * 绑定计算总数HQL语句,返回绑定后的hql字符串
	 * 
	 * @param orgHql hql
	 * 
	 * @return String
	 */
	private String prepareCountHql(String orgHql) {
		String countField = StringUtils.substringBetween(orgHql, "select", "from");
		String countHql = MessageFormat.format("select count ({0}) {1} ",
										StringUtils.isEmpty(countField) ? "*" : countField,
										removeSelect(removeOrders(orgHql)));
		return countHql;
	}
	
	/**
	 * 移除from前面的select 字段 返回移除后的hql字符串
	 * @param hql 
	 * @return String
	 */
	private String removeSelect(String hql) {
		int beginPos = hql.toLowerCase().indexOf("from");
		return hql.substring(beginPos);
	}
	
	/**
	 * 删除hql中的 order by的字段,返回删除后的新字符串
	 * 
	 * @param hql
	 * 
	 * @return String
	 */
	private String removeOrders(String hql) {
		Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(hql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	/**
	 * 为Query添加distinct transformer,讲查询出来的重复数据进行distinct处理
	 * 
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 值
	 * 
	 * @return List
	 */
	public <X> List<X> distinct(String queryOrNamedQuery,Object... values) {
		Query query = createQuery(queryOrNamedQuery, values);
		query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		List<X> result = query.list();
		
		if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
			return Lists.newArrayList();
		} 
		
		return result;
	}
	
	/**
	 * 为Query添加distinct transformer,讲查询出来的重复数据进行distinct处理
	 * 
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 值
	 * 
	 * @return List
	 */
	public <X> List<X> distinct(String queryOrNamedQuery,Map<String, Object> values) {
		Query query = createQuery(queryOrNamedQuery, values);
		query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		List<X> result = query.list();
		
		if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
			return Lists.newArrayList();
		}
		
		return result;
	}

	/**
	 * Flush当前Session.
	 */
	public void flush() {
		getSession().flush();
	}
	
	/**
	 * 如果session中存在相同持久化识别的实例，用给出的对象的状态覆盖持久化实例
	 * 
	 * @param entity 持久化实例
	 */
	public void merge(T entity) {
		getSession().merge(entity);
	}
	
	/**
	 * 如果session中存在相同持久化识别的实例，用给出的对象的状态覆盖持久化实例
	 * 
	 * @param entity 持久化实例
	 * @param entityName 持久化对象名称
	 */
	public void merge(String entityName,T entity) {
		getSession().merge(entityName, entity);
	}
	
	/**
	 * 刷新操作对象
	 * 
	 * @param entity 操作对象
	 */
	public void refresh(T entity) {
		getSession().refresh(entity);
	}
	
	/**
	 * 刷新操作对象
	 * 
	 * @param entity 操作对象
	 * @param lockOptions Hibernate LockOptions
	 */
	public void refresh(T entity,LockOptions lockOptions) {
		
		if (lockOptions == null) {
			refresh(entity);
		} else {
			getSession().refresh(entity, lockOptions);
		}
	}
	
	/**
	 * 把操作对象在缓存区中直接清除
	 * 
	 * @param entity 操作对象
	 */
	public void evict(T entity) {
		getSession().evict(entity);
	}
	
	/**
	 * 把session所有缓存区的对象全部清除，但不包括正在操作中的对象
	 */
	public void clear() {
		getSession().clear();
	}
	
	/**
	 * 对于已经手动给ID主键的操作对象进行insert操作
	 * 
	 * @param entity 操作对象
	 * @param replicationMode 创建策略
	 */
	public void replicate(T entity, ReplicationMode replicationMode) {
		getSession().replicate(entity, replicationMode);
	}
	
	/**
	 * 对于已经手动给ID主键的操作对象进行insert操作
	 * 
	 * @param entityName 操作对象名称
	 * @param entity 操作对象
	 * @param replicationMode 创建策略
	 */
	public void replicate(String entityName,T entity, ReplicationMode replicationMode) {
		getSession().replicate(entityName,entity, replicationMode);
	}
	
	/**
	 * 把一个瞬态的实例持久化，但很有可能不能立即持久化实例，可能会在flush的时候才会持久化
	 * 当它在一个transaction外部被调用的时候并不会触发insert。
	 * 
	 * @param entity 瞬态的实例
	 */
	public void persist(T entity) {
		getSession().persist(entity);
	}
	
	/**
	 * 把一个瞬态的实例持久化，但很有可能不能立即持久化实例，可能会在flush的时候才会持久化
	 * 当它在一个transaction外部被调用的时候并不会触发insert。
	 * 
	 * @param entity 瞬态的实例
	 * @param entityName 瞬态的实例名称
	 */
	public void persist(String entityName, T entity) {
		getSession().persist(entityName,entity);
	}
	
	/**
	 * 从当前Session中获取一个能够操作JDBC的Connection并执行想要操作的JDBC语句
	 * 
	 * @param work Hibernate Work
	 */
	public void doWork(Work work) {
		getSession().doWork(work);
	}
	
	/**
	 * 判断entity实例是否已经与session缓存关联,是返回true,否则返回false
	 * 
	 * @param entity 实例
	 * 
	 * @return boolean
	 */
	public boolean contains(Object entity) {
		return getSession().contains(entity);
	}
	
	/**
	 * 执行HQL进行批量修改/删除操作.成功后返回更新记录数
	 * 
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 命名参数,按名称绑定.
	 *            
	 * @return int
	 */
	public int executeUpdate(String queryOrNamedQuery,  Map<String, ?> values) {
		return createQuery(queryOrNamedQuery, values).executeUpdate();
	}

	/**
	 * 执行HQL进行批量修改/删除操作.成功后更新记录数
	 * 
	 * @param queryOrNamedQuery hql 或者Hibernate的NamedQuery
	 * @param values 参数值
	 *            
	 * @return int
	 */
	public int executeUpdate(String queryOrNamedQuery,  Object... values) {
		return createQuery(queryOrNamedQuery, values).executeUpdate();
	}
}
