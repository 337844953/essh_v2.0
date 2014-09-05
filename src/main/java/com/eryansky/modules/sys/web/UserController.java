/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.PropertyFilter;
import com.eryansky.common.orm.entity.StatusState;
import com.eryansky.common.orm.hibernate.EntityManager;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.BaseController;
import com.eryansky.core.excelTools.ExcelUtils;
import com.eryansky.core.excelTools.JsGridReportBase;
import com.eryansky.core.excelTools.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys._enum.SexType;
import com.eryansky.modules.sys.entity.*;
import com.eryansky.modules.sys.service.*;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用户User管理 Controller层.
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2013-3-21 上午12:20:13
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "/sys/user")
public class UserController extends BaseController<User,Long> {


    @Autowired
    private UserManager userManager;
    @Autowired
    private OrganManager organManager;
    @Autowired
    private RoleManager roleManager;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private PostManager postManager;

    @Override
    public EntityManager<User, Long> getEntityManager() {
        return userManager;
    }


    @RequestMapping(value = {""})
    public String list() {
        return "modules/sys/user";
    }

    @RequestMapping(value = {"select"})
    public String selectPage(String userIds,Model model,Integer grade) {
        List<User> users = Lists.newArrayList();
        if (StringUtils.isNotBlank(userIds)) {
            String[] userIdss = userIds.split(",");
            List<Long> userIdLs = Lists.newArrayList();
            for (String userId : userIdss) {
                userIdLs.add(Long.valueOf(userId));
            }
            Criterion inUserCriterion = Restrictions.in("id",userIdLs);
            users = userManager.findByCriteria(inUserCriterion);
        }
        model.addAttribute("users", users);
        model.addAttribute("grade", grade);
//        model.addAttribute("userDatagridData", this.combogridSelectUser(null,null,null,"name",Page.ASC));
        model.addAttribute("userDatagridData", JsonMapper.getInstance().toJson(new Datagrid(users.size(),users),User.class,new String[]{"id","loginName","name","sexView","defaultOrganName"}));
        return "modules/sys/user-select";
    }

    @RequestMapping(value = {"combogridSelectUser"})
    @ResponseBody
    public String combogridSelectUser(Long organId, Long roleId,String loginNameOrName,String sort, String order) {
        List<User> users = userManager.getUsersByOrgOrRole(organId, roleId,loginNameOrName, sort, order);
        Datagrid<User> dg = new Datagrid<User>(users.size(), users);
        return JsonMapper.getInstance().toJson(dg,User.class,new String[]{"id","loginName","name","sexView"});
    }

    /**
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"input"})
    public String input(@ModelAttribute("model") User user) throws Exception {
        return "modules/sys/user-input";
    }


    @RequestMapping(value = {"_remove"})
    @ResponseBody
    @Override
    public Result remove(@RequestParam(value = "ids", required = false) List<Long> ids) {
        Result result;
        userManager.deleteByIds(ids);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }

    /**
     * 自定义查询
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"userDatagrid"})
    @ResponseBody
    public Datagrid<User> userDatagrid(Long organId,String organSysCode, String loginNameOrName,Integer userType,
                                       Integer page, Integer rows, String sort, String order) {
        //非管理员用户 添加机构系统编码
        if(StringUtils.isBlank(organSysCode)){
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            User sueperUser = userManager.getSuperUser();
            if(!sessionInfo.getUserId().equals(sueperUser.getId())){
                organSysCode = sessionInfo.getLoginOrganSysCode();
            }
        }
        Page<User> p = userManager.getUsersByQuery(organId,organSysCode, loginNameOrName,userType, page, rows, sort, order);
        Datagrid<User> dg = new Datagrid<User>(p.getTotalCount(), p.getResult());
        return dg;
    }

    /**
     * 用户combogrid所有
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"combogridAll"})
    @ResponseBody
    public String combogridAll() {
        List<PropertyFilter> filters = Lists.newArrayList();
        filters.add(new PropertyFilter("EQI_status", StatusState.normal.getValue().toString()));
        List<User> users = userManager.find(filters, "id", "asc");
        Datagrid<User> dg = new Datagrid<User>(users.size(), users);
        return JsonMapper.getInstance().toJson(dg,User.class,new String[]{"id","loginName","name","sexView","defaultOrganName"});
    }

    /**
     * 获取机构用户
     * @param organId 机构ID
     * @return
     */
    @RequestMapping(value = {"combogridOrganUser"})
    @ResponseBody
    public String combogridOrganUser(@RequestParam(value = "organId", required = true)Long organId) {
        List<User> users = userManager.getUsersByOrganId(organId);
        Datagrid dg = new Datagrid(users.size(),users);
        return JsonMapper.getInstance().toJson(dg,User.class,new String[]{"id","loginName","name","sexView","defaultOrganName"});
    }


