# Spring 全家桶开发规范与实战手册

> 涵盖：Spring Boot 3.x · Spring MVC · Spring Data JPA · MyBatis-Plus · Spring Security · Spring AOP · Spring
> Validation · Spring Cache · Spring Async · Spring Actuator
>
> 规约等级说明：**【强制】** 必须遵守 | **【推荐】** 建议遵守 | **【参考】** 可酌情选择

---

## 一、项目结构与分层架构

### 1.1 标准项目结构

```
com.smartdoc
├── SmartDocApplication.java              # 启动类（项目根包下）
├── controller/                           # 控制器层 — 接收请求、参数校验、返回响应
│   ├── AdminController.java
│   ├── UserController.java
│   └── AdviceController.java             # 全局异常处理
├── service/                              # 业务接口层
│   ├── UserService.java
│   └── impl/                             # 业务实现层
│       └── UserServiceImpl.java
├── repository/                           # 数据访问层（也可叫 dao / mapper）
│   └── UserRepository.java
├── model/                                # 领域模型
│   ├── entity/                           # 实体类 — 与数据库表对应
│   │   └── User.java
│   ├── dto/                              # 数据传输对象 — 服务间传输
│   │   ├── UserDTO.java
│   │   └── CreateUserRequest.java
│   ├── vo/                               # 视图对象 — 返回给前端
│   │   └── UserVO.java
│   └── query/                            # 查询参数对象
│       └── UserQuery.java
├── config/                               # 配置类
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   ├── RedisConfig.java
│   └── ThreadPoolConfig.java
├── common/                               # 公共模块
│   ├── result/                           # 统一响应封装
│   │   ├── Result.java
│   │   └── PageResult.java
│   ├── enums/                            # 枚举定义
│   │   └── ResultCode.java
│   ├── constants/                        # 常量定义
│   │   └── BizConstants.java
│   └── exception/                        # 自定义异常
│       ├── BizException.java
│       └── GlobalExceptionHandler.java
└── util/                                 # 工具类
    ├── JsonUtil.java
    └── DateUtil.java
```

### 1.2 分层职责与调用规则

**调用方向：Controller → Service → Repository，严禁反向调用、严禁跨层调用。**

| 层          | 职责                       | 允许                    | 禁止                      |
|------------|--------------------------|-----------------------|-------------------------|
| Controller | 参数校验、调用 Service、组装 VO 返回 | 使用 `@Valid` 校验参数      | 编写业务逻辑                  |
| Service    | 业务编排、事务控制、调用 Repository  | 使用 `@Transactional`   | 直接操作 HttpServletRequest |
| Repository | 数据库 CRUD、自定义查询           | 使用 JPA/MyBatis        | 编写业务逻辑                  |
| Model      | 纯数据载体，不包含业务逻辑            | getter/setter/Builder | 注入 Spring Bean          |

### 1.3 多模块项目结构（Maven 多模块）

```
smartdoc-backend/
├── pom.xml                               # 父 POM — 版本管理
├── smartdoc-api/                         # Web API 模块（启动入口）
│   ├── src/main/java/
│   └── src/main/resources/
│       ├── application.yml
│       └── sample-docs/                  # 知识库文档
├── smartdoc-chat/                        # AI 对话模块
│   └── src/main/java/com/smartdoc/chat/
├── smartdoc-llm/                         # LLM 集成模块
│   └── src/main/java/com/smartdoc/llm/
├── smartdoc-rag/                         # RAG 检索增强模块
│   └── src/main/java/com/smartdoc/rag/
└── smartdoc-tools/                       # 工具调用模块
    └── src/main/java/com/smartdoc/tools/
```

**构建顺序：先 `mvn clean install` 父项目，再启动 api 模块。**

---

## 二、Spring Boot 核心配置

### 2.1 启动类

- **【强制】** 启动类放在根包下，确保 `@SpringBootApplication` 能扫描到所有组件
- **【推荐】** 启动类只负责启动，不要放业务逻辑

```java
@SpringBootApplication
public class SmartDocApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartDocApplication.class, args);
    }
}
```

### 2.2 配置文件（application.yml）

- **【强制】** 使用 YAML 格式（`application.yml`），不要用 Properties 格式，YAML 层次更清晰
- **【强制】** 敏感信息（数据库密码、API Key）不要硬编码，使用环境变量覆盖

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: smartdoc

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/smartdoc?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA 配置（使用 JPA 时启用）
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

  # MyBatis-Plus 配置（使用 MyBatis-Plus 时启用）
# mybatis-plus:
#   mapper-locations: classpath*:/mapper/**/*.xml
#   type-aliases-package: com.smartdoc.model.entity
#   configuration:
#     map-underscore-to-camel-case: true
#     log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
#   global-config:
#     db-config:
#       id-type: auto
#       logic-delete-field: deleted
#       logic-delete-value: 1
#       logic-not-delete-value: 0

  # Jackson 配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null

# 日志配置
logging:
  level:
    root: INFO
    com.smartdoc: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### 2.3 多环境配置

```
resources/
├── application.yml                   # 公共配置
├── application-dev.yml               # 开发环境
├── application-test.yml              # 测试环境
└── application-prod.yml              # 生产环境
```

激活方式：

```yaml
# application.yml
spring:
  profiles:
    active: dev    # 切换环境只需修改此处
```

或启动参数：`java -jar app.jar --spring.profiles.active=prod`

### 2.4 常用配置属性速查

| 配置项                                                       | 说明            | 示例                            |
|-----------------------------------------------------------|---------------|-------------------------------|
| `server.port`                                             | 服务端口          | `8080`                        |
| `server.servlet.context-path`                             | 上下文路径         | `/api`                        |
| `spring.datasource.*`                                     | 数据源配置         | 见上方示例                         |
| `spring.jpa.hibernate.ddl-auto`                           | DDL 策略        | `none`/`update`/`validate`    |
| `mybatis-plus.mapper-locations`                           | Mapper XML 路径 | `classpath*:/mapper/**/*.xml` |
| `mybatis-plus.configuration.map-underscore-to-camel-case` | 下划线转驼峰        | `true`                        |
| `mybatis-plus.global-config.db-config.id-type`            | 主键生成策略        | `auto`/`assign_id`            |
| `spring.jackson.date-format`                              | 日期格式化         | `yyyy-MM-dd HH:mm:ss`         |
| `spring.jackson.default-property-inclusion`               | JSON 序列化策略    | `non_null`                    |
| `spring.servlet.multipart.max-file-size`                  | 文件上传大小限制      | `10MB`                        |
| `spring.servlet.multipart.max-request-size`               | 请求大小限制        | `100MB`                       |
| `spring.cache.type`                                       | 缓存类型          | `redis`/`caffeine`            |
| `management.endpoints.web.exposure.include`               | Actuator 暴露端点 | `health,info,metrics`         |

