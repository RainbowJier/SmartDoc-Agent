# Java 编码规范精华手册

> 综合整理自：阿里巴巴 Java 开发手册(黄山版)、Google Java Style Guide、Oracle Java Code Conventions
>
> 规约等级说明：**【强制】** 必须遵守 | **【推荐】** 建议遵守 | **【参考】** 可酌情选择

---

## 一、命名规约

### 1.1 通用规则

- **【强制】** 代码中的命名均不能以下划线或美元符号开始/结束。反例：`_name`、`__name`、`$Object`、`name_`、`name$`、`Object$`
- **【强制】** 代码中的命名严禁使用拼音与英文混合的方式，更不允许直接使用中文的方式。正确的英文拼写和语法可以让阅读者易于理解，避免歧义。正例：
  `alibaba`、`taobao`、`youku` 等国际通用名称可视为英文。反例：`DaZhePromotion` [打折]、`getPingfenByName()` [评分]
- **【强制】** 类名使用 UpperCamelCase 风格，必须遵从驼峰形式。正例：`ForceCode` / `UserDO` / `HtmlDTO` / `XmlService` /
  `TcpUdpDeal` / `TaPromotion`
- **【强制】** 方法名、参数名、成员变量、局部变量都统一使用 lowerCamelCase 风格，必须遵从驼峰形式。正例：`localValue` /
  `getHttpMessage()` / `inputUserId`
- **【强制】** 常量命名全部大写，单词间用下划线隔开，力求语义表达完整清楚，不要嫌名字长。正例：`MAX_STOCK_COUNT`。反例：
  `MAX_COUNT`
- **【强制】** 抽象类命名使用 `Abstract` 或 `Base` 开头；异常类命名使用 `Exception` 结尾；测试类命名以它要测试的类的名称开始，以
  `Test` 结尾
- **【强制】** 类型与中括号紧挨相连来表示数组。正例：`int[] arrayDemo`。反例：`int arrayDemo[]`
- **【强制】** POJO 类中布尔类型的变量都不要加 `is` 前缀，否则部分框架解析会引起序列化错误。说明：本文 MySQL
  规约中的建表约定第一条，表达是与否的变量采用 `is_xxx` 的命名方式，所以需要在 `<resultMap>` 中设置从 `is_xxx` 到 `xxx`
  的映射关系。正例：`deleted`。反例：`isDeleted`
- **【推荐】** 包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。正例：`com.alibaba.open.util`。反例：
  `com.alibaba.open.util2`
- **【推荐】** 如果模块、接口、类、方法使用了设计模式，在命名时需体现出具体模式。说明：将设计模式体现在名字中，有利于阅读者快速理解架构设计理念。正例：
  `OrderFactory`（工厂模式）、`LoginProxy`（代理模式）、`ResourceObserver`（观察者模式）
- **【推荐】** 接口类中的方法和属性不要加任何修饰符号（`public` 也不要加），保持代码的简洁性，并加上有效的 Javadoc 注释
- **【参考】** 枚举类名建议带上 `Enum` 后缀，枚举成员名称需要全大写，单词间用下划线隔开。正例：枚举名字为 `ProcessStatusEnum`
  的成员名称：`SUCCESS` / `UNKNOWN_REASON`

### 1.2 各标识符命名风格总览

| 类型    | 风格                      | 正例                                    | 反例                                               |
|-------|-------------------------|---------------------------------------|--------------------------------------------------|
| 包名    | 全小写，连续单词直接拼接            | `com.example.deepspace`               | `com.example.deepSpace`、`com.example.deep_space` |
| 类名    | UpperCamelCase，名词       | `UserOrderService`                    | `userOrderService`、`UserorderService`            |
| 接口名   | UpperCamelCase，形容词/名词   | `Runnable`、`OrderService`             | `IOrderService`（不要加 I 前缀）                        |
| 抽象类   | `Abstract` 或 `Base` 开头  | `AbstractTreeNode`、`BaseController`   | —                                                |
| 异常类   | 以 `Exception` 结尾        | `BizException`、`TimeoutException`     | —                                                |
| 测试类   | 以被测类名 + `Test` 结尾       | `UserServiceTest`                     | `TestUserService`                                |
| 方法名   | lowerCamelCase，动词/动宾    | `sendMessage`、`getOrderById`          | `SendMessage`、`getorder`                         |
| 常量    | UPPER_SNAKE_CASE        | `MAX_RETRY_COUNT`、`DEFAULT_PAGE_SIZE` | `maxRetryCount`、`MAXRETRY`                       |
| 变量/字段 | lowerCamelCase，名词       | `orderList`、`userAge`                 | `orderlist`、`user_age`                           |
| 类型参数  | 单大写字母或 UpperCamelCase+T | `T`、`E`、`K`、`V`、`RequestT`            | `Type`、`ELEMENT`                                 |
| 布尔变量  | lowerCamelCase，不加 is 前缀 | `deleted`、`finished`                  | `isDeleted`、`isFinished`                         |

### 1.3 Service / DAO 层命名规约

| 操作类型   | 命名约定                    | 示例                                     |
|--------|-------------------------|----------------------------------------|
| 获取单个对象 | `get` + 实体名             | `getUserById(Long id)`                 |
| 获取多个对象 | `list` + 实体名（复数）        | `listUsersByRole(String role)`         |
| 获取统计值  | `count` + 实体名           | `countActiveUsers()`                   |
| 插入     | `save`/`insert` + 实体名   | `saveUser(User user)`                  |
| 删除     | `delete`/`remove` + 实体名 | `deleteUserById(Long id)`              |
| 修改     | `update` + 实体名          | `updateUserRole(Long id, String role)` |

