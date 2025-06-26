示例 5
=========

概述:
=========
本示例演示了任务失火（Misfire）机制的工作原理。

功能特性:
=========
1. 任务失火机制 - 当任务执行时间超过触发间隔时的处理策略
2. 失火处理指令 - 不同的失火处理策略对比
3. 有状态任务 - 使用 @PersistJobDataAfterExecution 和 @DisallowConcurrentExecution
4. 长时间运行任务 - 模拟任务执行时间超过触发间隔的场景
5. 并发控制 - 防止同一任务的多个实例同时执行

核心概念:
=========
1. Misfire（失火）- 当触发器应该触发但由于某种原因未能及时触发时发生
2. Misfire Threshold（失火阈值）- 调度器判断失火的时间阈值
3. Misfire Instructions（失火指令）- 定义失火发生时的处理策略
4. Smart Policy（智能策略）- 默认的失火处理策略
5. Stateful Job（有状态任务）- 维护执行状态的任务

失火处理策略:
=============
1. MISFIRE_INSTRUCTION_SMART_POLICY（默认）
   - 智能策略，跳过已错过的触发时间，在下一个预定时间执行
   
2. MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
   - 立即重新调度，保持现有的重复次数
   
3. MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
   - 在下一个预定时间重新调度，保持现有的重复次数
   
4. MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
   - 在下一个预定时间重新调度，调整重复次数

文件说明:
=========
- StatefulDumbJob.java: 有状态的测试任务，模拟长时间执行
- MisfireExample.java: 主程序，演示不同失火处理策略
- misfires_readme.txt: 本说明文档

运行步骤:
=========
1. 编译项目: mvn compile
2. 运行示例: mvn exec:java -Dexec.mainClass="org.quartz.examples.example5.MisfireExample"
3. 观察输出，查看不同失火处理策略的效果

代码执行流程:
=============
1. 初始化调度器
2. 创建两个相同的任务，但使用不同的失火处理策略
3. 设置触发器每3秒执行一次，但任务需要10秒完成
4. 启动调度器，观察失火现象
5. 等待10分钟观察不同策略的处理效果
6. 关闭调度器

关键代码说明:
=============
1. 任务执行延迟设置:
   .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L)
   
2. 默认失火策略（智能策略）:
   .withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever())
   
3. 立即重新调度失火策略:
   .withMisfireHandlingInstructionNowWithExistingCount()
   
4. 有状态任务注解:
   @PersistJobDataAfterExecution
   @DisallowConcurrentExecution

预期行为:
=========
1. 第一个任务（默认策略）:
   - 失火后跳过错过的执行时间
   - 在下一个预定时间执行
   - 执行间隔相对规律
   
2. 第二个任务（立即重新调度）:
   - 失火后立即重新执行
   - 可能导致连续执行
   - 执行间隔不规律

注意事项:
=========
1. 失火阈值默认为60秒，可通过配置调整
2. 任务执行时间超过触发间隔时容易发生失火
3. @DisallowConcurrentExecution 防止同一任务并发执行
4. 不同的失火策略适用于不同的业务场景
5. 长时间运行的任务应该考虑失火处理策略

扩展建议:
=========
1. 尝试不同的失火处理指令
2. 调整失火阈值设置
3. 测试不同的任务执行时间和触发间隔组合
4. 添加失火事件监听器
5. 实现自定义的失火处理逻辑
6. 使用数据库持久化调度器状态

运行方法:
=========
在项目根目录执行:
mvn compile exec:java -Dexec.mainClass="org.quartz.examples.example5.MisfireExample"


