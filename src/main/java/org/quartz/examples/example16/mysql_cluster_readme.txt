===============================================================================
                    Quartz MySQL 集群调度示例 - 使用指南
===============================================================================

本示例演示如何使用 MySQL 数据库作为 JobStore 来实现 Quartz 集群功能。
通过多个调度器实例连接同一个 MySQL 数据库，实现任务的分布式执行、
故障转移和负载均衡。

===============================================================================
1. 环境准备
===============================================================================

1.1 MySQL 数据库准备
--------------------
- 安装并启动 MySQL 数据库服务器（推荐版本 5.7 或更高）
- 确保 MySQL 服务器可以接受外部连接
- 记录数据库服务器的地址、端口、用户名和密码

1.2 创建数据库和表
------------------
方法一：使用提供的脚本
  1. 打开 MySQL 命令行客户端或图形化工具（如 MySQL Workbench）
  2. 执行 mysql_schema.sql 脚本：
     mysql -u root -p < mysql_schema.sql
  3. 脚本会自动创建 quartz_db 数据库和所有必需的表

方法二：手动创建
  1. 创建数据库：CREATE DATABASE quartz_db;
  2. 下载 Quartz 官方的 MySQL 建表脚本
  3. 在 quartz_db 数据库中执行建表脚本

1.3 配置数据库连接
------------------
编辑配置文件中的数据库连接参数：
- mysql_instance1.properties
- mysql_instance2.properties

需要修改的参数：
- org.quartz.dataSource.myDS.URL（数据库连接地址）
- org.quartz.dataSource.myDS.user（数据库用户名）
- org.quartz.dataSource.myDS.password（数据库密码）

1.4 添加 MySQL JDBC 驱动
------------------------
确保项目的 classpath 中包含 MySQL JDBC 驱动：
- mysql-connector-java-8.0.x.jar 或更高版本
- 可以通过 Maven 依赖自动下载

===============================================================================
2. 运行集群示例
===============================================================================

2.1 通过 IDE 运行（推荐）
------------------------
实例1（主实例）：
  1. 在 IDE 中打开 MySQLClusterExample.java
  2. 配置运行参数：
     - VM 参数：-Dorg.quartz.properties=mysql_instance1.properties
     - 程序参数：clearJobs（首次运行时添加，清理数据库）
  3. 运行 main 方法

实例2（从实例）：
  1. 在另一个 IDE 窗口或进程中打开 MySQLClusterExample.java
  2. 配置运行参数：
     - VM 参数：-Dorg.quartz.properties=mysql_instance2.properties
     - 程序参数：dontScheduleJobs（不调度新任务，只处理现有任务）
  3. 运行 main 方法

2.2 通过命令行运行
-----------------
准备步骤：
  1. 编译项目：mvn clean compile
  2. 获取依赖：mvn dependency:copy-dependencies -DoutputDirectory=target/lib

运行实例1：
  java -cp "target/classes;target/lib/*" \
       -Dorg.quartz.properties=mysql_instance1.properties \
       org.quartz.examples.example16.MySQLClusterExample clearJobs

运行实例2：
  java -cp "target/classes;target/lib/*" \
       -Dorg.quartz.properties=mysql_instance2.properties \
       org.quartz.examples.example16.MySQLClusterExample dontScheduleJobs

注意：Windows 系统使用分号(;)分隔 classpath，Linux/Unix 系统使用冒号(:)

===============================================================================
3. 集群功能验证
===============================================================================

3.1 启动验证步骤
---------------
1. 首先启动实例1（使用 clearJobs 参数清理数据库）
2. 观察日志，确认：
   - 数据库连接成功
   - 任务调度成功
   - 调度器状态正常
3. 启动实例2（使用 dontScheduleJobs 参数）
4. 观察日志，确认：
   - 第二个实例成功连接数据库
   - 检测到集群环境
   - 开始处理现有任务

3.2 集群功能验证方法
------------------
任务分布验证：
- 观察日志中的 "调度器实例" 信息
- 确认任务在不同实例间分布执行
- 检查数据库 QRTZ_FIRED_TRIGGERS 表中的 INSTANCE_NAME 字段

故障转移验证：
- 在长时间任务执行过程中关闭其中一个实例
- 观察其他实例是否接管未完成的任务
- 检查 QRTZ_SCHEDULER_STATE 表中实例的状态变化

负载均衡验证：
- 启动多个实例
- 观察任务在各实例间的分布情况
- 通过日志统计各实例执行的任务数量

状态持久化验证：
- 观察 MySQLStatefulJob 的状态信息
- 确认状态在实例间迁移时保持一致
- 检查数据库中 JOB_DATA 字段的更新

3.3 数据库监控
-------------
查看集群状态：
  SELECT * FROM QRTZ_SCHEDULER_STATE;

查看活跃任务：
  SELECT * FROM QRTZ_FIRED_TRIGGERS;

查看任务详情：
  SELECT * FROM QRTZ_JOB_DETAILS;

查看触发器状态：
  SELECT TRIGGER_NAME, TRIGGER_STATE, NEXT_FIRE_TIME 
  FROM QRTZ_TRIGGERS;