### 1.4 领域模型命名规约

| 类型     | 后缀            | 示例                | 说明                   |
|--------|---------------|-------------------|----------------------|
| 数据对象   | `DO`          | `UserDO`          | 与数据库表结构一一对应          |
| 数据传输对象 | `DTO`         | `UserDTO`         | 用于 Service 层之间的数据传输  |
| 展示对象   | `VO`          | `UserVO`          | 用于 Controller 层返回给前端 |
| 查询对象   | `Query`       | `UserQuery`       | 用于接收前端查询参数           |
| 分页对象   | `PageRequest` | `UserPageRequest` | 继承通用分页基类             |

### 1.5 常见缩写驼峰转换

| 原始表述                 | 正确                  | 错误                      |
|----------------------|---------------------|-------------------------|
| XML HTTP request     | `XmlHttpRequest`    | `XMLHTTPRequest`        |
| new customer ID      | `newCustomerId`     | `newCustomerID`         |
| supports IPv6 on iOS | `supportsIpv6OnIos` | `supportsIPv6OnIOS`     |
| YouTube importer     | `YouTubeImporter`   | `YoutubeImporter`       |
| HTTP URL             | `httpUrl`           | `HTTPURL`               |
| CPU usage            | `cpuUsage`          | `CpuUsage` / `CPUUsage` |

---

## 二、代码格式

### 2.1 缩进与行宽

| 规范来源   | 缩进   | 单行最大字符数 |
|--------|------|---------|
| Google | 2 空格 | 100     |
| Oracle | 4 空格 | 80      |
| 阿里巴巴   | 4 空格 | 120     |

**推荐统一使用 4 空格缩进，单行不超过 120 字符。** 不要使用 Tab 缩进，必须用空格。IDE 的 Tab 设置应为将 Tab 替换为空格。

### 2.2 大括号（K&R 风格）

- **【强制】** 左大括号 `{` 不换行，紧跟语句
- **【强制】** 右大括号 `}` 独占一行
- **【强制】** `if/else/for/while/do` 即使只有一行也必须使用大括号
- **【强制】** 右大括号后还有 `else`、`catch`、`finally` 等关键字时，关键字与右大括号之间不换行

```java
// 正例
if (condition) {
    doSomething();
} else {
    doOther();
}

// 反例 — 缺少大括号
if (condition)
    doSomething();
else
    doOther();
```

### 2.3 换行规则

- 非赋值运算符在运算符**前**换行；赋值运算符 `=` 在运算符**后**换行
- 逗号后换行
- 续行缩进至少 +4 空格，续行与上一行同级语法元素对齐
- 方法/构造器名与紧跟的左括号 `(` 不换行

```java
// 方法调用换行
int result = someMethod(longExpression1, longExpression2,
        longExpression3, longExpression4);

// 运算符前换行（非赋值）
long sum = longExpression1
        + longExpression2
        + longExpression3;

// 方法链式调用换行
String result = users.stream()
        .filter(user -> user.isActive())
        .map(User::getName)
        .collect(Collectors.joining(", "));
```

### 2.4 空行与空格

**空行规则：**

- **【强制】** 方法之间保留一个空行
- **【强制】** 类成员（字段、构造器、方法、内部类）之间保留一个空行
- **【推荐】** 在方法体内，不同业务逻辑之间用一个空行分隔，提升可读性
- 连续多个空行不要出现

**空格规则：**

- **【强制】** 关键字与括号之间加空格：`if (condition)`、`for (int i = 0; i < n; i++)`
- **【强制】** 二元运算符两侧加空格：`a + b`、`a == b`、`a && b`
- **【强制】** 逗号后加空格：`method(a, b, c)`
- **【强制】** 类型声明中类型与标识符之间加空格：`List<String> list`
- **【强制】** 强制类型转换后加空格：`(int) num`
- **【强制】** 注释双斜线与内容之间有且仅有一个空格：`// comment`

### 2.5 修饰符顺序

多个修饰符按以下顺序排列：

```
public protected private abstract default static final
sealed non-sealed transient volatile synchronized native strictfp
```

```java
// 正例
public static final int MAX_COUNT = 100;

// 反例
static public final int MAX_COUNT = 100;
```

### 2.6 其他格式规约

- **【强制】** 单行字符数限制不超过 120 个，超出需要换行
- **【强制】** IDE 的 text file encoding 设置为 UTF-8；IDE 中文件的换行符使用 Unix 格式（`\n`），不要使用 Windows 格式（`\r\n`）
- **【推荐】** 没有必要增加若干空行来隔开不同的逻辑，不同逻辑之间用一个空行即可
- **【推荐】** 方法体内的执行语句组、变量的定义语句组、不同的业务逻辑之间或者不同的语义之间插入一个空行。相同业务逻辑和语义之间不需要插入空行

---

## 三、编程规约

### 3.1 变量声明

- **【强制】** 每行只声明一个变量，不要使用 `int a, b;` 这种声明方式
- **【强制】** 局部变量在使用处就近声明并初始化，不要在方法开头集中声明所有变量（这是 C89 的旧风格）
- **【强制】** 禁止使用 C 风格数组声明：用 `String[] args`，不用 `String args[]`
- **【推荐】** 成员变量不要以一个或多个下划线开头命名

