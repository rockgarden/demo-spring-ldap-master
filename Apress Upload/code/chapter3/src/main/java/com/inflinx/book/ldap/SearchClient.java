package com.inflinx.book.ldap;

import java.util.List;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

@Component
public class SearchClient {

	/**
	 * AttributesMapper is a raw type. 
	 * The generic type AttributesMapper<T> should be parameterized by string type.
	 */
	@SuppressWarnings("unchecked")
	public List<String> search() {
		LdapTemplate ldapTemplate = getLdapTemplate();
		return ldapTemplate.search("dc=inflinx,dc=com", "(objectclass=person)",
				(AttributesMapper) attributes -> attributes.get("cn").get());
	}

	private LdapTemplate getLdapTemplate() {
		// 配置和创建 LDAP 服务器上的初始上下文实例
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl("ldap://localhost:1389");
		contextSource.setUserDn("cn=Directory Manager WK");
		contextSource.setPassword("passwordwk");
		try {
			contextSource.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 创建 LdapTemplate 类的实例
		LdapTemplate ldapTemplate = new LdapTemplate();
		ldapTemplate.setContextSource(contextSource);
		return ldapTemplate;
	}

	public static void main(String[] args) {
		SearchClient client = new SearchClient();
		List<String> names = client.search();
		for (String name : names) {
			System.out.println(name);
		}
	}
}
