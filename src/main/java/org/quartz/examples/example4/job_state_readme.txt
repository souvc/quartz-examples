Example 4
=========

概述:
=========
本示例演示了如何维护任务状态以及如何向任务传递参数。

功能特性:
=========
1. 任务状态维护 - 通过 JobDataMap 在任务执行之间保持状态
2. 参数传递 - 向任务传递初始化参数
3. 状态持久化 - 使用 @PersistJobDataAfterExecution 注解
4. 并发控制 - 使用 @DisallowConcurrentExecution 注解
5. 多任务实例 - 演示同一任务类的不同实例

核心概念:
=========
1. JobDataMap - 任务数据映射，用于存储和传递参数
2. @PersistJobDataAfterExecution - 任务执行后持久化数据
3. @DisallowConcurrentExecution - 禁止并发执行
4. 任务状态 vs 实例变量 - 正确的状态维护方式

文件说明:
=========
- ColorJob.java: 演示状态维护和参数接收的任务类
- JobStateExample.java: 主程序，创建和调度任务
- job_state_readme.txt: 本说明文档

运行步骤:
=========
1. 编译项目: mvn compile
2. 运行示例: mvn exec:java -Dexec.mainClass="org.quartz.examples.example4.JobStateExample"
3. 观察输出，查看任务状态的变化

代码执行流程:
=============
1. 初始化调度器
2. 创建两个 ColorJob 任务实例，设置不同的参数
3. 为每个任务设置简单触发器（每10秒执行一次，共5次）
4. 启动调度器
5. 等待60秒观察任务执行
6. 关闭调度器

关键代码说明:
=============
1. 任务参数设置:
   job.getJobDataMap().put(ColorJob.FAVORITE_COLOR, "Green");
   
2. 状态维护:
   @PersistJobDataAfterExecution - 确保任务数据在执行后被保存
   
3. 并发控制:
   @DisallowConcurrentExecution - 防止同一任务的多个实例同时执行
   
4. 状态读取和更新:
   JobDataMap data = context.getJobDetail().getJobDataMap();
   int count = data.getInt(EXECUTION_COUNT);
   count++;
   data.put(EXECUTION_COUNT, count);

注意事项:
=========
1. 实例变量不能用于维护状态，因为 Quartz 每次执行都会重新实例化任务类
2. 使用 JobDataMap 是维护任务状态的正确方式
3. @PersistJobDataAfterExecution 确保状态在任务执行后被保存
4. @DisallowConcurrentExecution 防止并发执行可能导致的状态不一致
5. 任务类必须有无参构造函数

扩展建议:
=========
1. 尝试移除 @DisallowConcurrentExecution 注解，观察并发执行的影响
2. 添加更多的参数类型（Date、Boolean、自定义对象等）
3. 实现任务间的状态共享
4. 添加异常处理和状态验证
5. 使用数据库持久化任务状态

运行方法:
=========
在项目根目录执行:
mvn compile exec:java -Dexec.mainClass="org.quartz.examples.example4.JobStateExample"