```java
// 正例 — 就近声明
public void process() {
    List<Order> orders = orderService.listOrders();
    for (Order order : orders) {
        int discount = calculateDiscount(order);
        applyDiscount(order, discount);
    }
}

// 反例 — 集中声明
public void process() {
    List<Order> orders;
    int discount;
    orders = orderService.listOrders();
    for (Order order : orders) {
        discount = calculateDiscount(order);
        applyDiscount(order, discount);
    }
}
```

### 3.2 控制语句

- **【强制】** 在 `if/else/for/while/do` 语句中必须使用大括号，即使只有一行代码
- **【强制】** `switch` 必须包含 `default` 分支
- **【推荐】** `switch` 的 fall-through 必须注释 `// fall through`
- **【推荐】** 条件表达式用括号消除歧义

```java
// switch 正例
switch (status) {
    case ACTIVE:
        activate();
        // fall through
    case PENDING:
        process();
        break;
    case CLOSED:
        archive();
        break;
    default:
        handleUnknown();
        break;
}

// 条件表达式括号消除歧义
// 正例
if ((a == b) && (c == d)) { ... }
// 反例
if (a == b && c == d) { ... }
```

- **【推荐】** 在表达式中使用括号来明确运算优先级，不要假设所有读者都记住了运算符优先级表
- **【推荐】** `if/else/for/while/do` 语句独占一行，执行语句不得紧跟其后

```java
// 反例 — 复杂条件表达式缺少括号
if (file.isReady() && file.getName().endsWith(".csv") && file.getSize() > 0) { ... }

// 正例 — 拆分条件并适当加括号
boolean isCsvFile = file.getName().endsWith(".csv");
boolean hasContent = file.getSize() > 0;
if (file.isReady() && isCsvFile && hasContent) { ... }
```

- **【推荐】** 尽量使用卫语句（guard clauses）减少嵌套层级

```java
// 反例 — 嵌套过深
public void process(Order order) {
    if (order != null) {
        if (order.isPaid()) {
            if (order.getItems() != null) {
                shipOrder(order);
            }
        }
    }
}

// 正例 — 卫语句
public void process(Order order) {
    if (order == null) {
        return;
    }
    if (!order.isPaid()) {
        return;
    }
    if (order.getItems() == null) {
        return;
    }
    shipOrder(order);
}
```

### 3.3 异常处理

- **【强制】** 捕获的异常不能被忽略（空 catch 块），至少记录日志
- **【强制】** 不要用 `return`、`continue`、`break` 退出 `finally` 块
- **【强制】** 禁止在 `finally` 块中使用 `return`
- **【强制】** 方法返回值可以为 `null` 时，调用方需要做 NPE 判断
- **【强制】** 不要捕获大的异常类 `Exception`/`Throwable`，应捕获具体异常类
- **【推荐】** 异常不能用来做流程控制
- **【推荐】** 方法签名有 `throws` 时，Javadoc 需用 `@throws` 标注

```java
// 正例 — 捕获具体异常并处理
try {
    int value = Integer.parseInt(input);
    return handleValue(value);
} catch (NumberFormatException e) {
    log.warn("Invalid number format: {}", input, e);
    return defaultValue;
}

// 反例 — 捕获大异常且忽略
try {
    doSomething();
} catch (Exception e) {
    // 空 catch 块
}

// 反例 — 用异常做流程控制
public boolean isNumber(String str) {
    try {
        Integer.parseInt(str);
        return true;
    } catch (NumberFormatException e) {
        return false;
    }
}

// 正例 — 使用工具方法
public boolean isNumber(String str) {
    return StringUtils.isNumeric(str);
}
```

### 3.4 方法

- **【强制】** 能加 `@Override` 的地方必须加，包括接口方法实现
- **【推荐】** 方法参数不超过 5 个，超过时考虑封装为对象或使用 Builder 模式
- **【推荐】** 方法体不超过 80 行（不含空行和注释），超过应拆分子方法
- **【推荐】** 方法圈复杂度不超过 10，超过需重构

```java
// 正例 — 使用 @Override
public class OrderServiceImpl implements OrderService {
    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }
}

// 反例 — 缺少 @Override
public class OrderServiceImpl implements OrderService {
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }
}
```

### 3.5 静态成员引用

- **【强制】** 使用类名调用静态方法/变量，不要用实例引用
- **【强制】** 避免通过一个类的对象引用来访问它的静态变量或方法

```java
// 正确
Foo.staticMethod();
Foo.STATIC_FIELD;

// 错误
fooInstance.staticMethod();
fooInstance.STATIC_FIELD;
```

### 3.6 字面量

- **【强制】** `long` 类型字面量使用大写 `L` 后缀：`100L`，不用 `100l`（避免与数字 `1` 混淆）
- **【推荐】** 避免在代码中出现魔法值（未经预先定义的常量）

```java
// 正例 — 使用命名常量
private static final int MAX_RETRY = 3;
private static final String DEFAULT_CHARSET = "UTF-8";

for (int i = 0; i < MAX_RETRY; i++) {
    retry();
}

// 反例 — 魔法值
for (int i = 0; i < 3; i++) {
    retry();
}
```

### 3.7 finalize 方法

- **【强制】** 不要覆写 `Object.finalize()`，Java 9+ 已标记为 `@Deprecated(forRemoval=true)`

---

## 四、注释规约

### 4.1 类和接口注释