---

## 三、Spring MVC — Controller 层开发

### 3.1 RESTful API 设计规范

- **【强制】** URL 使用小写 + 连字符，使用名词复数表示资源集合

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping
    public PageResult<UserVO> listUsers(UserQuery query) { ... }

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) { ... }

    @PostMapping
    public Result<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) { ... }

    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) { ... }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) { ... }
}
```

**HTTP 方法与 CRUD 映射：**

| 操作   | HTTP 方法  | URL 示例                  | 说明     |
|------|----------|-------------------------|--------|
| 查询列表 | `GET`    | `/users?page=1&size=10` | 分页查询   |
| 查询详情 | `GET`    | `/users/{id}`           | 单个资源   |
| 创建   | `POST`   | `/users`                | 新建资源   |
| 全量更新 | `PUT`    | `/users/{id}`           | 替换资源   |
| 部分更新 | `PATCH`  | `/users/{id}`           | 部分字段更新 |
| 删除   | `DELETE` | `/users/{id}`           | 删除资源   |

### 3.2 参数接收方式

```java
// 1. @PathVariable — 路径参数
@GetMapping("/{id}")
public Result<UserVO> getUser(@PathVariable Long id) { ... }

// 2. @RequestParam — 查询参数（URL ?key=value）
@GetMapping("/search")
public Result<List<UserVO>> search(@RequestParam String keyword,
                                    @RequestParam(defaultValue = "1") int page) { ... }

// 3. @RequestBody — JSON 请求体
@PostMapping
public Result<UserVO> create(@Valid @RequestBody CreateUserRequest request) { ... }

// 4. 对象绑定 — 自动绑定查询参数到对象
@GetMapping
public PageResult<UserVO> list(UserQuery query) { ... }

// 5. @RequestHeader — 请求头
@GetMapping("/profile")
public Result<UserVO> getProfile(@RequestHeader("Authorization") String token) { ... }
```

### 3.3 统一响应封装

**【强制】** 所有接口返回统一的响应格式，避免直接返回裸数据。

```java
@Data
@Builder
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

```java
// 分页响应
@Data
@Builder
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }
}
```

### 3.4 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

### 3.5 参数校验（Spring Validation）

```java
// 请求对象 — 使用 JSR-303 注解校验
@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    private UserRole role;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

**常用校验注解：**

| 注解                  | 说明              | 适用类型                    |
|---------------------|-----------------|-------------------------|
| `@NotNull`          | 不能为 null        | 所有类型                    |
| `@NotBlank`         | 不能为空字符串（去除空格后）  | String                  |
| `@NotEmpty`         | 不能为空（集合/数组/字符串） | Collection, String, Map |
| `@Size(min, max)`   | 长度/大小范围         | String, Collection, Map |
| `@Min` / `@Max`     | 数值范围            | 数字类型                    |
| `@Email`            | 邮箱格式            | String                  |
| `@Pattern(regexp)`  | 正则匹配            | String                  |
| `@Past` / `@Future` | 过去/未来的日期        | Date, LocalDate         |

### 3.6 Controller 层注意事项

- **【强制】** Controller 中不要写业务逻辑，只做参数校验和调用 Service
- **【强制】** 使用 `@Valid` 或 `@Validated` 触发参数校验
- **【推荐】** Controller 方法返回类型统一使用 `Result<T>` 封装
- **【推荐】** API 路径加版本号前缀：`/api/v1/...`，便于后续版本升级
- **【推荐】** 使用 Swagger/SpringDoc 生成 API 文档

```java
// SpringDoc (Swagger) 注解示例
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "创建用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    @PostMapping
    public Result<UserVO> createUser(
            @Parameter(description = "创建用户请求") @Valid @RequestBody CreateUserRequest request) {
        return Result.success(userService.createUser(request));
    }
}
```

---

## 四、Service 层 — 业务逻辑

### 4.1 接口与实现分离

```java
// 接口 — 定义业务契约
public interface UserService {
    UserVO createUser(CreateUserRequest request);
    UserVO getUserById(Long id);
    PageResult<UserVO> listUsers(UserQuery query);
    UserVO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}

// 实现
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    // 推荐使用构造器注入（Spring 4.3+ 单构造器可省略 @Autowired）
    public UserServiceImpl(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public UserVO createUser(CreateUserRequest request) {
        // 1. 校验
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BizException(400, "用户名已存在");
        }

        // 2. 转换
        User user = userConverter.toEntity(request);

        // 3. 持久化
        user = userRepository.save(user);

        // 4. 返回
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "用户不存在"));
        return userConverter.toVO(user);
    }
}
```

### 4.2 依赖注入方式

- **【强制】** 使用**构造器注入**，不要使用字段注入（`@Autowired` 在字段上）
- **【推荐】** Spring 4.3+ 只有一个构造器时可省略 `@Autowired`

```java
// 正例 — 构造器注入（推荐）
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}

