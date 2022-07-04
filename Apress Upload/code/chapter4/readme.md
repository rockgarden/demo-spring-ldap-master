# Testing LDAP Code

- 单元/模拟/集成(unit/mock/integration)测试的基础。
- 使用嵌入式 LDAP 服务器进行测试。
- 使用 EasyMock 进行模拟测试。
- 生成测试数据。

测试是任何软件开发过程的一个重要方面。除了检测错误外，它还有助于验证是否满足所有要求以及软件是否按预期工作。无论是正式的还是非正式的，测试几乎都包含在软件开发过程的每个阶段。取决于正在测试的内容和为了测试背后的目的，我们最终得到了几种不同类型的测试。

开发人员最常见的测试是单元测试，它确保各个单元按预期工作。集成测试通常在单元测试之后，并关注之前测试过的组件之间的交互。开发人员通常参与创建自动化集成测试，尤其是处理数据库和目录的测试。接下来是系统测试，对完整的集成系统进行评估，以确保满足所有要求。性能和效率等非功能性要求也作为系统测试的一部分进行测试。验收测试通常在最后进行，以确保交付的软件满足客户/业务用户的需求。

The most common testing done by developers is unit testing, which ensures that individual units are working as expected. Integration testing usually follows unit testing and focuses on the interaction between previously tested components. Developers are usually involved in creating automated integration tests, especially the tests that deal with databases and directories. Next is system testing where the complete, integrated system is evaluated to ensure that all the requirements have been met. Non-functional requirements such as performance and efficiency are also tested as part of system testing. Acceptance testing is usually done at the end to make sure that the delivered software meets the customer/ business user needs.

## 单元测试

单元测试是一种测试方法，其中应用程序的最小部分（称为单元）单独进行验证和验证。 在结构化编程中，单元可以是单独的方法或函数。 在面向对象编程 (OOP) 中，对象是最小的可执行单元。 对象之间的交互是任何 OO 设计的核心，通常通过调用方法来完成。 因此，OOP 中的单元测试范围可以从测试单个方法到测试一组对象。

> 请注意，衡量单元测试覆盖了多少代码很重要。Clover 和 Emma 等工具提供代码覆盖率指标。这些指标也可用于突出显示由少数单元测试（或根本没有）执行的任何路径。These metrics can also be used to highlight any paths that are exercised by few unit tests (or none at all).

单元测试的最大优势在于它可以帮助在开发的早期阶段识别错误。仅在 QA 或生产中发现的错误会消耗更多的调试时间和金钱。此外，一组好的单元测试可以作为安全网，并在重构代码时提供信心。单元测试可以帮助改进设计，甚至可以作为文档。
好的单元测试具有以下特点：

- 每个单元测试必须独立于其他测试。这种原子性非常重要，每个测试都不得对其他测试造成任何副作用。单元测试也应该是顺序无关的。
- 单元测试必须是可重复的。为了使单元测试具有任何价值，它必须产生一致的结果。否则，它不能在重构期间用作健全性检查。
- 单元测试必须易于设置和清理。所以他们不应该依赖外部系统，比如数据库和服务器。
- 单元测试必须快速并提供即时反馈。在进行另一项更改之前等待长时间运行的测试不会有成效。
- 单元测试必须是自我验证的。每个测试都应该包含足够的信息来自动确定测试是通过还是失败。不需要人工干预来解释结果。

企业应用程序经常使用外部系统，如数据库、目录、和网络服务。在 DAO 层中尤其如此。例如，单元测试数据库代码可能涉及启动数据库服务器、加载模式和数据、运行测试以及关闭服务器。这很快变得棘手和复杂。一种方法是使用模拟对象并隐藏外部依赖项。如果这还不够，可能需要使用集成测试并在外部依赖项完好无损的情况下测试代码。

## 模拟测试

模拟测试的目标是使用模拟对象以受控方式模拟真实对象。 模拟对象实现与真实对象相同的接口，但被编写成模仿/伪造和跟踪它们的行为的脚本。Mock objects implement the same interface as that of the real objects but are scripted to mimic/fake and track their behavior.