- **【强制】** 所有的类都必须添加创建者和创建日期
- **【强制】** 类、类属性、类方法的注释必须使用 Javadoc 规范，使用 `/** */` 格式，不得使用 `// xxx` 方式

```java
/**
 * 订单服务 — 处理订单的创建、查询和状态流转。
 * <p>
 * 支持以下操作：创建订单、取消订单、查询订单详情、订单支付状态变更。
 *
 * @author frank
 * @since 2024-01-01
 */
public class OrderService {
    // ...
}
```

### 4.2 方法注释（Javadoc）

- **【强制】** 所有的抽象方法（包括接口中的方法）必须要用 Javadoc 注释
- **【推荐】** 所有的 public 方法都应该有 Javadoc 注释
- 每个 Javadoc 开头是简要的**摘要片段**（名词短语或动词短语），不是完整句子

```java
/**
 * 根据用户ID查询订单列表。
 * <p>
 * 返回结果按创建时间倒序排列，不包含已删除的订单。
 *
 * @param userId 用户ID，不能为空
 * @return 该用户的订单列表，不会返回null，无结果时返回空列表
 * @throws IllegalArgumentException 当 userId 为空时抛出
 */
public List<Order> queryOrders(Long userId) { ... }
```

### 4.3 行内注释

- **【强制】** 方法内部单行注释，在被注释语句上方另起一行，使用 `//` 注释。与注释语句有相同的缩进
- **【推荐】** 注释不要写无意义的内容，应解释"为什么"而非"做了什么"

```java
// 正例 — 解释原因
// 使用批量插入提升性能，单条插入在 10 万条数据时耗时超过 30 秒
orderMapper.batchInsert(orders);

// 反例 — 无意义注释
// 设置名称
user.setName(name);
```

### 4.4 TODO 与 FIXME

- `TODO` 标注待办事项，格式：`// TODO(作者): 描述 + 预期时间/事件`
- `FIXME` 标注需要修复的问题

```java
// TODO(frank): 优化为异步处理，预计v2.0完成
// FIXME: 并发场景下存在线程安全问题，需要加锁
// TODO(bug#12345): 删除此兼容代码，2026-06-30后不再需要
```

### 4.5 注释的其他要求

- **【强制】** 在代码修改的同时，注释也要相应修改，保持注释与代码的一致性
- **【强制】**
  注释掉的代码要配合说明，而不是简单的注释掉。如果无用，则直接删除。说明：代码被注释掉有两种可能性：1）后续会恢复此段代码逻辑。2）永久不用。如果没有备注信息则不能确定是哪一种，不可信。推荐直接删除，需要时从版本控制中恢复
- **【推荐】** 对于注释的要求：第一要能够准确反应设计思想和代码逻辑；第二要能够描述业务含义，使别的程序员能迅速了解到背景知识
- **【推荐】** 好的命名、代码结构是自解释的，注释力求精简准确、表达到位。避免出现注释的一个极端：过多过滥的注释
- **【参考】** 特殊注释标记请注明标记人与标记时间。注意及时处理这些标记，通过标记扫描，经常清理此类标记

---

## 五、OOP 规约

### 5.1 基本原则

- **【强制】** 避免通过类的对象引用来访问静态变量或方法，无意义增加编译器解析成本，直接用类名访问
- **【强制】** 所有的 POJO 类属性必须使用包装数据类型，RPC 方法的返回值和参数必须使用包装数据类型。说明：POJO
  类属性没有初值是提醒使用者在需要使用时，必须自己显式地进行赋值
- **【强制】** 所有的局部变量使用基本数据类型。说明：POJO 类属性没有初值是提醒使用者在需要使用时，必须自己显式地进行赋值
- **【强制】** 所有的 POJO 类必须有 `toString()` 方法。说明：方法执行抛出异常时，可以直接调用 POJO 的 `toString()` 方法打印其属性值
- **【推荐】** 类内方法定义顺序依次是：公有方法或保护方法 → 私有方法 → getter / setter 方法。说明：公有方法是类的接口或服务，对调用者来说应以公开方法为首兴趣

### 5.2 equals 方法

- **【强制】** `Object` 的 `equals` 方法容易抛空指针异常，应使用常量或确定不为空的对象来调用 `equals`

```java
// 正例
"test".equals(object);
Objects.equals(str1, str2);

// 反例
object.equals("test");
str1.equals(str2);  // str1 为 null 时 NPE
```

### 5.3 构造方法

- **【强制】** 构造方法里禁止加入业务逻辑，如果有初始化逻辑，请放在 `init()` 方法中

```java
public class OrderService {
    private final OrderMapper orderMapper;
    
    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;  // 只做赋值
    }
    
    @PostConstruct
    public void init() {
        // 初始化逻辑放这里
        loadCache();
    }
}
```

### 5.4 继承与多态

- **【推荐】** 慎用 `Object` 的 `clone` 方法来拷贝对象。说明：对象的 `clone` 方法默认是浅拷贝，若想实现深拷贝需重写 `clone`
  方法实现域对象的深度遍历式拷贝
- **【推荐】** 类成员方法访问顺序：public > protected > private
- **【推荐】** 相同业务含义的方法应放在一起，不要被其他方法隔开
- **【推荐】** 覆写方法必须加 `@Override`
- **【推荐】** 可变参数慎用，超过 3 个参数考虑封装为对象或使用 Builder 模式
- **【强制】** 接口入参和返回值不允许使用 `Map` 等弱类型，应使用明确的 DTO/VO

### 5.5 序列化

