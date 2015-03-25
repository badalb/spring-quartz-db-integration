package com.test.persistence.config;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "resamFactory", transactionManagerRef = "resamTransactionManager", basePackages = { "com.test.domain.repositories" })

public class PersistenceConfig {

	@Autowired
	private Environment env;

	@Primary
	@Profile("dev")
	@Bean(name = "resamDataSource")
	public DataSource dataSourceDev() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(env
				.getProperty("resam.datasource.driverClassName"));
		dataSource.setUrl(env.getProperty("resam.datasource.url"));
		dataSource.setUsername(env.getProperty("resam.datasource.username"));
		dataSource.setPassword(env.getProperty("resam.datasource.password"));

		return dataSource;
	}

	@Primary
	@Bean(name = "resamDataSource")
	@Profile("production")
	public DataSource dataSource() {
		JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
		return dataSourceLookup.getDataSource("java:comp/env/jdbc/resam");
	}

	@Bean(name = "flyway")
	@Profile("dev")
	public Flyway getFlywayDev() {
		org.flywaydb.core.Flyway flyway = new Flyway();
		flyway.setDataSource(dataSourceDev());
		flyway.setInitOnMigrate(true);
		flyway.migrate();
		return flyway;
	}

	@Bean(name = "flyway")
	@Profile("production")
	public Flyway getFlyway() {
		org.flywaydb.core.Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource());
		flyway.setInitOnMigrate(true);
		flyway.migrate();
		return flyway;
	}

	@Bean(name = "resamEntityManager")
	public EntityManager entityManager() {
		return entityManagerFactory().createEntityManager();
	}

	@Primary
	@Bean(name = "resamFactory")
	@DependsOn("flyway")
	public EntityManagerFactory entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(Boolean.TRUE);
		vendorAdapter.setShowSql(Boolean.TRUE);
		factory.setDataSource(dataSource());
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("com.test.persistence.entities");
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect",
				env.getProperty("spring.jpa.properties.hibernate.dialect"));
		factory.setJpaProperties(jpaProperties);
		factory.setPersistenceUnitName("resamPersistenceUnit");
		factory.afterPropertiesSet();
		factory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
		return factory.getObject();
	}

	@Bean(name = "resamTransactionManager")
	public PlatformTransactionManager transactionManager() {
		EntityManagerFactory factory = entityManagerFactory();
		return new JpaTransactionManager(factory);
	}

}