===============================================================================
4. 示例任务说明
===============================================================================

4.1 MySQLTestJob
---------------
- 基本的测试任务
- 执行时间：1-3秒
- 触发间隔：每10秒
- 功能：演示基本的集群任务执行

4.2 MySQLStatefulJob
-------------------
- 有状态的任务
- 执行时间：2-5秒
- 触发间隔：每15秒
- 功能：演示状态持久化和并发控制
- 特性：
  * 维护执行计数器
  * 记录执行历史
  * 禁止并发执行
  * 状态自动持久化

4.3 MySQLLongRunningJob
----------------------
- 长时间运行任务
- 执行时间：10-20秒
- 触发间隔：每30秒
- 功能：演示故障转移机制
- 特性：
  * 定期输出进度信息
  * 支持中断和恢复
  * 便于观察故障转移

===============================================================================
5. 配置参数说明
===============================================================================

5.1 关键集群配置
---------------
org.quartz.scheduler.instanceName: MySQLClusterScheduler
  - 集群中所有实例必须使用相同的名称

org.quartz.scheduler.instanceId: AUTO
  - 使用 AUTO 自动生成唯一的实例ID

org.quartz.jobStore.isClustered: true
  - 启用集群模式的关键配置

org.quartz.jobStore.clusterCheckinInterval: 20000
  - 集群检查间隔（毫秒），实例间心跳检测频率

5.2 数据库配置
-------------
org.quartz.dataSource.myDS.URL
  - MySQL 连接字符串
  - 包含时区、SSL等参数

org.quartz.dataSource.myDS.maxConnections: 10
  - 连接池最大连接数
  - 根据并发需求调整

5.3 性能调优参数
---------------
org.quartz.threadPool.threadCount: 10
  - 线程池大小，控制并发执行的任务数

org.quartz.jobStore.misfireThreshold: 60000
  - 错过触发阈值，超时任务的处理策略

===============================================================================
6. 故障排除
===============================================================================

6.1 常见问题
-----------
问题：无法连接数据库
解决：
- 检查 MySQL 服务是否启动
- 验证连接参数（地址、端口、用户名、密码）
- 确认防火墙设置
- 检查 MySQL 用户权限

问题：集群实例无法发现彼此
解决：
- 确认所有实例使用相同的 instanceName
- 检查数据库连接是否正常
- 验证 QRTZ_SCHEDULER_STATE 表中的记录
- 确认系统时间同步

问题：任务重复执行
解决：
- 检查 instanceId 是否唯一
- 验证集群配置是否正确
- 查看 QRTZ_LOCKS 表是否正常

6.2 日志分析
-----------
正常启动日志关键信息：
- "数据库连接成功"
- "集群模式已启用"
- "调度器已启动"
- "任务已调度"

集群检测日志：
- "检测到任务实例迁移"
- "这演示了集群环境下的任务故障转移功能"

6.3 数据库检查
-------------
检查实例状态：
  SELECT INSTANCE_NAME, LAST_CHECKIN_TIME, CHECKIN_INTERVAL 
  FROM QRTZ_SCHEDULER_STATE;

检查锁状态：
  SELECT * FROM QRTZ_LOCKS;

检查任务执行历史：
  SELECT JOB_NAME, INSTANCE_NAME, FIRED_TIME, STATE 
  FROM QRTZ_FIRED_TRIGGERS 
  ORDER BY FIRED_TIME DESC;

===============================================================================
7. 扩展和定制
===============================================================================

7.1 添加新任务
-------------
1. 创建实现 Job 接口的任务类
2. 在 MySQLClusterExample 中添加任务调度代码
3. 根据需要配置任务属性（恢复、并发控制等）

7.2 集群监控
-----------
- 可以开发 Web 界面监控集群状态
- 通过 JMX 暴露调度器指标
- 集成监控系统（如 Prometheus）

7.3 高可用部署
-------------
- 部署多个调度器实例到不同服务器
- 使用 MySQL 主从复制提高数据库可用性
- 配置负载均衡器分发请求

===============================================================================
8. 最佳实践
===============================================================================

8.1 生产环境建议
---------------
- 使用专用的数据库用户和密码
- 启用 MySQL 的 SSL 连接
- 定期备份 Quartz 数据库
- 监控集群实例的健康状态
- 设置合适的日志级别

8.2 性能优化
-----------
- 根据任务负载调整线程池大小
- 优化数据库连接池配置
- 定期清理历史数据
- 使用合适的触发器类型

8.3 安全考虑
-----------
- 限制数据库访问权限
- 使用强密码
- 定期更新 JDBC 驱动
- 监控异常访问

===============================================================================
9. 参考资源
===============================================================================

- Quartz 官方文档：http://www.quartz-scheduler.org/documentation/
- MySQL 官方文档：https://dev.mysql.com/doc/
- Quartz 集群配置指南：http://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/
- 示例源码：本目录下的所有 .java 文件

===============================================================================

如有问题，请检查日志输出并参考故障排除部分。
祝您使用愉快！

===============================================================================