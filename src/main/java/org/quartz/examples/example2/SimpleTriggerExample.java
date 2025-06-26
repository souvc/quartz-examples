/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
 
package org.quartz.examples.example2;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 本示例将演示使用简单触发器（Simple Triggers）的 Quartz 调度功能的所有基础知识。
 *
 * Quartz API 的关键接口是：
 *
 * Scheduler - 与调度程序交互的主要API。
 * Job - 由希望由调度程序执行的组件实现的接口。
 * JobDetail - 用于定义作业的实例。
 * Trigger（即触发器） - 定义执行给定作业的计划的组件。
 * JobBuilder - 用于定义/构建JobDetail实例，用于定义作业的实例。
 * TriggerBuilder - 用于定义/构建触发器实例。
 *
 * Scheduler的生命期，从SchedulerFactory创建它时开始，到Scheduler调用shutdown()方法时结束；
 * Scheduler被创建后，可以增加、删除和列举Job和Trigger，以及执行其它与调度相关的操作（如暂停Trigger）。
 * 但是，Scheduler只有在调用start()方法后，才会真正地触发trigger（即执行job）。
 *
 * Quartz提供的"builder"类，可以认为是一种领域特定语言（DSL，Domain Specific Language）。
 *
 * 本示例演示了以下简单触发器的使用场景：
 * 1. 单次执行任务
 * 2. 重复执行指定次数的任务
 * 3. 无限重复执行的任务
 * 4. 延迟执行的任务
 * 5. 同一任务的多个触发器
 * 6. 手动触发任务
 * 7. 重新调度任务
 *
 * @author Bill Kratzer
 */
public class SimpleTriggerExample {

  public void run() throws Exception {
    // 获取日志记录器，用于输出示例执行过程
    Logger log = LoggerFactory.getLogger(SimpleTriggerExample.class);

    log.info("------- 初始化调度器 -------------------");

    // 首先我们必须获取调度器的引用
    // 使用标准调度器工厂创建调度器实例
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 --------");

    log.info("------- 开始调度任务 ----------------");

    // 任务可以在调用 sched.start() 之前进行调度
    // 这样可以预先配置好所有任务，然后统一启动

    // 获取一个"整齐的"未来时间点（15秒后的整秒时间）
    // 这样可以让所有任务在同一个整秒时间开始执行，便于观察
    Date startTime = DateBuilder.nextGivenSecondDate(null, 15);

    // ===== 任务1：单次执行演示 =====
    // job1 将在指定时间只执行一次
    JobDetail job = newJob(SimpleJob.class).withIdentity("job1", "group1").build();

    // 创建简单触发器，只在指定时间执行一次（默认重复次数为0）
    SimpleTrigger trigger = (SimpleTrigger) newTrigger().withIdentity("trigger1", "group1").startAt(startTime).build();

    // 将任务和触发器关联并添加到调度器中
    Date ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 任务2：另一个单次执行演示 =====
    // job2 也将在指定时间只执行一次，与job1同时执行
    job = newJob(SimpleJob.class).withIdentity("job2", "group1").build();

    trigger = (SimpleTrigger) newTrigger().withIdentity("trigger2", "group1").startAt(startTime).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 任务3：重复执行演示 =====
    // job3 将运行11次（执行1次 + 重复10次）
    // job3 每10秒重复一次
    job = newJob(SimpleJob.class).withIdentity("job3", "group1").build();

    // 创建带有重复调度的触发器
    trigger = newTrigger().withIdentity("trigger3", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(10)).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 同一任务的多个触发器演示 =====
    // 同一个任务（job3）将被另一个触发器调度
    // 这次只重复2次，间隔10秒（注意：这里使用了不同的组名group2）

    trigger = newTrigger().withIdentity("trigger3", "group2").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(2)).forJob(job).build();