    /**
     * combogrid
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"combogrid"})
    @ResponseBody
    public Datagrid<User> combogrid(@RequestParam(value = "ids", required = false)List<Long> ids, String loginNameOrName, Integer rows) throws Exception {
        Criterion statusCriterion = Restrictions.eq("status", StatusState.normal.getValue());
        Criterion[] criterions = new Criterion[0];
        criterions = (Criterion[]) ArrayUtils.add(criterions, 0, statusCriterion);
        Criterion criterion = null;
        if (Collections3.isNotEmpty(ids)) {
            //in条件
            Criterion inCriterion = Restrictions.in("id", ids);

            if (StringUtils.isNotBlank(loginNameOrName)) {
                Criterion loginNameCriterion = Restrictions.like("loginName", loginNameOrName, MatchMode.ANYWHERE);
                Criterion nameCriterion = Restrictions.like("name", loginNameOrName, MatchMode.ANYWHERE);
                Criterion criterion1 = Restrictions.or(loginNameCriterion, nameCriterion);
                criterion = Restrictions.or(inCriterion, criterion1);
            } else {
                criterion = inCriterion;
            }
            //合并查询条件
            criterions = (Criterion[]) ArrayUtils.add(criterions, 0, criterion);
        } else {
            if (StringUtils.isNotBlank(loginNameOrName)) {
                Criterion loginNameCriterion = Restrictions.like("loginName", loginNameOrName, MatchMode.ANYWHERE);
                Criterion nameCriterion = Restrictions.like("name", loginNameOrName, MatchMode.ANYWHERE);
                criterion = Restrictions.or(loginNameCriterion, nameCriterion);
                //合并查询条件
                criterions = (Criterion[]) ArrayUtils.add(criterions, 0, criterion);
            }
        }

        //分页查询
        Page<User> p = new Page<User>(rows);//分页对象
        p = userManager.findByCriteria(p, criterions);
        Datagrid<User> dg = new Datagrid<User>(p.getTotalCount(), p.getResult());
        return dg;
    }

    /**
     * 保存.
     */
    @RequestMapping(value = {"save"})
    @ResponseBody
    public Result save(@ModelAttribute("model") User user) {
        getEntityManager().evict(user);//如过本方法中有对model。setXX操作 则需执行evict方法 防止Hibernate session自动同步
        Result result = null;
        // 名称重复校验
        User nameCheckUser = userManager.getUserByLoginName(user.getLoginName());
        if (nameCheckUser != null && !nameCheckUser.getId().equals(user.getId())) {
            result = new Result(Result.WARN, "登录名为[" + user.getLoginName() + "]已存在,请修正!", "loginName");
            logger.debug(result.toString());
            return result;
        }

        if (user.getId() == null) {// 新增
            user.setPassword(Encrypt.e(user.getPassword()));
        } else {// 修改
            User superUser = userManager.getSuperUser();
            User sessionUser = userManager.getCurrentUser();
            if (!sessionUser.getId().equals(superUser.getId())) {
                result = new Result(Result.ERROR, "超级用户信息仅允许自己修改!",null);
                logger.debug(result.toString());
                return result;
            }
        }
        userManager.saveEntity(user);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }

    /**
     * 修改用户密码页面.
     */
    @RequestMapping(value = {"password"})
    public String password() throws Exception {
        return "modules/sys/user-password";

    }

    /**
     * 修改用户密码.
     * @param id 用户ID
     * @param upateOperate 需要密码"1" 不需要密码"0".
     * @param password 原始密码
     * @param newPassword 新密码
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"updateUserPassword"})
    @ResponseBody
    public Result updateUserPassword(@RequestParam(value = "id", required = true)Long id, @RequestParam(value = "upateOperate", required = true)String upateOperate, String password, @RequestParam(value = "newPassword", required = true)String newPassword) throws Exception {
        Result result;
        User u = userManager.loadById(id);
        if (u != null) {
            boolean isCheck = true;
            //需要输入原始密码
            if (AppConstants.USER_UPDATE_PASSWORD_YES.equals(upateOperate)) {
                String originalPassword = u.getPassword(); //数据库存储的原始密码
                String pagePassword = password; //页面输入的原始密码（未加密）
                if (!originalPassword.equals(Encrypt.e(pagePassword))) {
                    isCheck = false;
                }
            }
            //不需要输入原始密码
            if (AppConstants.USER_UPDATE_PASSWORD_NO.equals(upateOperate)) {
                isCheck = true;
            }
            if (isCheck) {
                u.setPassword(Encrypt.e(newPassword));
                userManager.saveEntity(u);
                result = Result.successResult();
            } else {
                result = new Result(Result.WARN, "原始密码输入错误.", "password");
            }
        } else {
            throw new ActionException("用户【"+id+"】不存在或已被删除.");
        }
        logger.debug(result.toString());
        return result;
    }


    /**
     * 修改用户角色页面.
     */
    @RequestMapping(value = {"role"})
    public String role() throws Exception {
        return "modules/sys/user-role";
    }

