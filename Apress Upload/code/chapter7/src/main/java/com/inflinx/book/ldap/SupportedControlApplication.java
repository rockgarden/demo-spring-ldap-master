package com.inflinx.book.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class SupportedControlApplication {

	public void displayControls() {
		String ldapUrl = "ldap://localhost:1389";
		try {
			Properties environment = new Properties();
			environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.setProperty(Context.PROVIDER_URL, ldapUrl);

			DirContext context = new InitialDirContext(environment);
			Attributes attributes = context.getAttributes("", new String[] { "supportedcontrol" });

			Attribute supportedControlAttribute = attributes.get("supportedcontrol");
			NamingEnumeration controlOIDList = supportedControlAttribute.getAll();
			while (controlOIDList != null && controlOIDList.hasMore()) {
				System.out.println(controlOIDList.next());
			}
			context.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SupportedControlApplication supportedControlApplication = new SupportedControlApplication();
		supportedControlApplication.displayControls();
	}
}