- **【强制】** 序列化类新增属性时，请不要修改 `serialVersionUID` 字段，避免反序列失败；如果完全不兼容升级，避免反序列化混乱，那么请修改
  `serialVersionUID` 值。说明：注意 `serialVersionUID` 不一致会抛出序列化运行时异常

### 5.6 完整示例

```java
/**
 * 用户订单服务实现。
 *
 * @author frank
 * @since 2024-01-01
 */
public class OrderServiceImpl implements OrderService {

    private static final int MAX_ORDER_ITEMS = 50;

    private final OrderMapper orderMapper;
    private final ProductService productService;

    public OrderServiceImpl(OrderMapper orderMapper, ProductService productService) {
        this.orderMapper = orderMapper;
        this.productService = productService;
    }

    /**
     * 创建订单。
     *
     * @param request 创建订单请求
     * @return 创建成功的订单
     * @throws IllegalArgumentException 当请求参数不合法时抛出
     */
    @Override
    public Order createOrder(CreateOrderRequest request) {
        validateRequest(request);
        Order order = buildOrder(request);
        orderMapper.insert(order);
        return order;
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("请求参数不合法");
        }
    }

    private Order buildOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreateTime(new Date());
        return order;
    }

    @Override
    public String toString() {
        return "OrderServiceImpl{"
                + "orderMapper=" + orderMapper
                + ", productService=" + productService
                + '}';
    }
}
```

---

## 六、集合与并发

### 6.1 集合

- **【强制】** 不要在 `foreach` 循环里进行元素的 `remove/add` 操作，`remove` 元素请使用 `Iterator` 方式；如果并发操作，需要对
  `Iterator` 对象加锁

```java
// 正例
Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String item = iterator.next();
    if (condition) {
        iterator.remove();
    }
}

// 正例 — Java 8+ 使用 removeIf
list.removeIf(item -> condition);

// 反例 — foreach 中 remove 会抛 ConcurrentModificationException
for (String item : list) {
    if (condition) {
        list.remove(item);
    }
}
```

- **【强制】** 集合转数组使用 `T[] arr = list.toArray(new T[0])`，不要使用 `list.toArray()` 无参方法

```java
// 正例
String[] array = list.toArray(new String[0]);

// 反例 — 返回 Object[]，强转可能抛 ClassCastException
String[] array = (String[]) list.toArray();
```

- **【强制】** 集合判空使用 `isEmpty()` 而非 `size() == 0`。说明：`isEmpty()` 可读性更好且某些集合实现中性能更优
- **【强制】** 使用 `Arrays.asList()` 返回的列表不可修改，不支持 `add/remove/clear` 操作

```java
// 正例 — 需要可变列表
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));

// 反例 — asList 返回的列表不支持修改
List<String> list = Arrays.asList("a", "b", "c");
list.add("d");  // UnsupportedOperationException
```

- **【推荐】** 集合初始化时指定初始容量，避免频繁扩容

```java
// 正例 — 指定初始容量
List<String> list = new ArrayList<>(100);
Map<String, Object> map = new HashMap<>(32);

// 反例 — 使用默认容量，可能多次扩容
List<String> list = new ArrayList<>();
```

- **【推荐】** 使用 `entrySet()` 遍历 Map，而非 `keySet()` 二次取值

```java
// 正例
for (Map.Entry<String, String> entry : map.entrySet()) {
    String key = entry.getKey();
    String value = entry.getValue();
}

// 反例 — 效率低（多了一次 get 查找）
for (String key : map.keySet()) {
    String value = map.get(key);
}
```

- **【推荐】** 高度注意 Map 类集合 K/V 能不能存储 null 值的情况

| 集合类                 | Key 允许 null | Value 允许 null |
|---------------------|-------------|---------------|
| `HashMap`           | 是           | 是             |
| `ConcurrentHashMap` | 否           | 否             |
| `Hashtable`         | 否           | 否             |
| `TreeMap`           | 否           | 是             |

### 6.2 并发

- **【强制】** 线程资源通过线程池提供，不允许自行显式创建线程。说明：使用线程池的好处是减少在创建和销毁线程上所消耗的时间以及系统资源的开销，解决资源不足的问题

```java
// 正例 — 使用线程池
private static final ExecutorService EXECUTOR = 
        new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactoryBuilder().setNameFormat("order-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());

// 反例 — 直接创建线程
new Thread(() -> doSomething()).start();
```

- **【强制】** `SimpleDateFormat` 非线程安全，不要定义为 static 共享变量

```java
// 正例 — 使用 ThreadLocal 或 DateTimeFormatter
private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

// 或者使用 ThreadLocal
private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// 反例 — 共享 static 的 SimpleDateFormat
private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
```

- **【强制】** 多线程环境下避免使用 `HashMap`，应使用 `ConcurrentHashMap`
- **【强制】** `ThreadLocal` 用完必须 `remove()`，防止内存泄漏

```java
// 正例
try {
    threadLocalUser.set(currentUser);
    doBusiness();
} finally {
    threadLocalUser.remove();
}
```

- **【强制】** 锁的获取与释放要在同一代码块中，使用 `try-finally`

```java
// 正例
lock.lock();
try {
    doBusiness();
} finally {
    lock.unlock();
}

// 反例 — lock 和 unlock 不在同一代码块
lock.lock();
doBusiness();
// ... 中间可能抛异常导致 unlock 不执行
lock.unlock();
```

