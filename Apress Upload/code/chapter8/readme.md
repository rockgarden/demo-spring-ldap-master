# Object-Directory Mapping

- ODM 的基础知识。
- Spring LDAP ODM 实施。
企业 Java 开发人员采用面向对象 (OO) 技术来创建模块化的复杂应用程序。在 OO 范式中，对象是系统的中心，代表现实世界中的实体。每个对象都有一个身份、状态和行为。对象可以通过继承或组合与其他对象相关联。另一方面，LDAP 目录以分层树结构表示数据和关系。这种差异会导致对象-目录范式不匹配，并可能导致 OO 和目录环境之间的通信出现问题。
Spring LDAP 提供了一个 Object-Directory Mapping (ODM) 框架，它在对象和目录模型之间架起了一座桥梁。 ODM 框架允许我们在两个模型之间映射概念，并协调将 LDAP 目录条目自动转换为 Java 对象的过程。 ODM 类似于更熟悉的对象关系映射 (ORM) 方法，它弥合了对象和关系数据库世界之间的差距。 Hibernate 和 Toplink 等框架使 ORM 变得流行，并成为开发人员工具集的重要组成部分。
尽管 Spring LDAP ODM 与 ORM 共享相同的概念，但它确实有以下区别：
- 无法缓存 LDAP 条目。
- ODM 元数据通过类级别的注释来表达。
- 没有可用的 XML 配置。
- 不能延迟加载条目。
- HQL 等查询语言不存在。对象的加载是通过 DN 查找和标准 LDAP 搜索查询完成的。

## Spring ODM 基础知识

Spring LDAP ODM 作为独立于核心 LDAP 项目的模块分发。 要在项目中包含 Spring LDAP ODM，需要将以下依赖项添加到项目的 pom.xml 文件中：

```xml
<dependency>
    <groupId>org.springframework.ldap</groupId>
    <artifactId>spring-ldap-odm</artifactId>
    <version>${org.springframework.ldap.version}</version>
    <exclusions>
        <exclusion>
            <artifactId>commons-logging</artifactId>
            <groupId>commons-logging</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

Spring LDAP ODM 在 org.springframework.ldap.odm 包及其子包下可用。 Spring LDAP ODM 的核心类如图所示。

![Spring LDAP ODM core classes](../../resources/Spring_LDAP_ODM_core_classes.png)

LDAP ODM 的核心是提供通用搜索和 CRUD 操作的 OdmManager。 它充当中介并在 LDAP 条目和 Java 对象之间转换数据。 Java 对象被注释以提供转换元数据。 清单显示了 OdmManager API。

```java
Package org.springframeworkldap.odm.core;
import java.util.List;
import javax.naming.Name;
import javax.naming.directory.SearchControls;
public interface OdmManager {
   void create(Object entry);
   <T> T read(Class<T> clazz, Name dn);
   void update(Object entry);
   void delete(Object entry);
   <T> List<T> findAll(Class<T> clazz, Name base, SearchControls
   searchControls);
   <T> List<T> search(Class<T> clazz, Name base, String filter,
   SearchControls searchControls);
}
```

OdmManager 的创建、更新和删除方法采用 Java 对象并使用其中的信息来执行相应的 LDAP 操作。 read 方法采用两个参数，一个确定要返回的类型的 Java 类和一个用于查找 LDAP 条目的完全限定 DN。 OdmManager 可以看作是您在第 5 章中看到的 Generic DAO 模式的一个细微变化。
Spring LDAP ODM 提供了 OdmManager 的开箱即用实现，恰当地命名为 OdmManagerImpl。为了正常运行，OdmManagerImpl 使用以下三个对象：

- 用于与 LDAP 服务器通信的 ContextSource 实现。
- ConverterManager 实现，用于将 LDAP 数据类型转换为 Java 数据类型，反之亦然。
- 一组需要由ODM 实现管理的域类。
为了简化 OdmManagerImpl 实例的创建，框架提供了一个工厂 bean，OdmManagerImplFactoryBean。以下是创建 OdmManager 实例的必要配置：

```XML
<bean  id="odmManager" class="org.springframework.ldap.odm. core.impl.
OdmManagerImplFactoryBean">
    <property  name="converterManager" ref="converterManager"  />
    <property  name="contextSource" ref="contextSource" />
    <property  name="managedClasses">
        <set>
            <value>FULLY_QUALIFIED_CLASS_NAME</value>
        </set>
    </property>
