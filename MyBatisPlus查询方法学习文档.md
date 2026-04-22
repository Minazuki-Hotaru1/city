# MyBatis-Plus 查询方法学习文档

这份文档结合你当前 `city` 项目的写法，帮助你快速理解 MyBatis-Plus 常见查询方法。

## 1. 先理解几个核心对象

在 MyBatis-Plus 里，最常见的查询一般围绕这几个对象展开：

- `BaseMapper<T>`
  作用：MyBatis-Plus 提供的基础 Mapper，封装了很多通用的增删改查方法。
- `QueryWrapper<T>`
  作用：用来拼接普通条件查询。
- `LambdaQueryWrapper<T>`
  作用：和 `QueryWrapper` 类似，但字段写法更安全，推荐优先使用。
- `IService<T>` / `ServiceImpl`
  作用：如果你使用 Service 层，也可以直接用它封装的查询方法。

你项目里已经有一个典型例子：

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.eq("username", username)
       .eq("password", password);

return adminMapper.selectOne(wrapper);
```

它的意思是：

- 查询 `admin` 表
- 条件是 `username = ?` 且 `password = ?`
- 期望只返回一条数据

## 2. BaseMapper 常见查询方法

假设你有：

```java
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
```

那么 `adminMapper` 就可以直接使用下面这些方法。

### 2.1 `selectById`

按主键查询一条记录。

```java
Admin admin = adminMapper.selectById("1");
```

适用场景：

- 已知主键 id
- 查询单条详情

### 2.2 `selectOne`

按条件查询一条记录。

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin");

Admin admin = adminMapper.selectOne(wrapper);
```

注意：

- 如果查出来多条，可能报错
- 所以要确保条件能唯一确定一条数据

### 2.3 `selectList`

按条件查询多条记录。

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.like("username", "a");

List<Admin> list = adminMapper.selectList(wrapper);
```

适用场景：

- 列表查询
- 模糊搜索
- 多条件组合查询

### 2.4 `selectCount`

统计符合条件的数据条数。

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin");

Long count = adminMapper.selectCount(wrapper);
```

适用场景：

- 判断某个用户是否存在
- 分页前先统计总数

### 2.5 `selectMaps`

查询结果不转实体类，而是返回 `Map`。

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.select("id", "username");

List<Map<String, Object>> list = adminMapper.selectMaps(wrapper);
```

适用场景：

- 只想查部分字段
- 不想完整映射实体对象

### 2.6 `selectPage`

分页查询。

```java
Page<Admin> page = new Page<>(1, 10);
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.like("username", "a");

Page<Admin> result = adminMapper.selectPage(page, wrapper);
```

参数解释：

- `1`：第几页
- `10`：每页多少条

常用结果：

- `result.getRecords()`：当前页数据
- `result.getTotal()`：总条数
- `result.getPages()`：总页数

## 3. QueryWrapper 常见条件方法

`QueryWrapper` 最核心的作用，就是“拼接 where 条件”。

### 3.1 `eq`

等于，SQL 类似于 `=`

```java
wrapper.eq("username", "admin");
```

SQL 效果：

```sql
where username = 'admin'
```

### 3.2 `ne`

不等于，SQL 类似于 `!=`

```java
wrapper.ne("username", "admin");
```

### 3.3 `gt`

大于，SQL 类似于 `>`

```java
wrapper.gt("age", 18);
```

### 3.4 `ge`

大于等于，SQL 类似于 `>=`

```java
wrapper.ge("age", 18);
```

### 3.5 `lt`

小于，SQL 类似于 `<`

```java
wrapper.lt("age", 60);
```

### 3.6 `le`

小于等于，SQL 类似于 `<=`

```java
wrapper.le("age", 60);
```

### 3.7 `like`

模糊匹配，SQL 类似于 `like '%xxx%'`

```java
wrapper.like("username", "adm");
```

SQL 效果：

```sql
where username like '%adm%'
```

### 3.8 `likeRight`

右模糊，SQL 类似于 `like 'xxx%'`

```java
wrapper.likeRight("username", "ad");
```

### 3.9 `likeLeft`

左模糊，SQL 类似于 `like '%xxx'`

```java
wrapper.likeLeft("username", "in");
```

### 3.10 `in`

范围查询，SQL 类似于 `in (...)`

```java
wrapper.in("id", 1, 2, 3, 4);
```

### 3.11 `between`

区间查询。

```java
wrapper.between("age", 18, 30);
```

SQL 效果：

```sql
where age between 18 and 30
```

### 3.12 `isNull` / `isNotNull`

判空查询。

```java
wrapper.isNull("email");
wrapper.isNotNull("username");
```

## 4. 排序与字段选择

### 4.1 `orderByAsc`

升序排序。

```java
wrapper.orderByAsc("username");
```

### 4.2 `orderByDesc`

降序排序。

```java
wrapper.orderByDesc("id");
```

### 4.3 `select`

只查指定字段。

```java
wrapper.select("id", "username");
```

适用场景：

- 减少无用字段返回
- 某些页面只需要少数字段

## 5. 条件组合写法

### 5.1 多个条件同时成立

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin")
       .eq("password", "123456");
```