// 反例 — 字段注入（不推荐）
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;    // 难以测试、隐藏依赖关系

    @Autowired
    private PaymentService paymentService;
}
```

### 4.3 事务管理

```java
// @Transactional 核心属性
@Transactional(
    rollbackFor = Exception.class,      // 所有异常都回滚（默认只回滚 RuntimeException）
    propagation = Propagation.REQUIRED,  // 传播行为
    isolation = Isolation.DEFAULT,       // 隔离级别
    timeout = 30,                        // 超时时间（秒）
    readOnly = true                      // 只读事务（查询场景优化性能）
)
public UserVO getUserById(Long id) { ... }
```

**事务传播行为：**

| 传播行为            | 说明              | 使用场景      |
|-----------------|-----------------|-----------|
| `REQUIRED`（默认）  | 有事务加入，没有则新建     | 大多数业务方法   |
| `REQUIRES_NEW`  | 总是新建事务，挂起当前事务   | 日志记录、独立操作 |
| `NESTED`        | 嵌套事务（Savepoint） | 部分失败可回滚   |
| `SUPPORTS`      | 有事务加入，没有则非事务执行  | 查询方法      |
| `NOT_SUPPORTED` | 非事务执行，挂起当前事务    | 不需要事务的操作  |
| `MANDATORY`     | 必须在已有事务中执行      | 强制要求事务的方法 |
| `NEVER`         | 必须非事务执行         | 不允许事务的操作  |

**事务注意事项：**

- **【强制】** `@Transactional` 默认只回滚 `RuntimeException`，需要回滚所有异常时指定 `rollbackFor = Exception.class`
- **【强制】** `@Transactional` 只对 `public` 方法生效（Spring AOP 限制）
- **【强制】** 同一个类中方法 A 调用方法 B，B 上的 `@Transactional` 不生效（因为没走代理）
- **【推荐】** 只读查询方法加 `@Transactional(readOnly = true)`，可提升性能

```java
// 反例 — 同类方法调用，事务不生效
@Service
public class UserServiceImpl implements UserService {
    public void methodA() {
        this.methodB();  // 直接调用，不走代理，methodB 的事务不生效
    }

    @Transactional(rollbackFor = Exception.class)
    public void methodB() { ... }
}

// 正例 — 拆分到不同 Service，通过注入调用
@Service
public class UserServiceImpl implements UserService {
    private final OrderService orderService;

    public void methodA() {
        orderService.methodB();  // 通过代理调用，事务生效
    }
}
```

---

## 五、Spring Data JPA — 数据访问层

### 5.1 Entity 实体定义

```java
@Entity
@Table(name = "sys_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) status = 1;
        if (deleted == null) deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
```

### 5.2 Repository 接口

```java
// 继承 JpaRepository 即可获得基础 CRUD + 分页能力
public interface UserRepository extends JpaRepository<User, Long> {

    // 方法名派生查询 — Spring Data 自动实现
    User findByUsername(String username);

    List<User> findByRoleAndStatus(UserRole role, Integer status);

    boolean existsByUsername(String username);

    List<User> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    // 分页查询
    Page<User> findByRole(UserRole role, Pageable pageable);

    // @Query 自定义 JPQL
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // @Query 原生 SQL
    @Query(value = "SELECT * FROM sys_user WHERE status = :status ORDER BY create_time DESC",
           nativeQuery = true)
    List<User> findByStatusNative(@Param("status") Integer status);

    // 更新/删除操作需要加 @Modifying + @Transactional
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
```

**方法名派生查询关键字速查：**

| 关键字                     | 示例                                | 等价 JPQL                                        |
|-------------------------|-----------------------------------|------------------------------------------------|
| `findBy`                | `findByUsername`                  | `WHERE u.username = ?1`                        |
| `And` / `Or`            | `findByRoleAndStatus`             | `WHERE u.role = ?1 AND u.status = ?2`          |
| `Between`               | `findByAgeBetween`                | `WHERE u.age BETWEEN ?1 AND ?2`                |
| `LessThan`              | `findByAgeLessThan`               | `WHERE u.age < ?1`                             |
| `GreaterThan`           | `findByAgeGreaterThan`            | `WHERE u.age > ?1`                             |
| `Like`                  | `findByUsernameLike`              | `WHERE u.username LIKE ?1`                     |
| `Containing`            | `findByUsernameContaining`        | `WHERE u.username LIKE %?1%`                   |
| `In`                    | `findByRoleIn`                    | `WHERE u.role IN (?1)`                         |
| `OrderBy`               | `findByRoleOrderByCreateTimeDesc` | `WHERE u.role = ?1 ORDER BY u.createTime DESC` |
| `IsNotNull` / `NotNull` | `findByEmailNotNull`              | `WHERE u.email IS NOT NULL`                    |
| `True` / `False`        | `findByDeletedFalse`              | `WHERE u.deleted = false`                      |

### 5.3 分页查询

```java
// Service 层
@Override
@Transactional(readOnly = true)
public PageResult<UserVO> listUsers(UserQuery query) {
    // 构建分页参数（页码从 0 开始）
    Pageable pageable = PageRequest.of(
            query.getPage() - 1,      // 前端传第几页，JPA 从 0 开始
            query.getSize(),
            Sort.by(Sort.Direction.DESC, "createTime")
    );

    // 构建查询条件
    Specification<User> spec = (root, criteriaQuery, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(query.getKeyword())) {
            predicates.add(cb.or(
                    cb.like(root.get("username"), "%" + query.getKeyword() + "%"),
                    cb.like(root.get("email"), "%" + query.getKeyword() + "%")
            ));
        }
        if (query.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), query.getStatus()));
        }
        predicates.add(cb.equal(root.get("deleted"), false));
        return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<User> page = userRepository.findAll(spec, pageable);
    List<UserVO> vos = page.getContent().stream()
            .map(userConverter::toVO)
            .collect(Collectors.toList());

    return PageResult.of(vos, page.getTotalElements(), query.getPage(), query.getSize());
}
```

### 5.4 JPA 注意事项

- **【强制】** N+1 问题：关联查询使用 `@EntityGraph` 或 `JOIN FETCH`
- **【强制】** 不要在 Entity 中使用 `@OneToMany(fetch = FetchType.EAGER)`，一对多默认 `LAZY`，不要改成 `EAGER`
- **【推荐】** 复杂查询使用 `Specification` 或 JPA `CriteriaBuilder`
- **【推荐】** 生产环境设置 `ddl-auto: validate` 或 `none`，不要使用 `update`/`create`

```java
// 解决 N+1 问题
@Entity
@Table(name = "sys_order")
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;
}

// 使用 EntityGraph 优化查询
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"user", "items"})
    Optional<Order> findWithUserAndItemsById(Long id);
}
```

---

## 六、MyBatis-Plus — 数据访问层（推荐）

> MyBatis-Plus（简称 MP）是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。
> 与 Spring Data JPA 二选一即可，MyBatis-Plus 在国内企业中使用更为广泛。

### 6.1 Maven 依赖

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>

<!-- 代码生成器（可选） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-generator</artifactId>
    <version>3.5.7</version>
</dependency>
```

