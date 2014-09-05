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
package com.eryansky.common.orm.core.spring.data.jpa.factory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * 创建srping data jpa repository接口代理类的FactoryBean
 * 
 * @author maurice
 *
 * @param <T> repository
 */
public class BasicRepositoryFactoryBean<T extends JpaRepository<Object, Serializable>> extends JpaRepositoryFactoryBean<T, Object, Serializable>{
	
	@Override
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
		
		return new BasicJpaRepositoryFactory(entityManager);
	}
}
