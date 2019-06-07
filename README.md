# 巧用 MyBatis 构建树形结构

在项目中我们经常会碰到这种格式的数据, 需要将其转化为树形结构:

| menu_id | parent_id | menu_name |       url        |
| :-----: | :-------: | :-------: | ---------------- |
|    1    |     0     | 权限管理  | #                |
|    2    |     1     | 用户管理  | /user/index      |
|    3    |     1     | 角色管理  | /role/index      |
|    4    |     1     | 菜单权限  | /menu/index      |
|   11    |     0     | 系统监控  | #                |
|   12    |    11     | 登录日志  | /log/login/index |
|   19    |    11     | 操作日志  | /log/sys/index   |
|   20    |    11     | 在线用户  | /online/index    |
|   27    |     1     | 操作权限  | /operator/index  |
|   28    |     1     | 部门管理  | /dept/index      |
|   29    |    11     | 系统管理  | /system/index    |
|   30    |     0     | 账号关联  | /oauth2/index    |


一般的做法是查询出所有, 然后递归构建树形结构, 但其实可以巧用 MyBatis 在查询时就进行转换, 这用到了 MyBatis 的 `resultMap` 功能.

首先由以下表结构定义:

```sql
create table menu
(
    menu_id   int primary key auto_increment comment '菜单 ID',
    parent_id int          not null,
    menu_name varchar(20)  null comment '菜单名称',
    url       varchar(100) null comment '菜单 URL'
);
```

实体类定义:

```java
public class Menu {

    private Integer menuId;

    private Integer parentId;

    private String menuName;

    private String url;

    private List<Menu> children;
}

```


resultMap 定义:

```xml
<resultMap id="BaseResultTreeMap" type="im.zhaojun.model.Menu">
    <id column="menu_id" jdbcType="INTEGER" property="menuId"/>
    <result column="parent_id" jdbcType="INTEGER" property="parentId"/>
    <result column="menu_name" jdbcType="VARCHAR" property="menuName"/>
    <result column="url" jdbcType="VARCHAR" property="url"/>
    <collection property="children" ofType="Menu" select="selectTree" column="{parent_id = menu_id}"/>
</resultMap>
```

查询定义:
```xml
<select id="selectMenuTree" resultMap="BaseResultTreeMap">
    select *
    from menu
    <where>
        <choose>
            <when test="parent_id!=null">
                and parent_id = #{parent_id}
            </when>
            <otherwise>
                and parent_id = 0
            </otherwise>
        </choose>
    </where>
</select>
```

查询出的结果集:

```json
[
    {
        "menuId": 1,
        "parentId": 0,
        "menuName": "权限管理",
        "url": "#",
        "children": [
            {
                "menuId": 2,
                "parentId": 1,
                "menuName": "用户管理",
                "url": "/user/index",
                "children": []
            },
            {
                "menuId": 3,
                "parentId": 1,
                "menuName": "角色管理",
                "url": "/role/index",
                "children": []
            },
            {
                "menuId": 4,
                "parentId": 1,
                "menuName": "菜单权限",
                "url": "/menu/index",
                "children": []
            },
            {
                "menuId": 27,
                "parentId": 1,
                "menuName": "操作权限",
                "url": "/operator/index",
                "children": []
            },
            {
                "menuId": 28,
                "parentId": 1,
                "menuName": "部门管理",
                "url": "/dept/index",
                "children": []
            }
        ]
    },
    {
        "menuId": 11,
        "parentId": 0,
        "menuName": "系统监控",
        "url": "#",
        "children": [
            {
                "menuId": 12,
                "parentId": 11,
                "menuName": "登录日志",
                "url": "/log/login/index",
                "children": []
            },
            {
                "menuId": 19,
                "parentId": 11,
                "menuName": "操作日志",
                "url": "/log/sys/index",
                "children": []
            },
            {
                "menuId": 20,
                "parentId": 11,
                "menuName": "在线用户",
                "url": "/online/index",
                "children": []
            },
            {
                "menuId": 29,
                "parentId": 11,
                "menuName": "系统管理",
                "url": "/system/index",
                "children": []
            }
        ]
    },
    {
        "menuId": 30,
        "parentId": 0,
        "menuName": "账号关联",
        "url": "/oauth2/index",
        "children": []
    }
]
```

看完了效果, 我们来讲解下 `resultMap` 中的定义, 主要是 `collection` 语句中的内容:

```xml
<collection property="children" ofType="Menu" select="selectTree" column="{parent_id = menu_id}"/>
```

* `property="children"` 对应的是实体类中的 children 字段.
* `ofType="Menu"` 对应 children 中泛型的的类型.
* `select="selectTree"` 指定了 SELECT 语句的 id.
* `column="{parent_id = menu_id}"` 参数的表达式, 向子语句中传递参数.


这个collection整体的含义可以这样理解:

通过 `selectTree` 这个 `SELECT` 语句来获取当前菜单中的 `children` 属性结果, 在查询子菜单的 SELECT 语句中, 需要传递一个 `parent_id` 参数, 这个参数的值就是当前菜单中的 id.


本章实例代码: https://github.com/zhaojun1998/mybatis-recursive-demo