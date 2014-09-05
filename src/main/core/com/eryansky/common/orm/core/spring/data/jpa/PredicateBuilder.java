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
package com.eryansky.common.orm.core.spring.data.jpa;


import com.eryansky.common.orm.core.PropertyFilter;
import com.eryansky.common.orm.core.spring.data.jpa.specification.SpecificationEntity;

import javax.persistence.criteria.Predicate;

/**
 * 
 * 辅助{@link JpaRestrictionBuilder}类创建PropertyFilter后使用哪种约束条件向{@link javax.persistence.criteria.CriteriaBuilder}添加{@link javax.persistence.criteria.Predicate}进行条件过滤查询的接口
 *
 * @author maurice
 *
 */
public interface PredicateBuilder {

	/**
	 * 获取Jpa的约束标准
	 *
	 * @param filter 属性过滤器
	 * @param entity jpa查询绑定载体
	 *
	 * @return {@link javax.persistence.criteria.Predicate}
	 *
	 */
	public Predicate build(PropertyFilter filter, SpecificationEntity entity);

	/**
	 * 获取Predicate标准的约束名称
	 *
	 * @return String
	 */
	public String getRestrictionName();

	/**
	 * 获取Jpa的约束标准
	 *
	 * @param propertyName 属性名
	 * @param value 值
	 * @param entity jpa查询绑定载体
	 *
	 * @return {@link javax.persistence.criteria.Predicate}
	 * 
	 */
	public Predicate build(String propertyName, Object value, SpecificationEntity entity);
}