    /**
     * 修改用户角色.
     */
    @RequestMapping(value = {"updateUserRole"})
    @ResponseBody
    public Result updateUserRole(@ModelAttribute("model") User user, @RequestParam(value = "roleIds", required = false)List<Long> roleIds) throws Exception {
        getEntityManager().evict(user);
        Result result = null;
        List<Role> rs = Lists.newArrayList();
        if(Collections3.isNotEmpty(roleIds)){
            for (Long id : roleIds) {
                Role role = roleManager.loadById(id);
                rs.add(role);
            }
        }

        user.setRoles(rs);
        userManager.saveEntity(user);
        result = Result.successResult();
        return result;
    }

    /**
     * 设置组织机构页面.
     */
    @RequestMapping(value = {"organ"})
    public String organ(@ModelAttribute("model") User user, Model model) throws Exception {
        //设置默认组织机构初始值
        List<Combobox> defaultOrganCombobox = Lists.newArrayList();
        if (user.getId() != null) {
            List<Organ> organs = user.getOrgans();
            Combobox combobox;
            if (!Collections3.isEmpty(organs)) {
                for (Organ organ : organs) {
                    combobox = new Combobox(organ.getId().toString(), organ.getName());
                    defaultOrganCombobox.add(combobox);
                }
            }
        }
        String defaultOrganComboboxData = JsonMapper.nonDefaultMapper().toJson(defaultOrganCombobox);
        logger.debug(defaultOrganComboboxData);
        model.addAttribute("defaultOrganComboboxData", defaultOrganComboboxData);
        return "modules/sys/user-organ";
    }

    /**
     * 设置用户组织机构.
     */
    @RequestMapping(value = {"updateUserOrgan"})
    @ResponseBody
    public Result updateUserOrgan(@ModelAttribute("model") User model, @RequestParam(value = "organIds", required = false)List<Long> organIds, String defaultOrganId) throws Exception {
        getEntityManager().evict(model);
        Result result = null;
        List<Organ> oldOrgans = model.getOrgans();
        //绑定组织机构
        model.setOrgans(null);
        List<Organ> organs = Lists.newArrayList();
        if(Collections3.isNotEmpty(organIds)){
            for (Long organId : organIds) {
                Organ organ = organManager.loadById(organId);
                organs.add(organ);
                if(Collections3.isNotEmpty(oldOrgans)){
                    Iterator<Organ> iterator = oldOrgans.iterator();
                    while (iterator.hasNext()){
                        Organ oldOrgan = iterator.next();
                        if(oldOrgan.getId().equals(organ.getId())){
                            iterator.remove();
                        }
                    }

                }
            }
        }


        //去除用户已删除机构下的岗位信息
        List<Post> userPosts = model.getPosts();
        if(Collections3.isNotEmpty(oldOrgans)){//已删除的机构
            Iterator<Organ> iterator = oldOrgans.iterator();
            while (iterator.hasNext()){
                Organ oldOrgan = iterator.next();
                List<Post> organPosts = oldOrgan.getPosts();
                for(Post organPost:organPosts){
                    if(Collections3.isNotEmpty(userPosts)){
                        Iterator<Post> iteratorPost = userPosts.iterator();
                        while (iteratorPost.hasNext()){
                            Post userPost = iteratorPost.next();
                            if(userPost.getId().equals(organPost.getId())){
                                iteratorPost.remove();
                            }
                        }
                    }
                }
            }

        }


        model.setOrgans(organs);

        //绑定默认组织机构
        model.setDefaultOrgan(null);
        Organ defaultOrgan = null;
        if (defaultOrganId != null) {
            defaultOrgan = organManager.loadById(model.getDefaultOrganId());
        }
        model.setDefaultOrgan(defaultOrgan);

        userManager.saveEntity(model);
        result = Result.successResult();
        return result;
    }