- **【推荐】** 并发修改同一记录时，避免更新丢失，需要加锁。悲观锁（`SELECT ... FOR UPDATE`）或乐观锁（版本号机制）均可
- **【推荐】** `ThreadPoolExecutor` 自定义线程池，明确各参数含义

```java
/**
 * 核心线程数：4
 * 最大线程数：8
 * 空闲存活时间：60秒
 * 工作队列：有界队列，容量1024
 * 线程命名：order-pool-{n}
 * 拒绝策略：CallerRunsPolicy（由调用线程执行）
 */
private static final ExecutorService EXECUTOR =
        new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactoryBuilder().setNameFormat("order-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
```

---

## 七、异常与日志

### 7.1 异常

- **【强制】** 不要捕获大的异常类 `Exception`/`Throwable`，应捕获具体异常
- **【强制】** 异常不能用来做流程控制
- **【强制】** 方法签名有 `throws` 时，Javadoc 需用 `@throws` 标注
- **【强制】** 禁止在 `finally` 块中使用 `return`
- **【强制】** 捕获异常与抛异常，必须是完全匹配，或者捕获异常是抛异常的父类
- **【推荐】** 方法的返回值可以为 `null`，不强制返回空集合或者空对象等，但必须在注释中充分说明什么情况下会返回 `null`
  值。调用方需要进行 `null` 判断防止 NPE 问题
- **【推荐】** 防止 NPE，是程序员的基本修养。注意 NPE 产生的场景：
    1. 返回类型为基本数据类型，return 包装数据类型的对象时，自动拆箱有可能产生 NPE
    2. 数据库的查询结果可能为 `null`
    3. 集合里的元素即使 `isNotEmpty`，取出的数据元素也可能为 `null`
    4. 远程调用返回对象一律要求进行 NPE 判断
    5. 对于 `Session` 中获取的数据，建议进行 NPE 检查
    6. 级联调用 `obj.getMethod().getMethod()` 容易产生 NPE

```java
// 正例 — 捕获具体异常
try {
    int value = Integer.parseInt(input);
    return handleValue(value);
} catch (NumberFormatException e) {
    log.warn("Invalid number format: {}", input, e);
    return defaultValue;
}

// 正例 — NPE 防护
public int getUserIdLength(User user) {
    if (user == null) {
        return 0;
    }
    return Strings.nullToEmpty(user.getName()).length();
}
```

### 7.2 日志

- **【强制】** 使用 SLF4J 门面，不要直接依赖 Log4j/Logback API。说明：使用日志门面有利于维护和各个类中日志处理方式的统一
- **【强制】** 日志文件保留至少 15 天，因为有些异常具备以"周"为频次发生的特点
- **【强制】** `DEBUG`/`INFO` 级别日志使用占位符拼接，不要字符串拼接

```java
// 正例 — 使用占位符
log.debug("Processing order: {}", orderId);
log.info("User {} logged in from {}", userId, ipAddress);

// 反例 — 字符串拼接（即使日志级别不匹配也会执行拼接）
log.debug("Processing order: " + orderId);
```

- **【强制】** 避免重复打印日志，否则会浪费磁盘空间。在日志配置中设置 `additivity=false`
- **【推荐】** 谨慎地记录日志。生产环境禁止输出 DEBUG 日志；有选择地输出 INFO 日志
- **【推荐】** 异常信息应该包括两类信息：案发现场信息和异常堆栈信息

```java
// 正例 — 包含案发现场和堆栈
log.error("Failed to create order for user: {}, product: {}", userId, productId, e);

// 反例 — 丢失堆栈信息
log.error("Failed to create order: " + e.getMessage());
```

- **【推荐】** 可以使用 `warn` 日志级别记录用户输入参数错误的情况，避免当用户投诉时无所适从。注意日志输出的级别：`error`
  只记录系统逻辑出错、异常或重要的错误信息；`warn` 记录用户输入参数错误；`info` 记录系统运行中的重要流程信息

---

## 八、工程结构

### 8.1 分层架构

- **【强制】** 项目分为三层：开放 API 层 → 业务逻辑层 → 数据访问层
- **【强制】** 分层领域模型规约：
    - `DTO`（Data Transfer Object）：用于 Service 层与 Controller 层之间的数据传输
    - `VO`（View Object）：用于 Controller 层返回给前端的展示对象
    - `DO`/`Entity`（Data Object）：与数据库表结构一一对应，通过 DAO 层向上传输数据源对象
    - `Query`：用于接收前端查询参数

### 8.2 各层职责

| 层          | 职责                       | 禁止                        |
|------------|--------------------------|---------------------------|
| Controller | 参数校验、调用 Service、转换 VO 返回 | 禁止写业务逻辑                   |
| Service    | 业务逻辑编排、事务控制              | 禁止直接操作 HttpServletRequest |
| DAO/Mapper | 数据库 CRUD                 | 禁止写业务逻辑                   |

### 8.3 Service 层设计

- **【推荐】** `Service` 层接口与实现分离：`XxxService`（接口）+ `XxxServiceImpl`（实现）
- **【推荐】** 接口和实现类的命名有两套规则：
    1. 对于 Service 和 DAO 类，基于 SOA 的理念，暴露出来的服务一定是接口，实现类用 `Impl` 后缀。正例：`CacheServiceImpl` 实现
       `CacheService` 接口
    2. 如果是形容能力的接口名称，取对应的形容词为接口名。正例：`AbstractTranslator` 实现 `Translatable` 接口

### 8.4 包命名规范