### 6.2 application.yml 完整配置

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml        # Mapper XML 文件路径
  type-aliases-package: com.smartdoc.model.entity       # 实体类包路径
  configuration:
    map-underscore-to-camel-case: true                  # 下划线自动转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印 SQL（开发环境）
    cache-enabled: false                                # 关闭二级缓存（推荐手动控制）
  global-config:
    db-config:
      id-type: auto                                     # 主键自增
      logic-delete-field: deleted                       # 全局逻辑删除字段
      logic-delete-value: 1                             # 已删除值
      logic-not-delete-value: 0                         # 未删除值
      table-prefix: t_                                  # 表名前缀
      insert-strategy: not_null                         # INSERT 只插入非 null 字段
      update-strategy: not_null                         # UPDATE 只更新非 null 字段
```

### 6.3 Entity 实体定义

```java
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)                    // 主键自增
    private Long id;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @TableField("role")
    private String role;

    @TableField("status")
    private Integer status;

    @TableLogic                                     // 逻辑删除字段
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

**核心注解速查：**

| 注解                           | 说明     | 示例                                |
|------------------------------|--------|-----------------------------------|
| `@TableName`                 | 映射表名   | `@TableName("sys_user")`          |
| `@TableId`                   | 主键映射   | `@TableId(type = IdType.AUTO)`    |
| `@TableField`                | 字段映射   | `@TableField("user_name")`        |
| `@TableLogic`                | 逻辑删除   | 自动在查询/删除时追加条件                     |
| `@TableField(exist = false)` | 非数据库字段 | 用于关联查询的虚拟字段                       |
| `@Version`                   | 乐观锁版本号 | 更新时自动 `SET version = version + 1` |

**主键生成策略（IdType）：**

| 策略            | 说明         | 适用场景          |
|---------------|------------|---------------|
| `AUTO`        | 数据库自增      | MySQL 自增、单库单表 |
| `ASSIGN_ID`   | 雪花算法（默认）   | 分布式 ID、高并发    |
| `ASSIGN_UUID` | UUID（无连字符） | 无序主键场景        |
| `INPUT`       | 手动输入       | 业务自定义主键       |
| `NONE`        | 无策略        | 跟随全局配置        |

### 6.4 自动填充处理器

