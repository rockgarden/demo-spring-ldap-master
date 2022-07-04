package com.inflinx.book.ldap;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

@Component
public class LdapOperationsClient {
	@Autowired
	@Qualifier("ldapTemplate")
	private LdapTemplate ldapTemplate;

	private static final String DN_1 = "uid=patron999,ou=patrons,dc=inflinx,dc=com";

	/**
	 * New entry cannot be added because an entry with that name already exists;
	 * Remaining name 'uid=patron999,ou=patrons,dc=inflinx,dc=com
	 */
	public void addPatron() {
		// Set the Patron attributes
		Attributes attributes = new BasicAttributes();
		attributes.put("sn", "Patron999");
		attributes.put("cn", "New Patron999");

		// Add the multi-valued attribute
		BasicAttribute objectClassAttribute = new BasicAttribute("objectclass");
		objectClassAttribute.add("top");
		objectClassAttribute.add("person");
		objectClassAttribute.add("organizationalperson");
		objectClassAttribute.add("inetorgperson");
		attributes.put(objectClassAttribute);

		ldapTemplate.bind(DN_1, null, attributes);
	}

	public void addTelephoneNumber() {
		Attribute attribute = new BasicAttribute("telephoneNumber", "801 100 1000");
		ModificationItem item = new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
		ldapTemplate.modifyAttributes(DN_1, new ModificationItem[] { item });
	}

	public void removePatron() {
		ldapTemplate.unbind(DN_1);
	}

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		LdapOperationsClient client = context.getBean(LdapOperationsClient.class);
		client.addPatron();
		client.addTelephoneNumber();
		client.removePatron();
		((ClassPathXmlApplicationContext) context).close();
	}
}
