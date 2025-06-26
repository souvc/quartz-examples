示例 2 - 简单触发器演示
========================

概述:
====
本示例将演示如何使用 Quartz 的简单触发器（Simple Triggers）。

功能特性:
========
- 演示多种简单触发器的使用方式
- 展示不同的重复执行策略
- 演示任务的调度、重新调度和手动触发
- 展示调度器的生命周期管理

核心概念:
========
1. **SimpleTrigger（简单触发器）**
   - 用于在特定时间执行任务
   - 支持重复执行指定次数
   - 支持按固定间隔重复执行
   - 支持无限重复执行

2. **调度策略类型**
   - 单次执行：在指定时间执行一次
   - 有限重复：执行指定次数后停止
   - 无限重复：按固定间隔持续执行
   - 延迟执行：在未来某个时间点开始执行

文件说明:
========
- **SimpleTriggerExample.java**: 主要演示类，展示各种简单触发器的使用方法
- **SimpleJob.java**: 简单的任务实现类，用于演示任务执行
- **simple_triggers_readme.txt**: 本说明文档

运行步骤:
========
1. 编译项目：`mvn compile`
2. 运行示例：`mvn exec:java -Dexec.mainClass="org.quartz.examples.example2.SimpleTriggerExample"`
3. 观察控制台输出，查看不同触发器的执行情况

代码执行流程:
============
1. **初始化阶段**
   - 创建调度器工厂
   - 获取调度器实例
   - 设置起始时间

2. **任务调度阶段**
   - job1 & job2: 单次执行演示
   - job3: 重复执行11次（每10秒一次）
   - job3（第二个触发器）: 同一任务的不同触发器
   - job4: 重复执行6次
   - job5: 延迟5分钟后执行
   - job6: 无限重复执行（每40秒一次）
   - job7: 调度器启动后添加的任务
   - job8: 手动触发的任务

3. **运行阶段**
   - 启动调度器
   - 观察任务执行
   - 演示重新调度
   - 等待执行完成

4. **清理阶段**
   - 关闭调度器
   - 显示执行统计信息

关键代码说明:
============

**创建简单触发器的几种方式：**

1. 单次执行触发器：
```java
SimpleTrigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(startTime)
    .build();
```

2. 重复执行触发器：
```java
Trigger trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
        .withIntervalInSeconds(10)
        .withRepeatCount(10))
    .build();
```

3. 无限重复触发器：
```java
Trigger trigger = newTrigger()
    .withIdentity("trigger6", "group1")
    .startAt(startTime)
    .withSchedule(simpleSchedule()
        .withIntervalInSeconds(40)
        .repeatForever())
    .build();
```

4. 延迟执行触发器：
```java
SimpleTrigger trigger = newTrigger()
    .withIdentity("trigger5", "group1")
    .startAt(futureDate(5, IntervalUnit.MINUTE))
    .build();
```

注意事项:
========
- 调度器必须调用 start() 方法后才会真正执行任务
- 可以在调度器启动前或启动后添加任务
- 同一个任务可以被多个触发器调度
- 任务可以手动触发执行
- 触发器可以重新调度
- 记得在程序结束时关闭调度器

扩展建议:
========
1. 尝试修改重复次数和间隔时间
2. 添加更多的任务和触发器
3. 实验不同的起始时间设置
4. 尝试在运行时动态添加和删除任务
5. 观察调度器的元数据信息

运行方法:
========
```bash
# 编译项目
mvn compile

# 运行示例
mvn exec:java -Dexec.mainClass="org.quartz.examples.example2.SimpleTriggerExample"

# 或者直接运行主类
java -cp target/classes org.quartz.examples.example2.SimpleTriggerExample
```