例如，考虑一个具有创建新用户帐户的方法的 UserAccountService。此类服务的实施通常涉及根据业务规则验证帐户信息，将新创建的帐户存储在数据库中，并发送确认电子邮件。持久化数据和电子邮件信息通常被抽象到其他层的类中。现在，在编写单元测试来验证与帐户创建相关的业务规则时，您可能并不真正关心电子邮件通知部分涉及的复杂性。但是，您确实想验证是否生成了电子邮件。这正是模拟对象派上用场的地方。
为此，您只需为 UserAccountService 提供一个负责发送电子邮件的 EmailService 的模拟实现。模拟实现将简单地标记电子邮件请求并返回硬编码结果。模拟对象是将测试与复杂依赖项隔离开来的好方法，可以让它们运行得更快。
有几个开源框架可以更轻松地处理模拟对象。流行的包括 Mockito、EasyMock 和 JMock。完整的比较列表这些框架中的一部分可以在 <http://code.google.com/p/jmockit/wiki/MockingToolkitComparisonMatrix> 找到。
其中一些框架允许为不实现任何接口的类创建模拟。无论使用何种框架，使用模拟对象的单元测试通常涉及以下步骤：

- 创建一个新的模拟实例。
- 设置模拟。这涉及指示模拟什么期望和返回什么。
- 运行测试，将模拟实例传递给被测组件。
- 验证结果。

## 集成测试

尽管模拟对象充当了很好的占位符，但很快你就会遇到伪装不够用的情况。 对于需要验证 SQL 查询执行和验证对数据库记录的修改的 DAO 层代码尤其如此。 测试这种代码属于集成测试的范畴。 如前所述，集成测试侧重于测试组件之间的交互以及它们的依赖关系。
开发人员使用单元测试工具编写自动化集成测试已变得很普遍，从而模糊了两者之间的区别。 但是，重要的是要记住，集成测试不是孤立运行的，而且通常速度较慢。 Spring 等框架为轻松编写和执行集成测试提供了容器支持。 嵌入式数据库、目录和服务器的可用性提高，使开发人员能够编写更快的集成测试。

## JUnit

在单元测试 Java 应用程序方面，JUnit 已成为事实上的标准。 JUnit 4.x 中注释的引入使得创建测试和断言预期值的测试结果变得更加容易。 JUnit 可以很容易地与 ANT 和 Maven 等构建工具集成。 它还在所有流行的 IDE 中提供了强大的工具支持。
对于 JUnit，标准做法是编写一个单独的类来保存测试方法。 此类通常被称为测试用例，每个测试方法都旨在测试单个工作单元。 也可以将测试用例组织成组，称为测试套件。
学习 JUnit 最好的方法是编写一个测试方法。 下面的清单显示了一个带有 isEmpty 方法的简单 StringUtils 类。 该方法将 String 作为参数，如果它为 null 或空字符串，则返回 true。

```java
public class StringUtils {
   public static boolean isEmpty(String text) {
   return test == null || "".equals(test);
} }

public class StringUtilsTest {
    @Test
    public void testIsEmpty() {
      Assert.assertTrue(StringUtils.isEmpty(null));
      Assert.assertTrue(StringUtils.isEmpty(""));
      Assert.assertFalse(StringUtils.isEmpty("Practical Spring Ldap"));
} }
```

请注意，我遵循了 `<Class Under Test>Test` 约定来命名测试类。 在 JUnit 4.x 之前，测试方法需要以单词“test”开头。 在 4.x 中，测试方法只需要使用注解 @Test 进行标记。 还要注意 testIsEmpty 方法包含几个用于测试 isEmpty 方法逻辑的断言。
下表列出了 JUnit 4 中可用的一些重要注解。

| Annotation   | Description                                                                                                                                                 | 描述                                                  |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| @Test        | Annotates a method as a JUnit test method. The method should be of public scope and have void return type.                                                  | 将方法注释为 JUnit 测试方法。 该方法应该是公共范围的并且具有 void 返回类型。       |
| @Before      | Marks a method to run before every test method. Useful for setting up test fixtures. The @Before method of a superclass is run before the current class.    | 标记要在每个测试方法之前运行的方法。 用于设置测试夹具。 超类的@Before 方法在当前类之前运行。 |
| @After       | Marks a method to be run after every test method. Useful for tearing down test fixtures. The @After method of a superclass is run before the current class. | 标记要在每个测试方法之后运行的方法。 用于拆除测试夹具。 超类的@After 方法在当前类之前运行。  |
| @Ignore      | Marks a method to be ignored during test runs. This helps avoid the need for commenting half-baked test methods.                                            | 标记在测试运行期间要忽略的方法。 这有助于避免评论半生不熟的测试方法的需要。              |
| @BeforeClass | Annotates a method to run before any test method is run. For a test case, the method is run only once and can be used to provide class level setup work.    | 在运行任何测试方法之前注释要运行的方法。 对于测试用例，该方法只运行一次，可用于提供类级别的设置工作。 |
| @AfterClass  | Annotates a method to run after all the test methods are run. This can be useful for performing any cleanups at a class level.                              | 在运行所有测试方法后注释要运行的方法。 这对于在类级别执行任何清理很有用。               |
| @RunWith     | Specifies the class that is used to run the JUnit test case.                                                                                                | 指定用于运行 JUnit 测试用例的类。                                |