SQL 效果：

```sql
where username = 'admin' and password = '123456'
```

这就是你登录功能当前最接近的写法。

### 5.2 `or`

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin")
       .or()
       .eq("username", "root");
```

SQL 效果：

```sql
where username = 'admin' or username = 'root'
```

### 5.3 `and` 嵌套

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.and(w -> w.eq("username", "admin").eq("password", "123456"));
```

### 5.4 `or` 嵌套

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.or(w -> w.eq("username", "admin").eq("password", "123456"));
```

## 6. 更推荐的写法：LambdaQueryWrapper

`QueryWrapper` 里字段名是字符串：

```java
wrapper.eq("username", username);
```

这种写法的问题是：

- 字段名写错，编译不会报错
- 实体类字段改名时，这里不会自动跟着改

所以更推荐：

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, username)
       .eq(Admin::getPassword, password);
```

优点：

- 字段更安全
- 有代码提示
- 重构更方便

## 7. 结合你项目的登录案例

你现在的登录本质上是在做：

```java
public Admin login(String username, String password) {
    QueryWrapper<Admin> wrapper = new QueryWrapper<>();
    wrapper.eq("username", username)
           .eq("password", password);

    return adminMapper.selectOne(wrapper);
}
```

我更推荐你改成：

```java
public Admin login(String username, String password) {
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Admin::getUsername, username)
           .eq(Admin::getPassword, password);

    return adminMapper.selectOne(wrapper);
}
```

这样更规范，也更不容易写错字段名。

## 8. 常见学习案例

### 案例 1：按用户名查询用户是否存在

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, "admin");

Long count = adminMapper.selectCount(wrapper);
```

意思：

- 查用户名是 `admin` 的记录有几条

### 案例 2：查询所有管理员列表

```java
List<Admin> list = adminMapper.selectList(null);
```

意思：

- 不加条件
- 查询整张表

### 案例 3：查询 id 在指定范围内的数据

```java
QueryWrapper<Admin> wrapper = new QueryWrapper<>();
wrapper.in("id", "1", "2", "3");

List<Admin> list = adminMapper.selectList(wrapper);
```

### 案例 4：用户名模糊搜索

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.like(Admin::getUsername, "ad");

List<Admin> list = adminMapper.selectList(wrapper);
```

意思：

- 查所有用户名中包含 `ad` 的记录

### 案例 5：按 id 倒序查询

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.orderByDesc(Admin::getId);

List<Admin> list = adminMapper.selectList(wrapper);
```

## 9. 查询时常见坑

### 9.1 `selectOne` 只能保证一条

如果条件可能查出多条，就不要用 `selectOne`，应该用 `selectList`。

### 9.2 字段名和数据库列名要对应

例如：

- 实体类字段：`username`
- 数据库列名：`username`

如果数据库列名和实体类不一致，就要做映射配置。

### 9.3 不要把密码明文查询当成长期方案

你现在的登录案例适合学习查询，但真实项目里更推荐：

- 数据库存密码哈希值
- 后端对输入密码加密后再比对

### 9.4 优先用 Lambda 写法

因为字符串字段名容易写错，例如：

```java
wrapper.eq("usernmae", username);
```

这个拼写错了，编译也不会提醒你。

## 10. 一个学习顺序建议

如果你刚开始学 MyBatis-Plus，建议按这个顺序练：

1. 学会 `selectById`
2. 学会 `selectOne`
3. 学会 `selectList`
4. 学会 `eq`、`like`、`in`
5. 学会 `orderByDesc`
6. 学会 `selectCount`
7. 学会 `LambdaQueryWrapper`
8. 最后再学分页查询

## 11. 你现在最值得记住的模板

### 查询单条

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, username);

Admin admin = adminMapper.selectOne(wrapper);
```

### 查询多条

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.like(Admin::getUsername, "ad");

List<Admin> list = adminMapper.selectList(wrapper);
```

### 统计数量

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, "admin");

Long count = adminMapper.selectCount(wrapper);
```

### 登录校验

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, username)
       .eq(Admin::getPassword, password);

Admin admin = adminMapper.selectOne(wrapper);
```

## 12. 最后总结

你可以先把 MyBatis-Plus 查询理解成两部分：

- `BaseMapper` 负责“执行什么查询”
- `Wrapper` 负责“查询条件怎么写”

最常用的组合就是：

```java
LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Admin::getUsername, username);

Admin admin = adminMapper.selectOne(wrapper);
```

如果你愿意，我下一步还可以继续帮你补一份：

- `MyBatisPlus增删改学习文档`
- 或者 `专门结合你 city 项目实体类的练习题文档`