    /**
     * 设置用户岗位页面.
     */
    @RequestMapping(value = {"post"})
    public String post() throws Exception {
        return "modules/sys/user-post";
    }

    /**
     * 修改用户岗位.
     */
    @RequestMapping(value = {"updateUserPost"})
    @ResponseBody
    public Result updateUserPost(@ModelAttribute("model") User model, @RequestParam(value = "postIds", required = false)List<Long> postIds) throws Exception {
        getEntityManager().evict(model);
        Result result = null;
        List<Post> ps = Lists.newArrayList();
        if(Collections3.isNotEmpty(postIds)){
            for (Long id : postIds) {
                Post post = postManager.loadById(id);
                ps.add(post);
            }
        }

        model.setPosts(ps);

        getEntityManager().saveEntity(model);
        result = Result.successResult();
        return result;
    }

    /**
     * 修改用户资源页面.
     */
    @RequestMapping(value = {"resource"})
    public String resource(Model model) throws Exception {
        List<TreeNode> treeNodes = resourceManager.getResourceTree(null, true);
        String resourceComboboxData = JsonMapper.nonDefaultMapper().toJson(treeNodes);
        logger.debug(resourceComboboxData);
        model.addAttribute("resourceComboboxData", resourceComboboxData);
        return "modules/sys/user-resource";
    }

    /**
     * 修改用户资源.
     */
    @RequestMapping(value = {"updateUserResource"})
    @ResponseBody
    public Result updateUserResource(@ModelAttribute("model") User user, @RequestParam(value = "resourceIds", required = false)List<Long> resourceIds) throws Exception {
        getEntityManager().evict(user);
        Result result = null;
        List<Resource> rs = Lists.newArrayList();
        if(Collections3.isNotEmpty(resourceIds)){
            for (Long id : resourceIds) {
                Resource resource = resourceManager.loadById(id);
                rs.add(resource);
            }
        }

        user.setResources(rs);
        userManager.saveEntity(user);
        result = Result.successResult();
        return result;

    }

    /**
     * 性别下拉框
     *
     * @throws Exception
     */
    @RequestMapping(value = {"sexTypeCombobox"})
    @ResponseBody
    public List<Combobox> sexTypeCombobox(String selectType) throws Exception {
        List<Combobox> cList = Lists.newArrayList();

        //为combobox添加  "---全部---"、"---请选择---"
        if (!StringUtils.isBlank(selectType)) {
            SelectType s = SelectType.getSelectTypeValue(selectType);
            if (s != null) {
                Combobox selectCombobox = new Combobox("", s.getDescription());
                cList.add(selectCombobox);
            }
        }
        SexType[] _enums = SexType.values();
        for (int i = 0; i < _enums.length; i++) {
            Combobox combobox = new Combobox(_enums[i].getValue().toString(), _enums[i].getDescription());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 多Sheet Excel导出，获取的数据格式是List<Object[]>
     * @return
     * @throws Exception
     */
    @RequestMapping("export")
    public void export(HttpServletRequest request, HttpServletResponse response) throws Exception{
        response.setContentType("application/msexcel;charset=UTF-8");
        List<User> users = userManager.getAllNormal();

        List<Object[]> list = new ArrayList<Object[]>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()){
            User user = iterator.next();
            list.add(new Object[]{user.getLoginName(),user.getName(),user.getSexView(),user.getAddress(),user.getDefaultOrganName()});
        }

        List<TableData> tds = new ArrayList<TableData>();

        //Sheet1
        String[] parents = new String[] {"", "基本信息", "",""};//父表头数组
        String[][] children = new String[][] {
                new String[]{"登录名"},
                new String[]{"姓名", "性别"},
                new String[]{"地址"},
                new String[]{"部门"}};//子表头数组
        String[] fields = new String[] {"loginName", "name", "sexView", "address","defaultOrganName"};//People对象属性数组
        TableData td = ExcelUtils.createTableData(users, ExcelUtils.createTableHeader(parents, children), fields);
        td.setSheetTitle("合并表头示例");
        tds.add(td);

        //Sheet2
        String[] hearders = new String[] {"登录名", "姓名", "性别", "地址","部门"};//表头数组
        td = ExcelUtils.createTableData(users, ExcelUtils.createTableHeader(hearders),fields);
        td.setSheetTitle("普通表头示例");
        tds.add(td);

        String title = "用户信息导出示例";
        JsGridReportBase report = new JsGridReportBase(request, response);
        report.exportToExcel(title, SecurityUtils.getCurrentSessionInfo().getLoginName(), tds);

    }
}