```java
@Component
public class AutoFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "status", Integer.class, 1);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 6.5 Mapper 层

```java
// 继承 BaseMapper 即可获得基础 CRUD 能力，无需编写 XML
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 自定义 SQL — 注解方式
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    User selectByUsername(@Param("username") String username);

    // 自定义 SQL — XML 方式（推荐复杂查询使用 XML）
    List<UserVO> selectUserListByCondition(@Param("query") UserQuery query);

    // 自定义分页（需要传入 IPage 参数）
    IPage<UserVO> selectUserPage(IPage<UserVO> page, @Param("query") UserQuery query);
}
```

对应的 `resources/mapper/UserMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.smartdoc.mapper.UserMapper">

    <!-- 结果映射 -->
    <resultMap id="userVOMap" type="com.smartdoc.model.vo.UserVO">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="email" property="email"/>
        <result column="role" property="role"/>
        <result column="status" property="status"/>
        <result column="create_time" property="createTime"/>
    </resultMap>

    <!-- 动态条件查询 -->
    <select id="selectUserListByCondition" resultMap="userVOMap">
        SELECT id, username, email, role, status, create_time
        FROM sys_user
        WHERE is_deleted = 0
        <if test="query.keyword != null and query.keyword != ''">
            AND (username LIKE CONCAT('%', #{query.keyword}, '%')
                 OR email LIKE CONCAT('%', #{query.keyword}, '%'))
        </if>
        <if test="query.status != null">
            AND status = #{query.status}
        </if>
        <if test="query.role != null and query.role != ''">
            AND role = #{query.role}
        </if>
        ORDER BY create_time DESC
    </select>

    <!-- 自定义分页查询 -->
    <select id="selectUserPage" resultMap="userVOMap">
        SELECT id, username, email, role, status, create_time
        FROM sys_user
        WHERE is_deleted = 0
        <if test="query.keyword != null and query.keyword != ''">
            AND (username LIKE CONCAT('%', #{query.keyword}, '%')
                 OR email LIKE CONCAT('%', #{query.keyword}, '%'))
        </if>
        ORDER BY create_time DESC
    </select>

</mapper>
```

### 6.6 Service 层（IService）

```java
// Service 接口 — 继承 IService
public interface UserService extends IService<User> {
    UserVO createUser(CreateUserRequest request);
    UserVO getUserById(Long id);
    PageResult<UserVO> listUsers(UserQuery query);
    UserVO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}

// Service 实现 — 继承 ServiceImpl，获得丰富的批量操作方法
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;

    public UserServiceImpl(UserConverter userConverter) {
        this.userConverter = userConverter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(CreateUserRequest request) {
        // 1. 校验
        long count = lambdaQuery().eq(User::getUsername, request.getUsername()).count();
        if (count > 0) {
            throw new BizException(400, "用户名已存在");
        }

        // 2. 转换
        User user = userConverter.toEntity(request);

        // 3. 持久化
        save(user);

        return userConverter.toVO(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return userConverter.toVO(user);
    }

    @Override
    public PageResult<UserVO> listUsers(UserQuery query) {
        // 方式一：使用 LambdaQueryWrapper
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper
            .like(StringUtils.hasText(query.getKeyword()), User::getUsername, query.getKeyword())
            .or()
            .like(StringUtils.hasText(query.getKeyword()), User::getEmail, query.getKeyword())
            .eq(query.getStatus() != null, User::getStatus, query.getStatus())
            .orderByDesc(User::getCreateTime);

        Page<User> result = page(page, wrapper);
        List<UserVO> vos = result.getRecords().stream()
                .map(userConverter::toVO)
                .collect(Collectors.toList());

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long id, UpdateUserRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        updateById(user);
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        removeById(id);     // 逻辑删除（自动执行 UPDATE SET is_deleted = 1）
    }
}
```

### 6.7 LambdaQueryWrapper — 构建查询条件

```java
// ==================== 基础查询 ====================

// 条件构造器 — 自动拼接 WHERE 条件（null 条件自动忽略）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, 1)                    // WHERE status = 1
       .like(User::getUsername, "frank")          // AND username LIKE '%frank%'
       .between(User::getAge, 18, 60)             // AND age BETWEEN 18 AND 60
       .isNotNull(User::getEmail)                 // AND email IS NOT NULL
       .orderByDesc(User::getCreateTime);         // ORDER BY create_time DESC
List<User> users = userMapper.selectList(wrapper);

// ==================== 链式查询（IService 提供） ====================

// lambdaQuery — 链式查询
List<User> activeUsers = lambdaQuery()
        .eq(User::getStatus, 1)
        .like(User::getUsername, "admin")
        .list();

// 查询一条
User user = lambdaQuery()
        .eq(User::getUsername, "frank")
        .one();

// 查询数量
long count = lambdaQuery()
        .eq(User::getRole, "ADMIN")
        .count();

// ==================== 更新构造器 ====================

// LambdaUpdateWrapper — 条件更新
LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(User::getId, 1L)
             .set(User::getStatus, 0)
             .set(User::getEmail, "new@example.com");
userMapper.update(null, updateWrapper);
// UPDATE sys_user SET status = 0, email = 'new@example.com' WHERE id = 1

// lambdaUpdate — 链式更新
lambdaUpdate()
    .eq(User::getId, 1L)
    .set(User::getStatus, 0)
    .update();

// ==================== QueryWrapper — 非 Lambda 方式 ====================

// 注意：QueryWrapper 使用字段名字符串，容易拼错，不推荐
// 优先使用 LambdaQueryWrapper（编译期检查字段名）
QueryWrapper<User> qw = new QueryWrapper<>();
qw.eq("status", 1).like("username", "frank");     // 不推荐
```

**条件构造器方法速查：**

| 方法            | SQL                       | 说明                          |
|---------------|---------------------------|-----------------------------|
| `eq`          | `= `                      | 等于                          |
| `ne`          | `<>`                      | 不等于                         |
| `gt` / `ge`   | `>` / `>=`                | 大于 / 大于等于                   |
| `lt` / `le`   | `<` / `<=`                | 小于 / 小于等于                   |
| `between`     | `BETWEEN ... AND ...`     | 区间                          |
| `notBetween`  | `NOT BETWEEN ... AND ...` | 不在区间                        |
| `like`        | `LIKE '%x%'`              | 模糊匹配                        |
| `likeLeft`    | `LIKE '%x'`               | 左模糊                         |
| `likeRight`   | `LIKE 'x%'`               | 右模糊                         |
| `in`          | `IN (...)`                | 包含                          |
| `notIn`       | `NOT IN (...)`            | 不包含                         |
| `isNull`      | `IS NULL`                 | 为空                          |
| `isNotNull`   | `IS NOT NULL`             | 不为空                         |
| `orderByDesc` | `ORDER BY ... DESC`       | 降序                          |
| `orderByAsc`  | `ORDER BY ... ASC`        | 升序                          |
| `groupBy`     | `GROUP BY ...`            | 分组                          |
| `having`      | `HAVING ...`              | 分组过滤                        |
| `or`          | `OR`                      | 或条件                         |
| `and`         | `AND`                     | 且条件（嵌套）                     |
| `last`        | 拼接到 SQL 末尾                | `LIMIT 10` 等（有 SQL 注入风险，慎用） |
| `exists`      | `EXISTS (...)`            | 存在                          |
| `notExists`   | `NOT EXISTS (...)`        | 不存在                         |

**条件构造器的条件拼接（自动忽略 null）：**

```java
// 第二个参数为 boolean，为 false 时该条件不拼接
// 这比手动 if-else 判断简洁很多
wrapper.eq(status != null, User::getStatus, status)              // status 非 null 时才拼接
       .like(StringUtils.hasText(keyword), User::getUsername, keyword);  // keyword 非空时才拼接
```

### 6.8 分页查询

```java
@Configuration
public class MybatisPlusConfig {

    // 分页插件 — 必须配置，否则分页不生效
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

```java
// Service 层分页
public PageResult<UserVO> listUsers(UserQuery query) {
    // Page 对象 — 第一个参数为当前页（从1开始），第二个为每页大小
    Page<User> page = new Page<>(query.getPage(), query.getSize());

    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(query.getStatus() != null, User::getStatus, query.getStatus())
           .like(StringUtils.hasText(query.getKeyword()), User::getUsername, query.getKeyword())
           .orderByDesc(User::getCreateTime);

    Page<User> result = baseMapper.selectPage(page, wrapper);

    List<UserVO> vos = result.getRecords().stream()
            .map(userConverter::toVO)
            .collect(Collectors.toList());

    return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
}
```

### 6.9 逻辑删除

```java
// Entity 中标记逻辑删除字段
@TableName("sys_user")
public class User {
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}

// 使用 — 自动处理
userMapper.deleteById(1L);
// 实际执行：UPDATE sys_user SET is_deleted = 1 WHERE id = 1 AND is_deleted = 0

userMapper.selectList(null);
// 实际执行：SELECT * FROM sys_user WHERE is_deleted = 0

userMapper.selectById(1L);
// 实际执行：SELECT * FROM sys_user WHERE id = 1 AND is_deleted = 0
```

### 6.10 乐观锁

```java
// Entity 中加版本号字段
public class User {
    @Version
    private Integer version;
}

// 使用
User user = userMapper.selectById(1L);     // version = 1
user.setUsername("newName");
userMapper.updateById(user);
// 实际执行：UPDATE sys_user SET username = 'newName', version = 2
//           WHERE id = 1 AND version = 1
// 如果受影响行数为 0，说明被其他线程修改过
```

### 6.11 IService 批量操作

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 批量插入
    public void batchCreate(List<CreateUserRequest> requests) {
        List<User> users = requests.stream()
                .map(userConverter::toEntity)
                .collect(Collectors.toList());
        saveBatch(users, 500);              // 每批 500 条
    }

    // 批量更新
    public void batchUpdate(List<User> users) {
        updateBatchById(users, 500);
    }

    // 批量保存或更新
    public void saveOrUpdateBatch(List<User> users) {
        saveOrUpdateBatch(users, 500);
    }
}
```

### 6.12 JPA vs MyBatis-Plus 选型对比

| 对比维度       | Spring Data JPA                     | MyBatis-Plus                |
|------------|-------------------------------------|-----------------------------|
| **上手难度**   | 较高（需理解 EntityManager、Session）       | 较低（SQL 可控，学习曲线平缓）           |
| **SQL 控制** | 弱（自动生成，复杂查询需 JPQL/Criteria）         | 强（灵活编写 SQL，XML/注解双模式）       |
| **代码量**    | 少（方法名派生查询自动实现）                      | 少（BaseMapper/IService 封装完善） |
| **动态条件**   | `Specification` / `CriteriaBuilder` | `LambdaQueryWrapper`（更简洁）   |
| **分页**     | `Pageable`（页码从 0 开始）                | `Page`（页码从 1 开始，更直觉）        |
| **批量操作**   | `saveAll()`（需手动 flush）              | `saveBatch()`（内置分批，性能更好）    |
| **N+1 问题** | 需手动处理（`@EntityGraph`）               | 需手动编写 `JOIN` 查询             |
| **国内生态**   | 外企/国际化团队常用                          | 国内主流，社区活跃                   |

**建议：国内项目优先使用 MyBatis-Plus。** 复杂报表查询、多表关联、SQL 调优场景 MyBatis-Plus 更有优势。

### 6.13 MyBatis-Plus 注意事项

- **【强制】** 必须配置分页插件 `PaginationInnerInterceptor`，否则 `selectPage` 不生效
- **【强制】** 使用 `LambdaQueryWrapper` 而非 `QueryWrapper`，避免字段名拼错
- **【强制】** 逻辑删除字段用 `@TableLogic` 标注，不要手动在 SQL 中判断 `is_deleted = 0`
- **【推荐】** Entity 中使用 `@TableField(fill = FieldFill.INSERT)` + `MetaObjectHandler` 自动填充时间字段
- **【推荐】** 复杂查询使用 XML 方式，简单 CRUD 使用 `BaseMapper` 内置方法
- **【推荐】** Service 层继承 `IService<User>`，可使用 `lambdaQuery()`/`lambdaUpdate()` 链式 API
- **【推荐】** 批量操作使用 `saveBatch(list, batchSize)`，默认每批 1000 条
- **【推荐】** `@MapperScan` 在启动类上配置扫描 Mapper 包路径

```java
@SpringBootApplication
@MapperScan("com.smartdoc.mapper")     // 扫描 Mapper 接口
public class SmartDocApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartDocApplication.class, args);
    }
}
```

---

## 七、Spring Security — 安全框架

### 7.1 基础安全配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 路径权限
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()       // 登录注册放行
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // 管理后台需要 ADMIN
                .requestMatchers("/api/v1/**").authenticated()        // 其他接口需要认证
                .anyRequest().permitAll()
            )
            // 禁用 CSRF（前后端分离项目）
            .csrf(csrf -> csrf.disable())
            // Session 管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                })
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 7.2 JWT Token 工具类

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;    // 默认 24 小时

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 7.3 密码存储

- **【强制】** 使用 `BCryptPasswordEncoder`，不要使用 MD5/SHA-1

```java
@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        // 正例 — BCrypt 加密
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

---

## 八、Spring AOP — 面向切面编程

### 8.1 统一日志切面

```java
@Aspect
@Component
@Slf4j
public class WebLogAspect {

    @Pointcut("execution(* com.smartdoc.controller..*.*(..))")
    public void webLog() {}

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String args = JsonUtil.toJson(joinPoint.getArgs());

        log.info(">>> {} {} args: {}", method, uri, args);

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.info("<<< {} {} cost: {}ms", method, uri, cost);
            return result;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("<<< {} {} cost: {}ms error: {}", method, uri, cost, e.getMessage());
            throw e;
        }
    }
}
```

### 8.2 常用 AOP 注解

| 注解                | 说明            | 示例         |
|-------------------|---------------|------------|
| `@Before`         | 方法执行前         | 参数校验前置处理   |
| `@After`          | 方法执行后（无论是否异常） | 资源清理       |
| `@AfterReturning` | 方法正常返回后       | 记录返回值      |
| `@AfterThrowing`  | 方法抛出异常后       | 异常告警       |
| `@Around`         | 环绕通知（最强大）     | 统计耗时、日志、权限 |

### 8.3 自定义注解 + AOP 实现

```java
// 自定义注解 — 操作日志
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String value();        // 操作描述
    OperateType type();    // 操作类型：CREATE/UPDATE/DELETE/QUERY
}

// AOP 切面
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @AfterReturning(pointcut = "@annotation(opLog)", returning = "result")
    public void recordLog(JoinPoint joinPoint, OperationLog opLog, Object result) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String method = joinPoint.getSignature().toShortString();
        log.info("操作日志: user={}, action={}, method={}, type={}",
                username, opLog.value(), method, opLog.type());
        // 也可以异步写入数据库
    }
}

// 使用
@OperationLog(value = "创建用户", type = OperateType.CREATE)
@PostMapping
public Result<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) { ... }
```

---

## 九、Spring Cache — 缓存

### 9.1 缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))                    // 默认过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();                          // 不缓存 null

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
```

### 9.2 缓存注解使用

```java
@Service
public class UserServiceImpl implements UserService {

    // 查询时缓存 — key 为参数 id
    @Cacheable(value = "user", key = "#id")
    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        log.info("缓存未命中，查询数据库: id={}", id);
        return userRepository.findById(id)
                .map(userConverter::toVO)
                .orElseThrow(() -> new BizException(404, "用户不存在"));
    }

    // 更新时刷新缓存
    @CachePut(value = "user", key = "#id")
    @Override
    public UserVO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "用户不存在"));
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        return userConverter.toVO(user);
    }

    // 删除时清除缓存
    @CacheEvict(value = "user", key = "#id")
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 清除所有 user 缓存
    @CacheEvict(value = "user", allEntries = true)
    @Override
    public void refreshAllUserCache() {
        log.info("清除所有用户缓存");
    }
}
```

**缓存注解对比：**

| 注解            | 说明                 | 使用场景    |
|---------------|--------------------|---------|
| `@Cacheable`  | 有缓存返回缓存，没有则执行方法并缓存 | 查询      |
| `@CachePut`   | 总是执行方法，将结果放入缓存     | 更新后刷新缓存 |
| `@CacheEvict` | 清除缓存               | 删除/批量刷新 |

---

## 十、Spring Async — 异步处理

### 10.1 线程池配置

```java
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean("businessExecutor")
    public Executor businessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("biz-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

### 10.2 异步方法

```java
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Async("businessExecutor")
    @Override
    public void sendAsyncEmail(String to, String subject, String content) {
        try {
            // 模拟发送邮件
            Thread.sleep(2000);
            log.info("邮件发送成功: to={}", to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("邮件发送失败: to={}", to, e);
        }
    }

    @Async("businessExecutor")
    @Override
    public CompletableFuture<String> asyncTaskWithResult(String input) {
        String result = doHeavyWork(input);
        return CompletableFuture.completedFuture(result);
    }
}
```

### 10.3 定时任务

```java
@Component
@Slf4j
public class ScheduledTasks {

    // 固定间隔（上次执行结束后间隔 60 秒）
    @Scheduled(fixedDelay = 60000)
    public void cleanupTempFiles() {
        log.info("开始清理临时文件...");
    }

    // 固定频率（每 5 分钟执行，不管上次是否结束）
    @Scheduled(fixedRate = 300000)
    public void syncData() {
        log.info("开始同步数据...");
    }

    // Cron 表达式（每天凌晨 2 点执行）
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyReport() {
        log.info("开始生成日报...");
    }
}
```

**Cron 表达式格式：`秒 分 时 日 月 周`**

| 表达式                    | 说明              |
|------------------------|-----------------|
| `0 0 2 * * ?`          | 每天凌晨 2 点        |
| `0 */5 * * * ?`        | 每 5 分钟          |
| `0 0 9-18 * * MON-FRI` | 工作日 9 点到 18 点整点 |
| `0 0 0 1 * ?`          | 每月 1 号零点        |

---

## 十一、Spring Actuator — 监控与运维

### 11.1 依赖与配置

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

### 11.2 常用端点

| 端点                  | 说明      | 用途              |
|---------------------|---------|-----------------|
| `/actuator/health`  | 健康检查    | K8s/Docker 存活探针 |
| `/actuator/info`    | 应用信息    | 版本号、构建时间        |
| `/actuator/metrics` | 性能指标    | JVM、HTTP、数据库指标  |
| `/actuator/env`     | 环境变量    | 查看配置（生产慎开）      |
| `/actuator/loggers` | 日志级别    | 动态调整日志级别        |
| `/actuator/beans`   | Bean 列表 | 排查 Bean 注册问题    |

### 11.3 自定义健康检查

```java
@Component
public class LlmServiceHealthIndicator implements HealthIndicator {

    private final LlmService llmService;

    @Override
    public Health health() {
        try {
            if (llmService.isAvailable()) {
                return Health.up()
                        .withDetail("provider", "zhipu")
                        .withDetail("latency", llmService.getLatency() + "ms")
                        .build();
            }
            return Health.down().withDetail("error", "LLM 服务不可用").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

## 十二、Spring Boot 测试

### 12.1 测试分层

```java
// 1. 单元测试 — 只测 Service 逻辑，Mock 依赖
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(BizException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void shouldReturnUserWhenExists() {
        User user = User.builder().id(1L).username("frank").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserVO result = userService.getUserById(1L);

        assertThat(result.getUsername()).isEqualTo("frank");
    }
}
```

```java
// 2. 集成测试 — 启动 Spring 上下文
@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = User.builder().username("test").email("test@example.com").build();
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("test");
    }
}
```

```java
// 3. Controller 测试 — MockMvc
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUserById() throws Exception {
        UserVO vo = UserVO.builder().id(1L).username("frank").build();
        when(userService.getUserById(1L)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("frank"));
    }
}
```

### 12.2 测试注意事项

- **【强制】** 测试方法命名清晰表达意图，使用 `should...When...` 格式
- **【强制】** 单元测试不能依赖外部环境（数据库、网络），使用 Mock
- **【推荐】** 集成测试使用 `@Transactional` 自动回滚，不污染数据库
- **【推荐】** 测试覆盖率目标：核心业务逻辑 ≥ 80%

---

## 十三、跨域与拦截器

### 13.1 跨域配置

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/**");
    }
}
```

