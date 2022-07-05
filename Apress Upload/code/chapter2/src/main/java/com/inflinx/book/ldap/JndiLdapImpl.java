package com.inflinx.book.ldap;

import java.util.Arrays;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.inflinx.book.ldap.domain.Employee;
import com.sun.jndi.ldap.LdapName;

public class JndiLdapImpl {

	private static final String BASE_PATH = "ou=employees,dc=inflinx,dc=com";

	// Connecting to LDAP
	private DirContext getContext() throws NamingException {
		Properties environment = new Properties();
		// 指定服务提供者类
		environment.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		// PROVIDER_URL 指定 LDAP 服务 URL, 协议 ldap 或 ldaps、LDAP 服务器主机名和端口
		environment.setProperty(Context.PROVIDER_URL, "ldap://localhost:1389");
		// SECURITY_AUTHENTICATION 属性设置为 simple，表示使用纯文本用户名和密码进行身份验证
		environment.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
		environment.setProperty(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
		environment.setProperty(Context.SECURITY_PRINCIPAL, "cn=Directory Manager WK");
		environment.setProperty(Context.SECURITY_CREDENTIALS, "passwordwk");
		return new InitialDirContext(environment);
	}

	// Closing Resources
	private void closeContext(DirContext context) {
		try {
			if (null != context) {
				context.close();
			}
		} catch (NamingException e) {
			// Ignore the exception
		}
	}

	public void search() {
		DirContext context = null;
		NamingEnumeration<SearchResult> searchResults = null;
		try {
			context = getContext();
			// 设置搜索元数据
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setReturningAttributes(new String[] { "givenName", "telephoneNumber" });
			// 三个参数：确定搜索起点的基数、缩小结果范围的过滤器和搜索控件。
			searchResults = context.search("dc=inflinx,dc=com",
					"(objectClass=inetOrgPerson)", searchControls);
			while (searchResults.hasMore()) {
				SearchResult result = searchResults.next();
				Attributes attributes = result.getAttributes();
				String firstName = (String) attributes.get("givenName").get();
				// Read the multi-valued attribute
				Attribute phoneAttribute = attributes.get("telephoneNumber");
				String[] phone = new String[phoneAttribute.size()];
				NamingEnumeration phoneValues = phoneAttribute.getAll();
				for (int i = 0; phoneValues.hasMore(); i++) {
					phone[i] = (String) phoneValues.next();
				}
				System.out.println(firstName + "> " + Arrays.toString(phone));
			}
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != searchResults) {
					searchResults.close();
				}
				closeContext(context);
			} catch (NamingException e) {
				// Ignore this
			}
		}
	}

	// 测试lookup
	private void lookup() {
		DirContext context = null;
		Object lookupResults = null;
		try {
			context = getContext();
			lookupResults = context.lookup("uid=emp1,ou=employees, dc=inflinx,dc=com");
			System.out.println(lookupResults);
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			closeContext(context);
		}
	}

	// Creating a New Entry
	public void addEmploye(Employee employee) {
		DirContext context = null;
		try {
			context = getContext();
			// BasicAttributes 来抽象一个属性集合
			Attributes attributes = new BasicAttributes();
			// 创建一组需要添加到条目的属性
			attributes.put(new BasicAttribute("objectClass", "inetOrgPerson"));
			attributes.put(new BasicAttribute("uid", employee.getUid()));
			attributes.put(new BasicAttribute("givenName", employee.getFirstName()));
			attributes.put(new BasicAttribute("surname", employee.getLastName()));
			attributes.put(new BasicAttribute("commonName", employee.getCommonName()));
			attributes.put(new BasicAttribute("departmentNumber", employee.getDepartmentNumber()));
			attributes.put(new BasicAttribute("mail", employee.getEmail()));
			attributes.put(new BasicAttribute("employeeNumber", employee.getEmployeeNumber()));
			Attribute phoneAttribute = new BasicAttribute("telephoneNumber");
			for (String phone : employee.getPhone()) {
				phoneAttribute.add(phone);
			}
			attributes.put(phoneAttribute);
			// 获取fully qualified DN（完全限定 DN）
			String dn = "uid=" + employee.getUid() + "," + BASE_PATH;
			// 添加条目 entry
			context.createSubcontext(dn, attributes);
		} catch (NamingException e) {
			// Handle the exception properly
			e.printStackTrace();
		} finally {
			closeContext(context);
		}
	}

	// 更新条目的
	public void update(String dn, ModificationItem[] items) {
		DirContext context = null;
		try {
			context = getContext();
			context.modifyAttributes(dn, items);
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			closeContext(context);
		}
	}

	// 删除条目
	public void remove(String dn) {
		DirContext context = null;
		try {
			context = getContext();
			context.destroySubcontext(dn);
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			closeContext(context);
		}
	}

	// 删除子树
	// 许多 LDAP 服务器不允许删除具有子条目的条目。 在这些服务器中，删除非叶条目将需要遍历子树并删除所有子条目，然后才可删除非叶子条目。
	public void removeSubTree(DirContext ctx, String root) throws NamingException {
		NamingEnumeration enumeration = null;
		try {
			enumeration = ctx.listBindings(root);
			while (enumeration.hasMore()) {
				Binding childEntry = (Binding) enumeration.next();
				LdapName childName = new LdapName(root);
				childName.add(childEntry.getName());
				try {
					ctx.destroySubcontext(childName);
				} catch (ContextNotEmptyException e) {
					removeSubTree(ctx, childName.toString());
					ctx.destroySubcontext(childName);
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				enumeration.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		JndiLdapImpl jli = new JndiLdapImpl();
		jli.search();
	}
}