</bean>
```

OdmManager 将 LDAP 属性到 Java 字段的转换管理（反之亦然）委托给 ConverterManager。 ConverterManager 本身依赖于一组 Converter 实例来进行实际的转换。 清单显示了 Converter 接口 API。 convert 方法接受一个对象作为其第一个参数，并将其转换为 toClass 参数指定的类型的实例。

```java
package org.springframework.ldap.odm.typeconversion.impl;
public interface Converter {
   <T> T convert(Object source, Class<T> toClass) throws Exception;
}
```

转换器的通用性使得创建特定的实现变得容易。 Spring LDAP ODM 提供了 Converter 接口的 ToStringConverter 实现，它将给定的源对象转换为字符串。 清单提供了 ToStringConverter API 实现。 如您所见，只需调用源对象的 toString 方法即可进行转换。

```java
package org.springframework.ldap.odm.typeconversion.impl.converters;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
public final class ToStringConverter implements Converter {
   public <T> T convert(Object source, Class<T> toClass) {
      return toClass.cast(source.toString());
} }
```

这个实现的反面是 FromStringConverter，它将 java.lang.String 对象转换为任何指定的 toClass 类型。 清单 8-4 提供了 FromStringConverter API 实现。 转换器实现通过调用 toClass 参数的构造函数并传入 String 对象来创建一个新实例。 toClass 类型参数必须有一个接受单个 java.lang.String 类型参数的公共构造函数。 例如，FromStringConverter 可以将 String 数据转换为 Integer 或 Long 数据类型。

```java
package org.springframework.ldap.odm.typeconversion.impl.converters;
import java.lang.reflect.Constructor;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
public final class FromStringConverter implements Converter {
   public <T> T convert(Object source, Class<T> toClass) throws Exception { 
      Constructor<T> constructor = toClass.getConstructor(java.lang.String.class); 
      return constructor.newInstance(source);
   }
}
```

这两个转换器类应该足以将大多数 LDAP 数据类型转换为常见的 Java 字段类型，例如 java.lang.Integer、java.lang.Byte 等，反之亦然。 清单 8-5 显示了创建 FromStringConverter 和 ToStringConverter 实例所涉及的 XML 配置。

```xml
<bean id="fromStringConverter" class="org.springframework.ldap.odm.
typeconversion.impl.converters.FromStringConverter" />
<bean id="toStringConverter" class="org.springframework.ldap.odm.
typeconversion.impl.converters.ToStringConverter" />
```

现在您已准备好创建一个 ConverterManager 实例并使用它注册上述两个转换器。 注册转换器包括指定转换器本身、指示转换器期望的源对象类型的 fromClass 和指示转换器将返回的类型的 toClass。 为了简化 Converter 注册过程，Spring ODM 提供了一个 ConverterConfig 类。 清单 8-6 显示了用于注册 toStringConverter 实例的 XML 配置。

```xml
<bean id="toStringConverter" class="org.springframework.ldap.odm.
typeconversion.impl.ConverterManagerFactoryBean$ConverterConfig">
   <property name="converter" ref="toStringConverter"/>
   <property name="fromClasses">
      <set>
         <value>java.lang.Integer</value>
      </set>
   </property>
   <property name="toClasses">
      <set>
         <value>java.lang.String</value>
      </set>
   </property>
