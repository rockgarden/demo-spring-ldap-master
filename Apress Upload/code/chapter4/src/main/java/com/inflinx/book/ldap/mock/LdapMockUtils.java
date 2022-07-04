package com.inflinx.book.ldap.mock;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.ldap.core.DirContextOperations;

import static org.easymock.EasyMock.*;

public class LdapMockUtils {

	/**
	 * Utility classes, which are collections of static members, are not meant to be instantiated. 
	 * Even abstract utility classes, which can be extended, should not have public constructors.
	 * Java adds an implicit public constructor to every class which does not define at least one explicitly. 
	 * Hence, at least one non-public constructor should be defined. 
	 * Java会向不定义构造函数的每个类添加隐式的公共构造函数。因此，应该定义至少一个非公共构造函数。
	 */
	private LdapMockUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static DirContextOperations mockContextOperations(Map<String, Object> attributes) {
		DirContextOperations contextOperations = createMock(DirContextOperations.class);

		for (Entry<String, Object> entry : attributes.entrySet()) {
			// contextOperations.gets
			if (entry.getValue() instanceof String) {
				expect(contextOperations.getStringAttribute(eq(entry.getKey())))
						.andReturn((String) entry.getValue());
				expectLastCall().anyTimes();

			} else if (entry.getValue() instanceof String[]) {
				expect(contextOperations.getStringAttributes(eq(entry.getKey())))
						.andReturn((String[]) entry.getValue());
				expectLastCall().anyTimes();
			}
		}
		return contextOperations;
	}

}
