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
package com.eryansky.common.orm.core.spring.data.jpa.restriction;

import com.eryansky.common.orm.core.PropertyFilter;
import com.eryansky.common.orm.core.spring.data.jpa.specification.SpecificationEntity;
import com.eryansky.common.utils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 * 对{@link com.eryansky.common.orm.core.PropertyFilter#getMatchValue()}的特殊情况值做处理，例如 in, not in, between的多值情况,
 * 该类值处理一种情况
 * 
 * <p>
 * 	例如:
 * </p>
 * 
 * INI_property = "1,2,3,4";
 * <p>
 * 会产生的sql为: property in (1,2,3,4)
 * 
 * @author maurice
 *
 */
public abstract class PredicateMultipleValueSupport extends PredicateSingleValueSupport{
	
	/**
	 * 将得到值与指定分割符号,分割,得到数组
	 *  
	 * @param value 值
	 * @param type 值类型
	 * 
	 * @return Object
	 */
	public Object convertMatchValue(String value, Class<?> type) {
		Assert.notNull(value, "值不能为空");
		String[] result = StringUtils.splitByWholeSeparator(value, getAndValueSeparator());
		
		return  ConvertUtils.convertToObject(result, type);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.github.dactiv.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport#build(com.github.dactiv.orm.core.PropertyFilter, com.github.dactiv.orm.core.spring.data.jpa.JpaBuilderModel)
	 */
	public Predicate build(PropertyFilter filter, SpecificationEntity entity) {
		Object value = convertMatchValue(filter.getMatchValue(), filter.getFieldType());
		Predicate predicate = null;
		
		if (filter.hasMultiplePropertyNames()) {
			Predicate orDisjunction = entity.getBuilder().disjunction();
			for (String propertyName:filter.getPropertyNames()) {
				orDisjunction.getExpressions().add(build(propertyName,value,entity));
			}
			predicate = orDisjunction;
		} else {
			predicate = build(filter.getSinglePropertyName(),value,entity);
		}
		
		return predicate;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.github.dactiv.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport#build(javax.persistence.criteria.Path, java.lang.Object, javax.persistence.criteria.CriteriaBuilder)
	 */
	public Predicate build(Path<?> expression, Object value,CriteriaBuilder builder) {
		return buildRestriction(expression,(Object[])value,builder);
	}
	
	/**
	 * 获取Jpa的约束标准
	 * 
	 * @param expression root路径
	 * @param values 值
	 * @param builder CriteriaBuilder
	 * 
	 * @return {@link javax.persistence.criteria.Predicate}
	 */
	public abstract Predicate buildRestriction(Path<?> expression,Object[] values,CriteriaBuilder builder);
}