### 13.2 登录拦截器

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求直接放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
            return false;
        }

        // 解析用户信息放入上下文
        Claims claims = jwtTokenProvider.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("username", claims.getSubject());
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

---

## 十四、常用设计模式在 Spring 中的应用

| 设计模式  | Spring 中的应用                          | 说明               |
|-------|--------------------------------------|------------------|
| 工厂模式  | `BeanFactory` / `ApplicationContext` | 创建和管理 Bean       |
| 单例模式  | Spring Bean 默认 `singleton`           | 容器中只有一个实例        |
| 代理模式  | AOP / `@Transactional`               | JDK 动态代理 / CGLIB |
| 模板方法  | `JdbcTemplate` / `RestTemplate`      | 封装固定流程           |
| 观察者模式 | `@EventListener` / ApplicationEvent  | 事件驱动             |
| 策略模式  | `Resource` 接口的多实现                    | 根据前缀选择不同资源加载策略   |
| 适配器模式 | `HandlerAdapter` / `PasswordEncoder` | 统一接口适配不同实现       |
| 装饰器模式 | `BeanWrapper` / 缓存代理                 | 增强对象功能           |

### 事件驱动示例

```java
// 定义事件
public class OrderCreatedEvent extends ApplicationEvent {
    private final Long orderId;
    private final Long userId;

    public OrderCreatedEvent(Object source, Long orderId, Long userId) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
    }
}

// 发布事件
@Service
public class OrderServiceImpl implements OrderService {
    private final ApplicationEventPublisher eventPublisher;

    public void createOrder(CreateOrderRequest request) {
        Order order = saveOrder(request);
        // 发布事件，解耦核心逻辑与后续动作
        eventPublisher.publishEvent(new OrderCreatedEvent(this, order.getId(), order.getUserId()));
    }
}

// 监听事件 — 异步处理
@Component
@Slf4j
public class OrderEventListener {

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("处理订单创建后续逻辑: orderId={}", event.getOrderId());
        sendOrderNotification(event.getOrderId());
        updateUserStatistics(event.getUserId());
    }
}
```