```
com.smartdoc
├── controller          # 控制器 — 接收请求、参数校验、返回响应
├── service             # 业务接口
│   └── impl            # 业务实现
├── dao / mapper        # 数据访问 — MyBatis Mapper 接口
├── model / entity      # 实体类 — 与数据库表对应
├── dto                 # 数据传输对象 — 服务间传输
├── vo                  # 视图对象 — 返回给前端
├── config              # 配置类 — Spring 配置、Bean 定义
├── common              # 公共类 — 常量、枚举、通用工具
│   ├── enums           # 枚举定义
│   ├── constants       # 常量定义
│   └── exception       # 自定义异常
└── util                # 工具类 — 静态工具方法
```

### 8.5 二方库依赖

- **【强制】** 二方库默认不开启混淆，发布上线后不可以通过新版覆盖旧版的方式升级
- **【强制】** 二方库的新增或升级，保持除功能点外的其他 jar 包仲裁结果不变

---

## 九、MySQL 数据库规约

### 9.1 建表规约

- **【强制】** 表名、字段名使用小写字母 + 下划线分隔。正例：`user_order`、`create_time`。反例：`UserOrder`、`createTime`
- **【强制】** 表名不使用复数名词。说明：表名应该仅仅表示表里面的实体内容，不应该表示实体数量。正例：`sys_user`。反例：
  `sys_users`
- **【强制】** 禁用保留字，如 `desc`、`range`、`match`、`delayed` 等，请参考 MySQL 官方保留字
- **【强制】** 主键命名为 `id`，使用自增 ID 或雪花算法生成分布式 ID
- **【强制】** 所有字段设置 `NOT NULL` + 默认值。说明：`NULL` 的列使索引/索引统计/值比较都更复杂，对 MySQL 来说 `NULL` 为一个特殊值
- **【强制】** 每张表必须包含以下字段：
    - `id` — 主键
    - `create_time` — 创建时间，默认 `CURRENT_TIMESTAMP`
    - `update_time` — 更新时间，默认 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
    - `is_deleted` — 逻辑删除标志，默认 `0`

```sql
CREATE TABLE `sys_user` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '用户名',
    `email`        VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 9.2 索引规约

- **【强制】** 索引命名规范：
    - 主键索引：`pk_字段名`
    - 唯一索引：`uk_字段名`
    - 普通索引：`idx_字段名`
- **【强制】** 禁止使用存储过程、视图、触发器、外键
- **【推荐】** `VARCHAR` 长度按实际需要设置，不要一概 `VARCHAR(255)`
- **【推荐】** 大字段（TEXT/BLOB）考虑独立建表，避免影响查询性能
- **【推荐】** 单表索引数量控制在 5 个以内，单个索引字段数不超过 5 个
- **【推荐】** 在 `VARCHAR` 字段上建立索引时，必须指定索引长度，没必要对全字段建立索引

### 9.3 SQL 语句

- **【强制】** 不要使用 `count(列名)` 或 `count(常量)` 来替代 `count(*)`，`count(*)` 是 SQL92 定义的标准统计行数语法
- **【强制】** `count(distinct col)` 计算该列除 `NULL` 之外的不重复行数，注意 `count(distinct col1, col2)` 如果其中一列全为
  `NULL`，那么即使另一列有不同的值，也返回 0
- **【强制】** 当某一列的值全是 `NULL` 时，`count(col)` 的返回结果为 0，但 `sum(col)` 的返回结果为 `NULL`，因此使用 `sum()`
  时需注意 NPE 问题

```java
// 正例 — 防止 NPE
Integer totalCount = (Integer) map.get("totalCount");
if (totalCount != null) {
    return totalCount;
}

// 反例 — 直接拆箱可能 NPE
return (int) map.get("totalCount");
```

- **【强制】** 使用 `ISNULL()` 判断是否为 `NULL` 值。说明：`NULL` 与任何值的直接比较都为 `NULL`
    - `NULL <> NULL` 的结果是 `NULL`，不是 `false`
    - `NULL = NULL` 的结果是 `NULL`，不是 `true`
    - `NULL <> 1` 的结果是 `NULL`，不是 `true`
- **【强制】** 代码中写分页查询逻辑时，若 `count` 为 0 应直接返回，避免执行后面的分页语句

### 9.4 ORM 映射

- **【强制】** 在表查询中，一律不要使用 `*` 作为查询的字段列表，需要哪些字段必须明确写明
- **【强制】** `POJO` 类的布尔属性不能加 `is`，而数据库字段必须加 `is_`，要求在 `resultMap` 中进行字段与属性之间的映射
- **【推荐】** 不要写一个大而全的数据更新接口。传入为 `POJO` 类，不管是不是自己的目标更新字段，都进行
  `update table set c1=value1, c2=value2, c3=value3`；这是不对的，执行 SQL 时不要更新无改动的字段

---

## 十、安全规约

### 10.1 权限控制

- **【强制】** 用户密码不可明文存储，必须使用 BCrypt 等不可逆加密算法。说明：MD5 已经被证明不安全
- **【强制】** API 接口需做鉴权，不能信任客户端传入的用户 ID。必须从服务端 Session/Token 中获取当前用户身份
- **【强制】** 用户敏感数据禁止直接展示，必须进行脱敏处理

```java
// 正例 — 手机号脱敏
String masked = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
// 13812345678 → 138****5678

