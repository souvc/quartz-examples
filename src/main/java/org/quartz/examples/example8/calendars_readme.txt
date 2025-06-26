示例8 - Quartz 日历功能演示
=============================

概述：
=====
本示例演示如何在 Quartz 中使用日历（Calendar）功能来排除特定的时间段，防止任务在这些时间段内执行。

功能特性：
=========
1. **年度日历（AnnualCalendar）**：定义年度重复的排除日期
2. **节假日排除**：设置特定日期为节假日，任务不会在这些日期执行
3. **触发器日历关联**：将日历与触发器关联，自动跳过排除的日期
4. **智能调度**：当触发时间遇到排除日期时，自动推迟到下一个可用时间

核心概念：
=========
- **Calendar（日历）**：Quartz 中用于定义时间排除规则的组件
- **AnnualCalendar（年度日历）**：按年重复的日历，适用于固定节假日
- **modifiedByCalendar()**：触发器方法，用于关联日历进行时间排除
- **setDayExcluded()**：设置特定日期为排除日期

文件说明：
=========
- **CalendarExample.java**：主示例类，演示日历的创建、配置和使用
- **SimpleJob.java**：简单的任务实现，用于测试日历功能
- **calendars_readme.txt**：本说明文档

运行步骤：
=========
1. 创建年度日历并设置节假日（7月4日、10月31日、12月25日）
2. 将日历添加到调度器中
3. 创建任务和触发器，触发器关联日历
4. 调度任务，观察日历排除效果
5. 启动调度器并等待执行结果

代码执行流程：
=============
1. **初始化调度器**：创建标准调度器工厂和调度器实例
2. **创建年度日历**：实例化 AnnualCalendar 对象
3. **设置排除日期**：
   - 7月4日（美国独立日）
   - 10月31日（万圣节）
   - 12月25日（圣诞节）
4. **注册日历**：将日历添加到调度器，名称为 "holidays"
5. **创建任务**：使用 SimpleJob 类创建任务实例
6. **创建触发器**：
   - 开始时间：10月31日上午10点
   - 重复间隔：每小时执行一次
   - 关联日历："holidays"
7. **调度任务**：将任务和触发器提交给调度器
8. **启动调度器**：开始任务调度
9. **等待执行**：等待30秒观察执行情况
10. **关闭调度器**：优雅关闭并输出统计信息

关键代码说明：
=============
```java
// 创建年度日历
AnnualCalendar holidays = new AnnualCalendar();

// 设置排除日期
Calendar fourthOfJuly = new GregorianCalendar(2005, 6, 4);
holidays.setDayExcluded(fourthOfJuly, true);

// 将日历添加到调度器
sched.addCalendar("holidays", holidays, false, false);

// 创建关联日历的触发器
SimpleTrigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startAt(runDate)
    .withSchedule(simpleSchedule().withIntervalInHours(1).repeatForever())
    .modifiedByCalendar("holidays")  // 关联日历
    .build();
```

预期行为：
=========
- 任务原本应该在10月31日（万圣节）上午10点开始执行
- 由于10月31日被设置为排除日期，任务会自动推迟到11月1日执行
- 后续每小时执行一次，但会跳过所有设置的节假日

注意事项：
=========
1. **月份索引**：Java Calendar 中月份从0开始，6表示7月，9表示10月，11表示12月
2. **日历持久化**：addCalendar() 方法的参数控制是否替换现有日历和是否更新触发器
3. **时区考虑**：日历排除基于系统默认时区
4. **性能影响**：大量排除日期可能影响调度性能

扩展建议：
=========
1. **其他日历类型**：
   - WeeklyCalendar：按周排除
   - MonthlyCalendar：按月排除
   - DailyCalendar：按日排除特定时间段
   - CronCalendar：使用 Cron 表达式排除
2. **日历组合**：使用 BaseCalendar.setBaseCalendar() 组合多个日历
3. **动态更新**：运行时动态添加或移除排除日期
4. **数据库存储**：将日历配置存储在数据库中便于管理

运行方法：
=========
```bash
# 编译项目
mvn compile

# 运行示例
mvn exec:java -Dexec.mainClass="org.quartz.examples.example8.CalendarExample"

# 或者直接运行 main 方法
java org.quartz.examples.example8.CalendarExample
```