</bean>
```

如您所见，ConverterConfig 是 org.springframework 的内部类。 ldap.odm.typeconversion.impl.ConverterManagerFactoryBean 类。 此配置告诉 ConverterManager 使用 toStringConverter bean 将 java.lang.Integer 类型转换为 String 类型。 在内部，转换器在使用以下算法计算的密钥下注册：
`key = fromClass.getName() + ":" + syntax + ":" + toClass. getName();`
有时您希望使用相同的转换器实例来转换多种数据类型。 例如，ToStringConverter 可用于转换其他类型，例如 java.lang.Long、java.lang.Byte、java.lang.Boolean 等。
为了解决这种情况，ConverterConfig 接受一组转换器可以处理的 from 和 to 类。 清单显示了接受多个 fromClass 的修改后的 ConverterConfig。

```xml
<bean id="toStringConverter" class="org.springframework.ldap.odm.
typeconversion.impl.ConverterManagerFactoryBean$ConverterConfig">
   <property name="converter" ref="toStringConverter" />
   <property name="fromClasses">
      <set>
         <value>java.lang.Byte</value>
         <value>java.lang.Integer</value>
         <value>java.lang.Boolean</value>
      </set>
   </property>
   <property name="toClasses">
      <set>
         <value>java.lang.String</value>
      </set>
   </property>
</bean>
```

上述 fromClasses 集中指定的每个类都将与 toClasses 集中的一个类配对，用于转换器注册。 因此，如果您指定 n fromClasses 和 m toClasses，它将导致转换器converter的 n*m 注册。 清单显示了 fromStringConverterConfig，它与之前的配置非常相似。

```xml
<bean id="fromStringConverterConfig" class="org.springframework.ldap.odm.
typeconversion.impl.ConverterManagerFactoryBean$ConverterConfig">
   <property name="converter" ref="fromStringConverter" />
   <property name="fromClasses">
      <set>
         <value>java.lang.String</value>
      </set>
   </property>
   <property name="toClasses">
      <set>
         <value>java.lang.Byte</value>
         <value>java.lang.Integer</value>
         <value>java.lang.Boolean</value>
      </set>
   </property>
</bean>
```

完成必要的转换器配置后，可以使用 ConverterManagerFactoryBean 创建新的 ConverterManager 实例。 清单显示了所需的 XML 声明。

```xml
<bean id="converterManager" class="org.springframework.ldap.odm.
typeconversion.impl.ConverterManagerFactoryBean">
   <property name="converterConfig">
      <set>
         <ref bean="fromStringConverterConfig"/>
         <ref bean="toStringConverterConfig"/>
      </set>
   </property>
