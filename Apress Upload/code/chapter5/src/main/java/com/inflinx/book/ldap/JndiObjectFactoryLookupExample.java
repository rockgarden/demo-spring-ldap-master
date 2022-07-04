package com.inflinx.book.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.inflinx.book.ldap.domain.Patron;

public class JndiObjectFactoryLookupExample {

	/**
	 * Get OpenDJ Context
	 * Use static access with "javax.naming.Context" for "PROVIDER_URL" instead of javax.naming.directory.DirContext.
	 * In the interest of code clarity, static members of a base class should never be accessed using a derived type’s name.
	 * Doing so is confusing and could create the illusion that two different static members exist.
	 * @return
	 * @throws NamingException
	 */
	private LdapContext getContext() throws NamingException {
		Properties environment = new Properties();
		environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.setProperty(Context.PROVIDER_URL, "ldap://localhost:1389");
		environment.setProperty(Context.SECURITY_PRINCIPAL, "cn=Directory Manager WK");
		environment.setProperty(Context.SECURITY_CREDENTIALS, "passwordwk");
		environment.setProperty(Context.OBJECT_FACTORIES, "com.inflinx.book.ldap.PatronObjectFactory");
		return new InitialLdapContext(environment, null);
	}

	public Patron lookupPatron(String dn) {	
		Patron patron = null;	
		try {
			LdapContext context = getContext();
			patron = (Patron) context.lookup(dn);
		}
		catch(NamingException e) {
			e.printStackTrace();
		}
		return patron;
	}
	
	public static void main(String[] args) {
		JndiObjectFactoryLookupExample jle = new JndiObjectFactoryLookupExample();
		Patron p = jle.lookupPatron("uid=patron99,ou=patrons,dc=inflinx,dc=com");
		System.out.println(p);
	}
}
