示例 9 - 任务监听器功能演示
=============================

概述：
=====
本示例演示了如何在 Quartz 中使用任务监听器（Job Listeners）来实现
一个任务完成后自动触发另一个任务的功能。

功能特性：
=========
1. 任务监听器机制：实现 JobListener 接口，监听任务的生命周期事件
2. 任务链式执行：通过监听器实现任务的顺序执行和依赖关系
3. 动态任务调度：在监听器中动态创建和调度新任务
4. 事件驱动架构：基于事件驱动的任务执行模式

核心概念：
=========
- JobListener：任务监听器接口，提供任务执行前、执行后、被否决等事件回调
- 监听器注册：通过 ListenerManager 注册监听器并指定监听范围
- 事件回调：jobToBeExecuted、jobWasExecuted、jobExecutionVetoed 三个核心方法
- 动态调度：在监听器回调中创建新任务并立即调度执行

文件说明：
=========
1. ListenerExample.java - 主程序类，演示监听器的注册和使用
2. Job1Listener.java - 任务监听器实现，监听 Job1 并触发 Job2
3. SimpleJob1.java - 第一个任务，被监听器监听
4. SimpleJob2.java - 第二个任务，由监听器动态创建和调度
5. listeners_readme.txt - 本说明文档

运行步骤：
=========
1. 编译项目：mvn compile
2. 运行示例：mvn exec:java -Dexec.mainClass="org.quartz.examples.example9.ListenerExample"
3. 观察输出：查看任务执行顺序和监听器事件

代码执行流程：
=============
1. 初始化调度器和日志系统
2. 创建 SimpleJob1 任务和对应触发器
3. 创建并注册 Job1Listener 监听器
4. 将监听器绑定到 SimpleJob1
5. 调度 SimpleJob1 并启动调度器
6. SimpleJob1 执行完成后触发监听器
7. 监听器在 jobWasExecuted 方法中创建 SimpleJob2
8. SimpleJob2 被立即调度执行
9. 等待 30 秒观察任务执行情况
10. 关闭调度器并输出执行统计

关键代码说明：
=============
- JobListener 接口：定义了任务生命周期的三个关键事件
- KeyMatcher.keyEquals()：指定监听器只监听特定的任务
- addJobListener()：注册监听器到调度器
- inContext.getScheduler().scheduleJob()：在监听器中动态调度新任务

预期行为：
=========
1. SimpleJob1 首先执行并输出执行信息
2. Job1Listener 监听到 SimpleJob1 完成事件
3. 监听器自动创建并调度 SimpleJob2
4. SimpleJob2 立即执行并输出执行信息
5. 整个过程展示了任务间的依赖和链式执行

注意事项：
=========
1. 监听器中的异常处理很重要，避免影响主任务执行
2. 动态创建的任务会立即执行，注意资源和性能影响
3. 监听器的注册范围要准确，避免监听不必要的任务
4. 在生产环境中要考虑监听器的性能开销

扩展建议：
=========
1. 实现更复杂的任务依赖关系和条件触发
2. 添加任务执行状态的持久化存储
3. 实现任务执行失败时的重试和补偿机制
4. 集成外部系统进行任务状态通知

运行方法：
=========
在项目根目录执行：
mvn compile exec:java -Dexec.mainClass="org.quartz.examples.example9.ListenerExample"

