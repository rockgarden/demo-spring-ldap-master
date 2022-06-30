# Advanced Spring LDAP

- JNDI 对象工厂的基础知识。
- 使用对象工厂的DAO 实现。

## JNDI 对象工厂

JNDI 提供了对象工厂的概念，这使得处理 LDAP 信息更加容易。 顾名思义，对象工厂将目录信息转换为对应用程序有意义的对象。 例如，使用对象工厂可以让搜索操作返回对象实例，例如 Patron 或 Employee，而不是普通的 javax.naming.NamingEnumeration。

下图描述了应用程序执行 LDAP 操作时所涉及的流程：
![JNDI/object factory flow](../../resources/JNDI_object_factory_flow.png)

与对象工厂结合使用。 该流程从应用程序调用搜索或查找操作开始。 JNDI API 将执行请求的操作并从 LDAP 检索条目。 然后将这些结果传递给已注册的对象工厂，后者将它们转换为对象。 这些对象被移交给应用程序。

处理 LDAP 的对象工厂需要实现 javax.naming.spi。 DirObjectFactory 接口。 PatronObjectFactory.java 显示了一个 Patron 对象工厂实现，它接受传入的信息并创建一个 Patron 实例。 getObjectInstance 方法的 obj 参数保存有关对象的引用信息。 name 参数保存对象的名称。 attrs 参数包含与对象关联的属性。 在 getObjectInstance 中，您读取所需的属性并填充新创建的 Patron 实例。

在开始使用此对象工厂之前，必须在初始上下文创建期间对其进行注册。 JndiObjectFactoryLookupExample.java 显示了在查找期间使用 PatronObjectFactory 的示例。 您使用 DirContext.OBJECT_FACTORIES 属性注册 PatronObjectFactory 类。 请注意，上下文的查找方法现在返回一个 Patron 实例。

## Spring and Object Factories

Spring LDAP 提供了一个开箱即用的 DirObjectFactory 实现，称为 org.springframework.ldap.core.support.DefaultDirObjectFactory。上一节中，PatronObjectFactory 从找到的上下文中创建 Patron 实例。同样， DefaultDirObjectFactory 从找到的上下文中创建 org.springframework.ldap.core.DirContextAdapter 的实例。
DirContextAdapter 类本质上是通用的，可以被视为 LDAP 条目数据的持有者。 DirContextAdapter 类提供了多种实用方法，极大地简化了获取和设置属性。正如您将在后面的部分中看到的，当对属性进行更改时，DirContextAdapter 会自动跟踪这些更改并简化更新 LDAP 条目的数据。 DirContextAdapter 和 DefaultDirObjectFactory 的简单性使您能够轻松地将 LDAP 数据转换为域对象，从而减少编写和注册大量对象工厂的需要。
在接下来的部分中，您将使用 DirContextAdapter 创建一个 Employee DAO，该 DAO 抽象了 Employee LDAP 条目的读写访问。

### DAO design pattern

如今，大多数 Java 和 JEE 应用程序都访问某种类型的持久性存储以进行日常活动。 持久存储从流行的关系数据库到 LDAP 目录再到遗留大型机系统各不相同。 根据持久存储的类型，获取和操作数据的机制会有很大差异。 这可能导致应用程序和数据访问代码之间的紧密耦合，从而难以在实现之间切换。 这是数据访问对象或 DAO 模式可以提供帮助的地方。
数据访问对象data Access Object是一种流行的核心 JEE 模式，它封装了对数据源的访问。 低级数据访问逻辑（例如连接到数据源和操作数据）被 DAO 干净地抽象到单独的层。 一个 dAO 实现通常包括以下内容：

1. 提供 CRUd 方法契约的 DAO 接口。
2. 使用特定于数据源的 API 的接口的具体实现。
3. DAO返回的领域对象或传输对象domain objects or transfer objects。

有了 DAO，应用程序的其余部分就不必担心底层数据实现，而可以专注于高级业务逻辑。

### 使用对象工厂Object Factories实现 DAO

通常，您在 Spring 应用程序中创建的 DAO 具有用作 DAO 合约的接口和包含访问数据存储或目录的实际逻辑的实现。 EmployeeDao.java 显示了您将要实现的 Employee DAO 的 EmployeeDao 接口。 DAO 具有用于修改员工信息的创建、更新和删除方法。 它还有两种查找方法，一种通过 id 检索员工，另一种返回所有员工。

之前的 EmployeeDao 接口使用 Employee 域对象。 Employee.java 显示了这个 Employee 域对象。 Employee 实现包含图书馆员工的所有重要属性。 请注意，您将使用 uid 属性作为对象的唯一标识符，而不是使用完全限定的 DN。

从 EmployeeDao 的基本实现开始，如 EmployeeDaoLdapImpl.java 所示。
在此实现中，您将注入 SimpleLdapTemplate 的一个实例。
SimpleLdapTemplate 的实际创建将在外部配置文件中完成， 即带有 SimpleLdapTemplate 和相关 bean 声明 repositoryContext.xml 文件。
该配置文件中，您将 LDAP 服务器信息提供给 LdapContextSource 以创建 contextSource bean。
通过将基数设置为“ou=employees,dc=inflinx,dc=com”，您已将所有 LDAP 操作限制在 LDAP 树的员工分支中。重要的是要了解使用此处创建的上下文将无法对分支“ou=patrons”进行搜索操作。如果要求搜索 LDAP 树的所有分支，则基本属性需要为空字符串。
LdapContextSource 的一个重要属性是 dirObjectFactory，可用于设置要使用的 DirObjectFactory。然而，在示例文件中，您没有使用此属性来指定您使用 DefaultDirObjectFactory 的意图。这是因为默认情况下 LdapContextSource 将 DefaultDirObjectFactory 注册为其 DirObjectFactory。
在配置文件的最后部分，您有 SimpleLdapTemplate bean 声明。您已将 LdapContextSource bean 作为构造函数参数传递给 SimpleLdapTemplate。