// 正例 — 身份证脱敏
String masked = idCard.replaceAll("(\\d{4})\\d{10}(\\d{4})", "$1**********$2");
// 110101199001011234 → 1101**********1234
```

### 10.2 输入校验

- **【强制】** HTTP 请求参数需做合法性校验，防止 SQL 注入、XSS 攻击
- **【强制】** 表单、AJAX 提交必须执行 CSRF 安全过滤
- **【推荐】** 使用参数化查询，禁止拼接 SQL 字符串

```java
// 正例 — 参数化查询
@Select("SELECT * FROM user WHERE username = #{username}")
User findByUsername(@Param("username") String username);

// 反例 — 字符串拼接（SQL 注入风险）
@Select("SELECT * FROM user WHERE username = '" + username + "'")
User findByUsername(String username);
```

### 10.3 日志安全

- **【强制】** 日志中禁止输出敏感信息（密码、身份证号、银行卡号、Token 等）
- **【推荐】** 错误页面不要暴露技术细节（堆栈信息、SQL 语句、服务器路径等）

### 10.4 其他安全规约

- **【强制】** 在使用平台资源，譬如短信、邮件、电话、下单、支付，必须实现正确的防重放限制，如数量限制、疲劳度控制、验证码校验，避免滥用资损
- **【推荐】** 发贴、评论、发送即时消息等用户生成内容的场景必须实现防刷、文本内容违禁词过滤等风控策略
- **【推荐】** 请求参数中带有 `redirect` 参数时，必须校验目标 URL 是否为合法域名，防止钓鱼攻击

---

## 十一、单元测试

- **【强制】** 好的单元测试必须遵守 AIR 原则：Automatic（自动化）、Independent（独立性）、Repeatable（可重复）
- **【强制】** 单元测试应该是全自动执行的，并且非交互式的。测试用例通常是被定期执行的，执行过程必须完全自动化才有意义
- **【强制】** 保持单元测试的独立性。为了保证单元测试稳定可靠且便于维护，单元测试用例之间决不能互相调用，也不能依赖执行的先后次序
- **【强制】** 单元测试的基本目标：语句覆盖率达到 70%；核心模块的语句覆盖率和分支覆盖率都要达到 100%
- **【推荐】** 和数据库相关的单元测试，可以考虑使用内存数据库（如 H2）进行测试，避免对真实数据库的影响
- **【推荐】** 测试方法命名应该清晰表达测试意图

```java
// 正例 — 清晰的测试命名
@Test
public void shouldThrowExceptionWhenUserIdIsNull() { ... }

@Test
public void shouldReturnEmptyListWhenNoOrdersFound() { ... }

// 或者使用下划线风格
@Test
public void calculateDiscount_vipUserWithAmountOver1000_returns20Percent() { ... }
```

- **【推荐】** BDD（Behavior Driven Development）风格使用 given-when-then 结构

```java
@Test
public void shouldCreateOrderSuccessfully() {
    // given
    CreateOrderRequest request = new CreateOrderRequest();
    request.setUserId(1L);
    request.setProductId(100L);
    
    // when
    Order order = orderService.createOrder(request);
    
    // then
    assertNotNull(order);
    assertEquals(OrderStatus.CREATED, order.getStatus());
}
```

---

## 十二、设计模式与最佳实践

### 12.1 常用设计模式

| 模式         | 场景                            | 示例                                            |
|------------|-------------------------------|-----------------------------------------------|
| 工厂模式       | 根据类型创建不同对象                    | `PaymentFactory.create(PaymentType.ALIPAY)`   |
| 策略模式       | 同一接口多种实现，运行时切换                | `DiscountStrategy` — 满减/折扣/无优惠                |
| 模板方法       | 固定流程，子类实现差异步骤                 | `BaseImportService.doImport()`                |
| 观察者模式      | 事件驱动的通知机制                     | `OrderEventPublisher` → 短信/邮件/积分              |
| Builder 模式 | 多参数对象的构建                      | `User.builder().name("test").age(20).build()` |
| 单例模式       | 全局唯一实例（推荐用 Spring Bean 或枚举实现） | `enum Singleton { INSTANCE; }`                |

### 12.2 SOLID 原则简述

| 原则           | 含义             | 简要说明                    |
|--------------|----------------|-------------------------|
| **S** — 单一职责 | 一个类只有一个引起变化的原因 | 不要把用户管理、订单管理、邮件发送都塞进一个类 |
| **O** — 开闭原则 | 对扩展开放，对修改关闭    | 新增支付方式应新增实现类，而非修改已有代码   |
| **L** — 里氏替换 | 子类必须能替换父类      | 重写方法不应改变父类的行为约定         |
| **I** — 接口隔离 | 客户端不应依赖它不需要的接口 | 拆分臃肿接口为多个小接口            |
| **D** — 依赖倒置 | 依赖抽象而非具体实现     | 面向接口编程，通过 DI 注入依赖       |

### 12.3 代码复杂度控制

- **【推荐】** 单个方法体不超过 80 行（不含空行和注释），超过应拆分子方法
- **【推荐】** 方法圈复杂度不超过 10，超过需使用卫语句或策略模式重构
- **【推荐】** 方法嵌套层级不超过 3 层，超过使用卫语句提前返回
- **【推荐】** 方法参数不超过 5 个，超过封装为 Request 对象

---

> 参考来源：
> - 阿里巴巴 Java 开发手册(黄山版) — https://github.com/alibaba/p3c
> - Google Java Style Guide — https://google.github.io/styleguide/javaguide.html
> - Oracle Java Code Conventions — https://www.oracle.com/java/technologies/javase/codeconventions-contents.html
