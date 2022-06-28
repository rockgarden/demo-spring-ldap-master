package com.inflinx.book.ldap;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

@Component
public class SearchClient {

	@SuppressWarnings("unchecked")
	public List<String> search() {
		LdapTemplate ldapTemplate = getLdapTemplate();
		return ldapTemplate.search("dc=inflinx,dc=com", "(objectclass=person)",
				new AttributesMapper() {
					@Override
					public Object mapFromAttributes(Attributes attributes) throws NamingException {
						return attributes.get("cn").get();
					}
				});
	}

	private LdapTemplate getLdapTemplate() {
		// 配置和创建 LDAP 服务器上的初始上下文实例
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl("ldap://localhost:11389");
		contextSource.setUserDn("cn=Directory Manager");
		contextSource.setPassword("opendj");
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
