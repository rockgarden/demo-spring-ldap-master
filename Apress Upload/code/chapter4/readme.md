# Testing LDAP Code

- 单元/模拟/集成(unit/mock/integration)测试的基础。
- 使用嵌入式 LDAP 服务器进行测试。
- 使用 EasyMock 进行模拟测试。
- 生成测试数据。

测试是任何软件开发过程的一个重要方面。除了检测错误外，它还有助于验证是否满足所有要求以及软件是否按预期工作。今天，无论是正式的还是非正式的，测试几乎都包含在软件开发过程的每个阶段。取决于正在测试的内容和为了测试背后的目的，我们最终得到了几种不同类型的测试。

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

## 使用嵌入式 LDAP 服务器进行测试

ApacheDS、OpenDJ 和 UnboundID 是可以嵌入到 Java 应用程序中的开源 LDAP 目录。嵌入式目录是应用程序 JVM 的一部分，可以轻松自动执行启动和关闭等任务。它们的启动时间很短，通常运行速度很快。嵌入式目录还消除了对每个开发人员或构建机器的专用独立 LDAP 服务器的需求。
> 注意 这里讨论的概念是 LdapUnit 开源项目的基础。在以后的所有章节中，您将使用 LdapUnit 来测试代码。请访问 <http://ldapunit.org> 下载项目工件并浏览完整的源代码。

嵌入 LDAP 服务器涉及以编程方式创建服务器并启动/停止它。然而，尽管它们很成熟，但以编程方式与 ApacheDS 或 OpenDJ 交互还是很麻烦。
