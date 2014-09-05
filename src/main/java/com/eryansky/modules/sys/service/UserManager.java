/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.DaoException;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.PropertyFilter;
import com.eryansky.common.orm.entity.StatusState;
import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.orm.hibernate.HibernateDao;
import com.eryansky.common.orm.hibernate.Parameter;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.entity.Organ;
import com.eryansky.modules.sys.entity.Role;
import com.eryansky.modules.sys.entity.User;
import com.eryansky.utils.CacheConstants;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理User Service层实现类.
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2013-3-20 上午11:22:13 
 *
 */
@Service
public class UserManager extends EntityManager<User, Long> {
	
	private HibernateDao<User, Long> userDao;

    @Autowired
    private OrganManager organManager;
    @Autowired
    private RoleManager roleManager;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		userDao = new HibernateDao<User, Long>(sessionFactory, User.class);
	}
	
	@Override
	protected HibernateDao<User, Long> getEntityDao() {
		return userDao;
	}

    /**
     * 新增或修改角色.
     * <br>修改角色的时候 会给角色重新授权菜单 更新导航菜单缓存.
     */
    @CacheEvict(value = {  CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_CACHE},allEntries = true)
    public void saveOrUpdate(User entity) throws DaoException,SystemException,ServiceException {
        logger.debug("清空缓存:{}",CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                +","+CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                +","+CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                +","+CacheConstants.ORGAN_USER_TREE_CACHE);
        userDao.saveOrUpdate(entity);
    }

    /**
     * 新增或修改角色.
     * <br>修改角色的时候 会给角色重新授权菜单 更新导航菜单缓存.
     */
    @CacheEvict(value = {  CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_CACHE},allEntries = true)
    public void merge(User entity) throws DaoException,SystemException,ServiceException {
        logger.debug("清空缓存:{}",CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                +","+CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                +","+CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                +","+CacheConstants.ORGAN_USER_TREE_CACHE);
        userDao.merge(entity);
    }

    /**
     * 新增或修改角色.
     * <br>修改角色的时候 会给角色重新授权菜单 更新导航菜单缓存.
     */
    @CacheEvict(value = {  CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_CACHE},allEntries = true)
    @Override
    public void saveEntity(User entity) throws DaoException, SystemException, ServiceException {
        logger.debug("清空缓存:{}",CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                +","+CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                +","+CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                +","+CacheConstants.ORGAN_USER_TREE_CACHE);
        super.saveEntity(entity);
    }

    /**
	 * 自定义删除方法.
	 */
    @CacheEvict(value = {  CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_CACHE},allEntries = true)
	public void deleteByIds(List<Long> ids) throws DaoException,SystemException,ServiceException {
        logger.debug("清空缓存:{}",CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                +","+CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                +","+CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                +","+CacheConstants.ORGAN_USER_TREE_CACHE);
		if(!Collections3.isEmpty(ids)){
			for(Long id :ids){
				User superUser = this.getSuperUser();
				if (id.equals(superUser.getId())) {
					throw new SystemException("不允许删除超级用户!");
				}
				User user = userDao.get(id);
				if(user != null){
					//清空关联关系
                    user.setDefaultOrgan(null);
                    user.setOrgans(null);
					user.setRoles(null);
                    user.setResources(null);
					//逻辑删除
					//手工方式(此处不使用 由注解方式实现逻辑删除)
//					user.setStatus(StatusState.delete.getValue());
					//注解方式 由注解设置用户状态
					userDao.delete(user);
				}
			}
		}else{
			logger.warn("参数[ids]为空.");
		}
	}
	
	/**
	 * 得到当前登录用户.
	 * @return
	 * @throws DaoException
	 * @throws SystemException
	 * @throws ServiceException
	 */
	public User getCurrentUser() throws DaoException,SystemException,ServiceException{
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User user = getEntityDao().load(sessionInfo.getUserId());
        return user;
    }

	/**
	 * 得到超级用户.
	 *
	 * @return
	 * @throws DaoException
	 * @throws SystemException
	 * @throws ServiceException
	 */
	public User getSuperUser() throws DaoException,SystemException,ServiceException {
        User superUser = userDao.load(1l);//超级用户ID为1
        if(superUser == null){
            throw new SystemException("系统未设置超级用户.");
        }
        return superUser;
	}

    /**
     * 判断当前用户是否是超级用户
     * @param userId 用户Id
     * @return
     * @throws DaoException
     * @throws SystemException
     * @throws ServiceException
     */
    public boolean isSuperUser(Long userId) throws DaoException,SystemException,ServiceException{
        boolean flag = false;
        User user = getEntityDao().load(userId);
        User superUser = getSuperUser();

        if(user != null && user.getId().equals(superUser.getId())){
            flag = true;
        }
        return flag;
    }

	/**
	 * 根据登录名、密码查找用户.
	 * <br/>排除已删除的用户
	 * @param loginName
	 *            登录名
	 * @param password
	 *            密码
	 * @return
	 * @throws DaoException
	 * @throws SystemException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public User getUserByLP(String loginName, String password)
			throws DaoException,SystemException,ServiceException {
		Assert.notNull(loginName, "参数[loginName]为空!");
		Assert.notNull(password, "参数[password]为空!");
        Parameter parameter = new Parameter(loginName, password,StatusState.delete.getValue());
		List<User> list = userDao.find(
					"from User u where u.loginName = :p1 and u.password = :p2 and u.status <> :p3",parameter);
		return list.isEmpty() ? null:list.get(0);
	}

	/**
	 * 根据登录名查找.
	 * <br>注：排除已删除的对象
	 * @param loginName 登录名
	 * @return
	 * @throws DaoException
	 * @throws SystemException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public User getUserByLoginName(String loginName)
			throws DaoException,SystemException,ServiceException {
		Assert.notNull(loginName, "参数[loginName]为空!");
		Assert.notNull(loginName, "参数[status]为空!");
        Parameter parameter = new Parameter(loginName, StatusState.delete.getValue());
        List<User> list = userDao.find(
                "from User u where u.loginName = :p1 and u.status <> :p2",parameter);
		return list.isEmpty() ? null:list.get(0);
	}

    /**
     * 获得所有可用用户
     * @return
     */
    public List<User> getAllNormal(){
        Parameter parameter = new Parameter(StatusState.normal.getValue());
        return userDao.find("from User u where u.status = :p1",parameter);
    }

    /**
     * 根据组织机构Id以及登录名或姓名分页查询
     * @param organId 组织机构ID （注；如果organSysCode参数不为空则 忽略organId）
     * @param organSysCode 组织机构系统编码
     * @param loginNameOrName 姓名或手机号码
     * @param page 第几页
     * @param rows 页大小
     * @param sort 排序字段
     * @param order 排序方式 增序:'asc',降序:'desc'
     * @return
     */
    public Page<User> getUsersByQuery(Long organId,String organSysCode, String loginNameOrName, Integer userType,int page, int rows, String sort, String order) {
        //条件都为空的时候能够查询出所有数据
        if(organId == null && StringUtils.isBlank(organSysCode) && StringUtils.isBlank(loginNameOrName) && userType == null){
            return super.find(page,rows,null,null,new ArrayList<PropertyFilter>());
        }
        Parameter parameter = new Parameter();
        StringBuilder hql = new StringBuilder();
        hql.append("select distinct(u) from User u left join u.organs uo  where u.status <> :status ");
        List<Long> organIds = null;
        parameter.put("status",StatusState.delete.getValue());
        if(StringUtils.isNotBlank(organSysCode)){
            Organ organ = organManager.findUniqueBy("sysCode",organSysCode);
            hql.append("and uo.sysCode like :sysCode ");
            parameter.put("sysCode",organ.getSysCode()+"%");
        }else {
            if(organId !=null){
                hql.append("and uo.id = :organId ");
                parameter.put("organId",organId);
            }
        }
        if(StringUtils.isNotBlank(loginNameOrName)){
            hql.append("and (u.loginName like :loginName or u.name like :name) ");
            parameter.put("loginName","%"+loginNameOrName+"%");
            parameter.put("name","%"+loginNameOrName+"%");
        }
        if(userType != null){
            hql.append("and u.userType = :userType ");
            parameter.put("userType",userType);
        }

        //设置分页
        Page<User> p = new Page<User>(rows);
        p.setPageNo(page);
        p = userDao.findPage(p,hql.toString(),parameter);
        return p;
    }

    /**
     * 根据组织机构Id以及登录名或姓名分页查询
     * @param inUserIds 用户id集合
     * @param loginNameOrName 姓名或手机号码
     * @param page 第几页
     * @param rows 页大小
     * @param sort 排序字段
     * @param order 排序方式 增序:'asc',降序:'desc'
     * @return
     */
    public Page<User> getUsersByQuery(List<Long> inUserIds, String loginNameOrName, int page, int rows, String sort, String order) {
        Parameter parameter = new Parameter();
        StringBuilder hql = new StringBuilder();
        hql.append("from User u where 1=1 ");
        if(!Collections3.isEmpty(inUserIds)){
            hql.append("and (u.id in (:inUserIds) ");
            parameter.put("inUserIds",inUserIds);
            if(StringUtils.isNotBlank(loginNameOrName)){
                hql.append("or u.loginName like :loginName or u.name like :name ) ");
                parameter.put("loginName","%"+loginNameOrName+"%");
                parameter.put("name","%"+loginNameOrName+"%");
            }
        }else{
            if(StringUtils.isNotBlank(loginNameOrName)){
                hql.append("and (u.loginName like :loginName or u.name like :name) ");
                parameter.put("loginName","%"+loginNameOrName+"%");
                parameter.put("name","%"+loginNameOrName+"%");
            }
        }

        hql.append("and u.status = :status ");
        parameter.put("status",StatusState.normal.getValue());
        //设置分页
        Page<User> p = new Page<User>(rows);
        p.setPageNo(page);
        p = userDao.findPage(p,hql.toString(),parameter);
        return p;
    }

    /**
     *
     * @param organId 机构ID
     * @param roleId 角色ID
     * @param loginNameOrName 登录名或姓名
     * @param sort
     * @param order
     * @return
     */
    public List<User> getUsersByOrgOrRole(Long organId, Long roleId, String loginNameOrName, String sort, String order) {
        Parameter parameter = new Parameter();
        StringBuilder hql = new StringBuilder();
        hql.append("from User u where u.status = :status ");
        parameter.put("status",StatusState.normal.getValue());
        if (organId != null) {
            Organ organ = organManager.loadById(organId);
            hql.append("and :organ in elements(u.organs) ");
            parameter.put("organ",organ);
        }
        if (roleId != null) {
            Role role = roleManager.loadById(roleId);
            hql.append("and :role in elements(u.roles) ");
            parameter.put("role",role);
        }
        if (StringUtils.isNotBlank(loginNameOrName)) {
            hql.append("and  (u.name like :name or loginName like :loginName) ");
            parameter.put("name","%" + loginNameOrName + "%");
            parameter.put("loginName","%" + loginNameOrName + "%");
        }
        List<User> users = userDao.find(hql.toString(), parameter);
        return users;
    }

    /**
     * 获取机构用户
     * @param organId
     * @return
     */
    public List<User> getUsersByOrganId(Long organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Organ organ  = organManager.loadById(organId);
        if(organ == null){
            throw new ServiceException("机构["+organId+"]不存在.");
        }
        List<User> users = organ.getUsers();
        return users;
    }
}
