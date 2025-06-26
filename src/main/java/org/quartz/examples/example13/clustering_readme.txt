示例 13 - Quartz 集群调度演示
===============================

概述:
=====

本示例演示了如何在集群环境中使用 Quartz 来支持故障转移。在集群环境中，
您可以运行多个 Quartz 实例，它们使用共享的调度器。这个共享调度器持久化
在数据库中，使得所有实例都能共享相同的调度数据。

集群架构说明:
- 多个 Quartz 实例共享同一个数据库
- 任务和触发器信息存储在数据库中
- 实例间通过数据库进行协调和故障检测
- 支持自动故障转移和任务恢复

注意: 本示例在不同计算机上运行客户端和服务器时效果最佳。
      当然，您也可以在同一台机器上运行服务器和客户端！

运行示例:
=========

1. 配置 instance1.properties 文件和 instance2.properties 文件
   (详细信息请参见下面的"配置"部分)。

2. 运行集群示例的方法：
   由于本项目没有提供 bat 脚本文件，您需要手动运行 Java 程序：
   
   方法一：使用 IDE（推荐）
   - 在 IDE 中直接运行 ClusterExample.java 的 main 方法
   - 可以通过修改运行配置的 VM 参数来指定不同的配置文件：
     实例1：-Dorg.quartz.properties=instance1.properties
     实例2：-Dorg.quartz.properties=instance2.properties
   
   方法二：使用命令行
    - 首先编译项目：mvn compile
    - 获取依赖：mvn dependency:copy-dependencies -DoutputDirectory=target/lib
    - 运行实例1：java -cp "target/classes;target/lib/*" -Dorg.quartz.properties=instance1.properties org.quartz.examples.example13.ClusterExample
    - 运行实例2：java -cp "target/classes;target/lib/*" -Dorg.quartz.properties=instance2.properties org.quartz.examples.example13.ClusterExample
   
   (注意：这些实例可以在同一台机器上，也可以在不同机器上！)
   
3. 验证集群功能的方法：
    
    启动验证步骤：
    - 先启动第一个实例，观察控制台输出
    - 等待第一个实例完全启动后，再启动第二个实例
    - 观察两个实例的日志输出，确认它们都连接到了同一个数据库
    
    集群功能验证：
    - 任务分布：观察任务是否在两个实例之间分布执行
    - 故障转移：关闭其中一个实例，观察另一个实例是否接管了所有任务
    - 负载均衡：重新启动被关闭的实例，观察任务是否重新分布
    - 数据库状态：可以查询数据库中的 QRTZ_SCHEDULER_STATE 表，查看集群节点状态
    
4. UNIX/Linux 用户运行方法：
     由于本项目没有提供 sh 脚本文件，请参考上述方法二的命令行运行方式：
     - 编译项目：mvn compile
     - 获取依赖：mvn dependency:copy-dependencies -DoutputDirectory=target/lib
     - 运行实例1：java -cp "target/classes:target/lib/*" -Dorg.quartz.properties=instance1.properties org.quartz.examples.example13.ClusterExample
     - 运行实例2：java -cp "target/classes:target/lib/*" -Dorg.quartz.properties=instance2.properties org.quartz.examples.example13.ClusterExample
     (注意：Linux/Unix 使用冒号(:)分隔 classpath，Windows 使用分号(;))
     
 5. 观察输出！
 
 运行提示:
- 如果您同时拥有 Windows 和 UNIX/Linux 机器，可以尝试在不同平台上运行示例！
- 您可以在运行 ClusterExample 时传递命令行参数：
  "clearJobs" - 删除所有现有的任务和触发器
  "dontScheduleJobs" - 阻止新任务被调度（仅用于观察现有任务的恢复）
  例如：java -cp ... org.quartz.examples.example13.ClusterExample clearJobs

集群测试建议:
- 在任务运行过程中尝试终止其中一个集群实例
- 观察剩余实例如何恢复正在进行的任务
- 故障检测可能需要15秒左右的时间（默认设置）


配置:
=====

1. 日志配置 (可选)
   您可以指定 log4j.properties 文件来控制日志输出

2. 数据库表安装
   本示例使用数据库在集群环境中维护调度信息。您需要首先安装 Quartz 
   数据库表。Quartz 发行版中包含了针对多种流行数据库平台的 SQL 表创建脚本。
   
   数据库表的作用:
   - QRTZ_TRIGGERS: 存储触发器信息
   - QRTZ_JOB_DETAILS: 存储任务详细信息
   - QRTZ_FIRED_TRIGGERS: 存储已触发的触发器
   - QRTZ_LOCKS: 用于集群锁定机制
   - QRTZ_SCHEDULER_STATE: 存储调度器状态信息

3. JDBC 驱动程序
   您需要为数据库准备 JDBC 驱动程序。本示例使用 PostgreSQL 进行演示。
   您可以从 http://jdbc.postgresql.org 下载 PostgreSQL JDBC 驱动程序
   只需将 jar 文件放在 Quartz 发行版的 "lib" 文件夹下

4. 数据库连接配置
   安装数据库脚本后，您需要配置两个属性文件，以便 Quartz 知道如何连接到数据库。

需要设置的参数如下 (以 PostgreSQL 为例):

# JobStore 配置 - 使用数据库存储任务信息
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
# 数据库委托类 - 针对 PostgreSQL 优化的实现
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
# 是否使用 Properties 存储 JobDataMap (false=使用序列化)
org.quartz.jobStore.useProperties=false
# 数据源名称
org.quartz.jobStore.dataSource=myDS
# 数据库表前缀
org.quartz.jobStore.tablePrefix=QRTZ_
# 启用集群模式 - 这是关键配置！
org.quartz.jobStore.isClustered=true

# 数据源配置
org.quartz.dataSource.myDS.driver = org.postgresql.Driver
org.quartz.dataSource.myDS.URL = jdbc:postgresql://localhost:5432/quartz
org.quartz.dataSource.myDS.user = quartz
org.quartz.dataSource.myDS.password = quartz
org.quartz.dataSource.myDS.maxConnections = 5
org.quartz.dataSource.myDS.validationQuery = select 1

重要配置说明:
- isClustered=true: 启用集群模式，多个实例可以共享任务
- 所有集群实例必须使用相同的数据库和表前缀
- 每个实例必须有唯一的 instanceId (在 instance1.properties 和 instance2.properties 中设置)
- 确保所有集群节点的系统时间同步

支持的数据库:
- PostgreSQL (推荐用于生产环境)
- MySQL/MariaDB
- Oracle
- SQL Server
- DB2
- H2 (仅用于开发测试)

应用场景:
- 高可用性任务调度
- 负载均衡的任务执行
- 故障转移和任务恢复
- 分布式系统中的定时任务管理