## 使用嵌入式 LDAP 服务器进行测试

ApacheDS、OpenDJ 和 UnboundID 是可以嵌入到 Java 应用程序中的开源 LDAP 目录。嵌入式目录是应用程序 JVM 的一部分，可以轻松自动执行启动和关闭等任务。它们的启动时间很短，通常运行速度很快。嵌入式目录还消除了对每个开发人员或构建机器的专用独立 LDAP 服务器的需求。
> 注意 这里讨论的概念是 LdapUnit 开源项目的基础。您将使用 LdapUnit 来测试代码。请访问 <http://ldapunit.org> 下载项目工件并浏览完整的源代码。

嵌入 LDAP 服务器涉及以编程方式创建服务器并启动/停止它。然而，尽管它们很成熟，但以编程方式与 ApacheDS 或 OpenDJ 交互还是很麻烦。

### 设置嵌入式 ApacheDS

ApacheDS 的核心是存储数据并支持搜索操作的目录服务。因此，启动 ApacheDS LDAP 服务器首先涉及创建和配置目录服务。使用 DefaultDirectoryServiceFactory 并对其进行初始化。

ApacheDS 使用分区partitions来存储 LDAP 条目entries。（一个分区可以看作是一个包含整个 DIT 的逻辑容器）。单个 ApacheDS 实例可能有多个分区。与每个分区相关联的是一个称为分区后缀的根专有名称 (DN)。该分区中的所有条目都存储在该根 DN 下。创建一个分区并将其添加到 directoryService 中。

您使用分区工厂创建分区。为了创建新分区，您必须提供以下信息：唯一标识分区的名称、分区后缀或 rootDn、缓存大小和工作目录。使用了 rootDn 作为分区名称。
创建和配置目录服务后，下一步是创建 LDAP 服务器。对于新创建的 LDAP 服务器，您提供一个名称。然后创建一个 TcpTransport 对象，它将在端口 12389 上进行侦听。TcpTransport 实例允许客户端与您的 LDAP 服务器进行通信。

#### 创建嵌入式上下文工厂

使用上述代码，下一步是自动启动服务器并创建可用于与嵌入式服务器交互的上下文。 在 Spring 中，您可以通过实现创建 ContextSource 的新实例的自定义 FactoryBean 来实现这一点。 创建上下文工厂。

EmbeddedContextSourceFactory bean 使用两个 setter 方法：setPort 和 setRootDn。 setPort 方法可用于设置嵌入式服务器应运行的端口。 setRootDn 方法可用于提供根上下文的名称。 createInstance 方法的实现，它创建了一个新的 ApacheDSConfigurer 实例并启动了服务器。 然后它创建一个新的 LdapContenxtSource 并使用嵌入式 LDAP 服务器信息填充它。

提供了 destroyInstance 的实现。 它只涉及清理创建的上下文并停止嵌入式服务器。

最后一步是创建一个使用新上下文工厂的 Spring 上下文文件。 请注意，嵌入式上下文源被注入到 ldapTemplate 中。

现在您拥有编写 JUnit 测试用例所需的整个基础架构。 简单的 JUnit 测试用例。 这个测试用例有一个在每个测试方法之前运行的设置方法。 在设置方法中，您加载数据，以便 LDAP 服务器处于已知状态。 从 employees.ldif 文件中加载数据。 拆解方法在每个测试方法运行后运行。 在 teardown 方法中，您将删除 LDAP 服务器中的所有条目。 这将允许您从新测试开始干净。 三种测试方法都很简陋，简单的在控制台打印信息。

## 使用 EasyMock 模拟 LDAP

EasyMock 是一个开源库，可以轻松创建和使用模拟对象。 从 3.0 版开始，EasyMock 原生支持模拟接口和具体类。 最新版本的 EasyMock 可以从 <http://easymock.org/Downloads.html> 下载。 为了模拟具体的类，需要另外两个库，即 CGLIB 和 Objenesis。 