---

## 十五、性能优化与最佳实践

### 15.1 数据库性能

- **【推荐】** 查询指定字段，不要 `SELECT *`
- **【推荐】** 大数据量分页使用 `Pageable`，避免一次性加载
- **【推荐】** 合理使用索引，定期分析慢查询
- **【推荐】** 批量操作使用 `saveAll()`，不要循环单条 `save()`

```java
// 正例 — 批量插入
userRepository.saveAll(users);

// 反例 — 循环单条插入
for (User user : users) {
    userRepository.save(user);
}
```

### 15.2 缓存策略

- **【推荐】** 热点数据使用 Redis 缓存，设置合理过期时间
- **【推荐】** 缓存穿透防护：缓存空值（短过期时间）
- **【推荐】** 缓存雪崩防护：过期时间加随机偏移
- **【推荐】** 使用 `@Cacheable` 的 `unless` 条件排除不需要缓存的结果

### 15.3 异步与并发

- **【推荐】** 耗时操作使用 `@Async` 异步处理（发邮件、生成报表）
- **【推荐】** 合理配置线程池参数，避免无界队列导致 OOM
- **【推荐】** 使用 `CompletableFuture` 组合多个异步任务

```java
@Async("businessExecutor")
public CompletableFuture<UserVO> getUserAsync(Long id) {
    return CompletableFuture.completedFuture(userRepository.findById(id)
            .map(userConverter::toVO)
            .orElseThrow(() -> new BizException(404, "用户不存在")));
}

// 组合多个异步任务
CompletableFuture<UserVO> userFuture = userService.getUserAsync(1L);
CompletableFuture<List<OrderVO>> ordersFuture = orderService.getOrdersAsync(1L);

CompletableFuture.allOf(userFuture, ordersFuture).join();

UserVO user = userFuture.get();
List<OrderVO> orders = ordersFuture.get();
```