</bean>
```

使用 ODM 框架所需的设置到此结束。 在接下来的部分中，您将了解对域类进行注释并将此配置用于 LDAP 读取和写入。 在你这样做之前，让我们回顾一下你到目前为止所做的事情（见图 8-2）。

![OdmManager inner workings](../../resources/OdmManager_inner_workings.png)

OdmManager 内部工作原理:

1. OdmManager 实例由 OdmManagerImplFactoryBean 创建。
2. OdmManager 使用 ConverterManager 实例在 LDAP 和 Java 类型之间进行转换。
3. 对于从特定类型到另一种特定类型的转换，ConverterManager 使用转换器。
4. ConverterManager 实例由 ConverterManagerFactoryBean 创建。
5. ConverterManagerFactoryBean 使用 ConverterConfig 实例来简化 Converter 注册。 ConverterConfig 类采用 fromClasses、toClasses 和伴随关系的转换器。The ConverterConfig class takes the fromClasses, toClasses, and the converter that goes along with the relationship.

## ODM Metadata

org.springframework.ldap.odm.annotations 包包含可用于将简单的 Java POJO 转换为 ODM 可管理实体的注释。 Patron.java   显示了您将转换为 ODM 实体的 Patron 类。

您将通过使用 @Entry 注释类来开始转换。 此标记注释告诉 ODM 管理器该类是一个实体。 它还用于提供实体映射到的 LDAP 中的 objectClass 定义。 见带 @Entry 注释的 Patron 类。

您需要添加的下一个注释是@Id。 此注释指定条目的 DN，并且只能放置在 javax.naming.Name 类的派生字段上。
为了解决这个问题，您将在 Patron 类中创建一个名为 dn 的新字段。 见带 @Id 注释的 Patron 属性。

Java Persistence API 中的 @Id 注释指定实体bean 的标识符属性。此外，它的位置决定了 JPA 提供者将用于映射的默认访问策略。如果 @Id 放置在字段上，则使用字段访问。如果将它放在 getter 方法上，将使用属性访问。但是，Spring LDAP ODM 只允许字段访问。
@Entry 和 @Id 是使Patron 类成为 ODM 实体的仅有的两个必需注释。默认情况下，Patron 实体类中的所有字段都将自动变为可持久化的。默认策略是在持久化或读取时使用实体字段的名称作为 LDAP 属性名称。在 Patron 类中，这将
因为字段名称和 LDAP 属性名称相同，所以适用于电话号码或邮件等属性。但这会导致诸如 firstName 和 fullName 之类的字段出现问题，因为它们的名称与 LDAP 属性名称不同。为了解决这个问题，ODM 提供了将实体字段映射到对象类字段的 @Attribute 注释。此注释允许您指定 LDAP 属性的名称、可选的语法 OID 和可选的类型声明。见显示了完全注释的 Patron 实体类。
有时您不想保留实体类的某些字段。 通常，这些涉及计算的字段。 此类字段可以使用 @Transient 注释进行注释，指示 OdmManager 应忽略该字段。

## ODM Service Class服务等级

基于 Spring 的企业应用程序通常有一个服务层来保存应用程序的业务逻辑。 服务层中的类将持久性细节委托给 DAO 或存储库层。 在第 5 章中，您使用 LdapTemplate 实现了一个 DAO。 在本节中，您将创建一个使用 OdmManager 作为 DAO 替代品的新服务类。 PatronService.java 显示了您将要实现的服务类的接口。
服务类实现如 PatronServiceImpl.java 所示。 在实现中，您注入了一个 OdmManager 实例。 创建和更新方法实现只是将调用委托给 OdmManager。 find 方法将传入的 id 参数转换为完全限定的 DN，并将实际检索委托给 OdmManager 的 read 方法。 最后，delete 方法使用 find 方法读取赞助人，并使用 OdmManager 的 delete 方法将其删除。

验证 PatronService 实现的 JUnit 测试如 PatronServiceImplTest.java 所示。

repositoryContext-test.xml 文件包含您目前看到的配置片段。

## Configuration Simplifications

示例 repositoryContext-test.xml 中的配置乍一看可能令人望而生畏。 因此，为了解决这个问题，让我们创建一个新的 ConverterManager 实现来简化配置过程。 DefaultConverterManagerImpl.java 代码显示了 DefaultConverterManagerImpl 类。 如您所见，它在其实现中使用了 ConverterManagerImpl 类。

使用这个类可以大大减少所需的配置，如 repositoryContext-test2.xml 所示。

### 创建自定义转换器

考虑您的 Patron 类使用 customPhoneNumber 类来存储读者的电话号码的场景。 现在，当需要持久化一个 Patron 类时，您需要将 PhoneNumber 类转换为 String 类型。 同样，当您从 LDAP 中读取 Patron 类时，电话属性中的数据需要转换为 PhoneNumber 类。 默认的 ToStringConverter 和 FromStringConverter 对这种转换没有用处。 新建  PhoneNumber.java 并修改 Patron 类，添加以下代码：

```java
public class Patron {
   ...
   @Attribute(name = "telephoneNumber") 
   private PhoneNumber phoneNumber;
   ...
   @Override
   public String toString() {
      return "Dn: " + dn + ", firstName: " + firstName + "," + " fullName: " + fullName + ", " + "Telephone Number: " + phoneNumber; }
}
```

要将 PhoneNumber 转换为 String，您需要创建一个新的 FromPhoneNumberConverter 转换器。 清单 FromPhoneNumberConverter.java 显示了实现。 该实现只涉及调用 toString 方法来执行转换。

接下来，您需要一个实现来将 LDAP 字符串属性转换为 Java PhoneNumber 类型。 为此，您需要创建 ToPhoneNumberConverter，如清单 ToPhoneNumberConverter 所示。

最后，绑定配置中的所有内容，如清单 repositoryContext-test3.xml 所示。

用于测试新添加的转换器的修改后的测试用例如清单 PatronServiceImplCustomTest.java 所示。

Spring LDAP 的对象目录映射 (ODM) 弥合了对象和目录模型之间的差距。