    // 只添加触发器，因为任务已经存在
    ft = sched.scheduleJob(trigger);
    log.info(job.getKey() + " 将[同时]在: " + ft + " 运行，重复: " + trigger.getRepeatCount()
             + " 次，间隔 " + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 任务4：有限重复执行演示 =====
    // job4 将运行6次（执行1次 + 重复5次）
    // job4 每10秒重复一次
    job = newJob(SimpleJob.class).withIdentity("job4", "group1").build();

    trigger = newTrigger().withIdentity("trigger4", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(5)).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 任务5：延迟执行演示 =====
    // job5 将在5分钟后执行一次
    job = newJob(SimpleJob.class).withIdentity("job5", "group1").build();

    // 使用 futureDate 方法设置未来的执行时间
    trigger = (SimpleTrigger) newTrigger().withIdentity("trigger5", "group1")
        .startAt(futureDate(5, IntervalUnit.MINUTE)).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 任务6：无限重复执行演示 =====
    // job6 将无限期运行，每40秒执行一次
    job = newJob(SimpleJob.class).withIdentity("job6", "group1").build();

    // 使用 repeatForever() 创建无限重复的触发器
    trigger = newTrigger().withIdentity("trigger6", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(40).repeatForever()).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    log.info("------- 启动调度器 ----------------");

    // 所有任务都已添加到调度器中，但在调度器启动之前，
    // 没有任何任务会运行。这是 Quartz 的重要特性。
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    // ===== 动态添加任务演示 =====
    // 任务也可以在调用 start() 之后进行调度...
    // job7 将重复20次，每5分钟重复一次
    job = newJob(SimpleJob.class).withIdentity("job7", "group1").build();

    trigger = newTrigger().withIdentity("trigger7", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInMinutes(5).withRepeatCount(20)).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在: " + ft + " 运行，重复: " + trigger.getRepeatCount() + " 次，间隔 "
             + trigger.getRepeatInterval() / 1000 + " 秒");

    // ===== 手动触发任务演示 =====
    // 任务可以直接触发执行...（而不是等待触发器）
    // 注意：使用 storeDurably() 创建持久化任务，即使没有触发器也会保存
    job = newJob(SimpleJob.class).withIdentity("job8", "group1").storeDurably().build();

    // 添加任务到调度器（replace=true 表示如果存在同名任务则替换）
    sched.addJob(job, true);

    log.info("'手动' 触发 job8...");
    // 直接触发任务执行，不需要等待触发器
    sched.triggerJob(jobKey("job8", "group1"));

    log.info("------- 等待30秒观察任务执行... --------------");

    try {
      // 等待30秒以观察任务执行情况
      Thread.sleep(30L * 1000L);
      // 在此期间可以观察到各种任务的执行...
    } catch (Exception e) {
      // 忽略中断异常
    }

    // ===== 重新调度任务演示 =====
    // 任务可以重新调度...
    // 将 job7 重新调度为每5分钟执行一次，重复20次
    log.info("------- 重新调度任务... --------------------");
    trigger = newTrigger().withIdentity("trigger7", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInMinutes(5).withRepeatCount(20)).build();

    // 重新调度现有触发器
    ft = sched.rescheduleJob(trigger.getKey(), trigger);
    log.info("job7 重新调度到: " + ft + " 运行");

    log.info("------- 等待5分钟观察重新调度效果... ------------");
    try {
      // 等待5分钟以观察重新调度的任务执行
      Thread.sleep(300L * 1000L);
      // 在此期间可以观察到重新调度的任务执行...
    } catch (Exception e) {
      // 忽略中断异常
    }

    log.info("------- 关闭调度器 ---------------------");

    // 关闭调度器（waitForJobsToComplete=true 表示等待正在执行的任务完成）
    sched.shutdown(true);

    log.info("------- 调度器关闭完成 -----------------");

    // 显示刚刚运行的调度的一些统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");

  }

  /**
   * 程序入口点
   * @param args 命令行参数
   * @throws Exception 如果示例执行过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    SimpleTriggerExample example = new SimpleTriggerExample();
    example.run();

  }

}
