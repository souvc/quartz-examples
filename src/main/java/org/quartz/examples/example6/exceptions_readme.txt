Example 6
=========

概述:
=========

本示例演示了 Quartz 中任务异常处理机制的工作原理。

功能特性:
=========

1. **异常处理策略演示**
   - 展示两种不同的异常处理方式
   - 演示异常后的重新调度机制
   - 演示异常后的触发器取消机制

2. **任务异常类型**
   - BadJob1: 抛出异常后立即重新触发
   - BadJob2: 抛出异常后取消所有相关触发器

核心概念:
=========

1. **JobExecutionException**
   - Quartz 专用的任务执行异常类
   - 提供异常处理的控制选项
   - 支持设置重新调度策略

2. **异常处理选项**
   - setRefireImmediately(true): 立即重新触发任务
   - setUnscheduleAllTriggers(true): 取消所有相关触发器
   - 可以修复数据后重新执行

文件说明:
=========

1. **BadJob1.java**
   - 演示除零异常的处理
   - 修复数据后立即重新执行
   - 使用 @PersistJobDataAfterExecution 持久化数据

2. **BadJob2.java**
   - 演示不可恢复的异常处理
   - 异常后取消所有触发器
   - 防止任务继续执行

3. **JobExceptionExample.java**
   - 主示例类，演示异常处理机制
   - 创建两个不同的异常处理任务
   - 观察不同异常策略的效果

运行步骤:
=========

1. 编译并运行 JobExceptionExample
2. 观察 BadJob1 的异常和重新执行
3. 观察 BadJob2 的异常和触发器取消
4. 查看日志输出了解异常处理过程

代码执行流程:
=============

1. **初始化阶段**
   - 创建调度器工厂和调度器实例
   - 配置日志记录

2. **任务调度阶段**
   - 创建 BadJob1，设置除数为0（触发异常）
   - 创建 BadJob2，包含除零错误
   - 设置不同的触发间隔

3. **执行阶段**
   - 启动调度器
   - BadJob1 异常后立即重新执行
   - BadJob2 异常后停止执行

4. **关闭阶段**
   - 等待30秒观察行为
   - 关闭调度器
   - 输出执行统计信息

关键代码说明:
=============

1. **异常捕获和处理**
   ```java
   try {
       // 可能抛出异常的代码
   } catch (Exception e) {
       JobExecutionException e2 = new JobExecutionException(e);
       e2.setRefireImmediately(true); // 或 setUnscheduleAllTriggers(true)
       throw e2;
   }
   ```

2. **数据修复机制**
   - BadJob1 在异常处理中修复除数
   - 确保重新执行时不会再次失败

预期行为:
=========

1. **BadJob1 行为**
   - 第一次执行：除零异常，立即重新执行
   - 第二次执行：成功完成（除数已修复为1）
   - 后续执行：正常执行

2. **BadJob2 行为**
   - 第一次执行：除零异常
   - 取消所有触发器，不再执行

注意事项:
=========

1. **异常处理策略选择**
   - 可恢复错误使用 setRefireImmediately
   - 不可恢复错误使用 setUnscheduleAllTriggers

2. **数据持久化**
   - 使用 @PersistJobDataAfterExecution 确保数据修复生效
   - 异常处理中的数据修改会被保存

3. **并发控制**
   - @DisallowConcurrentExecution 防止并发执行
   - 确保异常处理的原子性

扩展建议:
=========

1. **添加重试机制**
   - 实现指数退避重试
   - 设置最大重试次数

2. **异常分类处理**
   - 根据异常类型选择不同策略
   - 实现异常恢复逻辑

3. **监控和告警**
   - 添加异常监控
   - 实现异常通知机制

运行方法:
=========

```bash
# 编译
javac -cp "lib/*" src/main/java/org/quartz/examples/example6/*.java

# 运行
java -cp "lib/*:src/main/java" org.quartz.examples.example6.JobExceptionExample
```

