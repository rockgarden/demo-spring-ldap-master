package com.inflinx.book.ldap.repository;

import javax.naming.Binding;
import javax.naming.ContextNotEmptyException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.schema.BasicSchemaSpecification;

public class LdapUnitUtils {

	public static void loadData(ContextSource contextSource, Resource ldifFile) throws Exception {
		DirContext ctx = null;
		try {
			ctx = contextSource.getReadWriteContext();
			loadData(ctx, ldifFile);
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {
				// Never mind this
			}
		}
	}

	public static void loadData(DirContext ctx, Resource ldifFile) throws Exception {
		LdifParser parser = new LdifParser(ldifFile);
		parser.setRecordSpecification(new BasicSchemaSpecification());
		parser.open();
		LdapAttributes attributes;
		while (parser.hasMoreRecords()) {
			try {
				attributes = parser.getRecord();
				if (attributes != null && attributes.getDN() != null) {
					if (isEntryPresent(ctx, attributes.getDN())) {
						ctx.rebind(attributes.getDN(), attributes);
					} else {
						ctx.createSubcontext(attributes.getDN(), attributes);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		parser.close();
	}

	public static boolean isEntryPresent(DirContext context, Name name) {

		try {
			context.lookup(name);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void clearSubContexts(ContextSource contextSource, Name name) throws NamingException {
		DirContext ctx = null;
		try {
			ctx = contextSource.getReadWriteContext();
			clearSubContexts(ctx, name);
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {
				// Never mind this
			}
		}
	}

	public static void clearSubContexts(DirContext ctx, Name name) throws NamingException {
		NamingEnumeration enumeration = null;
		try {
			enumeration = ctx.listBindings(name);
			while (enumeration.hasMore()) {
				Binding element = (Binding) enumeration.next();
				LdapName childNameL = new LdapName(element.getName());
				System.out.println(childNameL);
				DistinguishedName childName = new DistinguishedName(element.getName());
				System.out.println(childName);
				childName.prepend((DistinguishedName) name);
				try {
					ctx.destroySubcontext(childName);
				} catch (ContextNotEmptyException e) {
					clearSubContexts(ctx, childName);
					ctx.destroySubcontext(childName);
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				enumeration.close();
			} catch (Exception e) {
				// Never mind this
			}
		}
	}

}