### 15.4 JSON 序列化优化

```yaml
# application.yml — Jackson 全局配置
spring:
  jackson:
    default-property-inclusion: non_null     # 不序列化 null 字段
    date-format: yyyy-MM-dd HH:mm:ss        # 日期格式
    time-zone: Asia/Shanghai                # 时区
    serialization:
      write-dates-as-timestamps: false      # 日期不转时间戳
      fail-on-empty-beans: false            # 空 Bean 不报错
    deserialization:
      fail-on-unknown-properties: false     # 未知属性不报错
```

---

## 十六、Spring Boot 3.x 新特性速览

| 特性                  | 说明                                                      |
|---------------------|---------------------------------------------------------|
| **Java 17+ 基线**     | 最低要求 Java 17，推荐 Java 21                                 |
| **Jakarta EE 迁移**   | `javax.*` → `jakarta.*`（Servlet、JPA、Validation 等）       |
| **GraalVM 原生镜像**    | 支持 AOT 编译为原生可执行文件，启动速度毫秒级                               |
| **可观测性**            | 内置 Micrometer + OpenTelemetry 支持                        |
| **Virtual Threads** | Java 21 虚拟线程支持，配置 `spring.threads.virtual.enabled=true` |
| **ProblemDetail**   | 标准化错误响应（RFC 7807）                                       |
| **HTTP Client**     | 新增 `RestClient`（声明式 HTTP 客户端）                           |
| **SSL Bundle**      | 统一 SSL 证书配置                                             |

```java
// Spring Boot 3 — ProblemDetail 标准错误响应
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ProblemDetail> handleBizException(BizException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(e.getCode()), e.getMessage());
        detail.setTitle("业务异常");
        detail.setProperty("errorCode", e.getErrorCode());
        return ResponseEntity.of(detail).build();
    }
}

// Spring Boot 3 — RestClient
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.example.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

// 使用
String result = restClient.get()
        .uri("/users/{id}", 1L)
        .retrieve()
        .body(String.class);
```

---

## 十七、常见踩坑与避坑指南

| 问题                            | 原因                           | 解决方案                                           |
|-------------------------------|------------------------------|------------------------------------------------|
| `@Transactional` 不生效          | 同类方法调用 / 非 public 方法         | 通过注入调用 / 确保是 public                            |
| `LazyInitializationException` | Session 关闭后访问延迟加载属性          | 使用 `@EntityGraph` 或 `JOIN FETCH`               |
| 循环依赖报错                        | A 注入 B，B 注入 A                | 使用 `@Lazy` 或重构设计                               |
| JSON 日期格式不对                   | 默认序列化为时间戳                    | 配置 `spring.jackson.date-format`                |
| 中文乱码                          | 编码不一致                        | 配置 `server.servlet.encoding.charset=UTF-8`     |
| `NullPointerException` 自动拆箱   | 返回包装类型但未判空                   | 使用 `Optional` 或判空                              |
| `@Value` 注入失败                 | 配置文件中没有对应 key                | 提供默认值 `@Value("${key:default}")`               |
| 文件上传失败                        | 超过默认大小限制                     | 配置 `spring.servlet.multipart.max-file-size`    |
| Actuator 端点 404               | 默认只暴露 health/info            | 配置 `management.endpoints.web.exposure.include` |
| 异步方法不生效                       | 同类调用 / 启动类未加 `@EnableAsync`  | 通过注入调用 / 添加 `@EnableAsync`                     |
| MyBatis-Plus 分页不生效            | 未配置分页插件                      | 配置 `PaginationInnerInterceptor`                |
| MyBatis-Plus 字段映射失败           | 数据库下划线与 Java 驼峰不匹配           | 配置 `map-underscore-to-camel-case: true`        |
| Mapper 注入失败                   | 未配置 `@MapperScan`            | 启动类加 `@MapperScan("com.xxx.mapper")`           |
| MyBatis-Plus 逻辑删除不生效          | 未在 Entity 标注 `@TableLogic`   | 添加 `@TableLogic` 注解                            |
| MyBatis-Plus 自动填充不生效          | 未注册 `MetaObjectHandler` Bean | 创建类实现 `MetaObjectHandler` 并加 `@Component`      |

---

> 参考来源：
> - Spring Boot 官方文档 — https://docs.spring.io/spring-boot/docs/current/reference/html/
> - Spring Framework 官方文档 — https://docs.spring.io/spring-framework/reference/
> - Spring Security 官方文档 — https://docs.spring.io/spring-security/reference/
> - Spring Data JPA 官方文档 — https://docs.spring.io/spring-data/jpa/reference/
> - MyBatis-Plus 官方文档 — https://baomidou.com/
> - Baeldung Spring Tutorials — https://www.baeldung.com/spring-tutorials