Maven 用户只需在 pom.xml 中添加以下依赖即可获取所需的 jar 文件：

```xml
<dependency>
   <groupId>org.easymock</groupId>
   <artifactId>easymock</artifactId>
   <version>3.2</version>
   <scope>test</scope>
</dependency>
```

使用 EasyMock 创建模拟需要调用 EasyMock 类。以下示例为 LdapTemplate 创建一个模拟对象：
`LdapTemplate ldapTemplate = EasyMock.createMock(LdapTemplate.class);`
每个新创建的模拟对象都以录制模式开始。在这种模式下，您记录模拟的预期行为或期望expectation。
例如，您可以告诉模拟，如果调用此方法，则返回此值。
例如，以下代码为 LdapTemplate 模拟添加了新的期望：
`EasyMock.expect(ldapTemplate.bind(isA(DirContextOperations.class)));`
在此代码中，您将指示模拟绑定方法将被调用，并且 DirContextOperations 的实例将作为其参数传入。
一旦记录了所有期望，模拟就需要能够重放这些期望。这是通过调用 EasyMock 上的重播方法并传入需要重播的模拟对象作为参数来完成的。
`EasyMock.replay(ldapTemplate);`
模拟对象现在可以在测试用例中使用。一旦被测代码完成执行，您可以验证是否满足模拟的所有期望。这是通过调用 EasyMock 上的 verify 方法来完成的。
`EasyMock.verify(ldapTemplate);`
模拟对于验证搜索方法中使用的上下文行映射器context row mappers特别有用。如您之前所见，行映射器实现将 LDAP 上下文/条目转换为 Java 域对象domain object。这是执行转换的 ContextMapper 接口中的方法签名：
`public Object mapFromContext(Object ctx)`
此方法中的 ctx 参数通常是 DirContextOperations 实现的实例。因此，为了对 ContextMapper 实现进行单元测试，您需要将模拟 DirContextOperations 实例传递给 mapFromContext 方法。模拟 DirContextOperations 应该返回虚拟但有效的数据，以便 ContextMapper 实现可以从中创建域对象。LdapMockUtils.java 中的 DirContextOperations 方法显示了模拟和填充实例。 mockContextOperations 循环通过传入的虚拟属性数据并添加对单值和多值属性的期望。

## 测试数据生成

出于测试目的，您通常需要生成初始测试数据。 OpenDJ 提供了一个很棒的命令行实用程序，称为 make-ldif，它使生成测试 LDAP 数据变得轻而易举。
make-ldif 工具需要用于创建测试数据的模板。 Pater.template 文件来生成顾客条目。

```txt
define suffix=dc=inflinx,dc=com define maildomain=inflinx.com define numusers=101
branch: [suffix]
branch: ou=patrons,[suffix] subordinateTemplate: person:[numusers]
template: person
rdnAttr: uid
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
givenName: <first>
sn: <last>
cn: {givenName} {sn}
initials: {givenName:1}<random:chars:ABCDEFGHIJKLMNOPQRSTUVWXYZ:1>{sn:1}
employeeNumber: <sequential:0>
uid: patron<sequential:0>
mail: {uid}@[maildomain]
userPassword: password
telephoneNumber: <random:telephone>
homePhone: <random:telephone>
mobile: <random:telephone>
street: <random:numeric:5> <file:streets> Street
l: <file:cities>
st: <file:states>
postalCode: <random:numeric:5>
postalAddress: {cn}${street}${l}, {st} {postalCode}
```

这是对安装时开箱即用的 example.template 文件的简单修改。 example.template 位于 `<OpenDJ_Install>\config\ MakeLDIF` 文件夹中。 uid 已修改为使用前缀“patron”而不是“user”。此外，numUsers 值已更改为 101。这表示您希望脚本生成的测试用户数。要生成测试数据，请在命令行中运行以下命令：

```bash
C:\practicalldap\opendj\bat>make-ldif --ldifFile
c:\practicalldap\testdata\patrons.ldif --templateFile
c:\practicalldap\templates\patron.template --randomSeed 1
```

- --ldifFile 选项用于指定目标文件位置。在这里，您将其存储在 testdata 目录中的名称 Patrons.ldif 下
- --templateFile 用于指定要使用的模板文件。
- --randomSeed 是一个整数，需要用于为数据生成期间使用的随机数生成器播种。
创建成功后，除了 101 个测试条目之外，该脚本还创建了两个额外的基本条目。
