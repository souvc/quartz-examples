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

package org.quartz.examples.example8;

import static org.quartz.DateBuilder.dateOf;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.examples.example2.SimpleJob;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.AnnualCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Quartz 日历功能演示示例
 *
 * 本示例演示如何使用 Quartz 中的日历（Calendar）功能来排除特定的时间段，
 * 防止任务在这些时间段内执行。主要展示以下功能：
 *
 * 功能特性：
 * 1. **年度日历创建**：使用 AnnualCalendar 创建年度重复的排除日期
 * 2. **节假日设置**：设置美国独立日、万圣节、圣诞节为排除日期
 * 3. **日历注册**：将日历添加到调度器中进行管理
 * 4. **触发器关联**：通过 modifiedByCalendar() 将触发器与日历关联
 * 5. **智能调度**：当触发时间遇到排除日期时，自动推迟到下一个可用时间
 *
 * 执行流程：
 * 1. 创建年度日历并设置三个节假日为排除日期
 * 2. 将日历注册到调度器中，名称为 "holidays"
 * 3. 创建简单任务和每小时重复的触发器
 * 4. 触发器关联日历，开始时间设为万圣节（排除日期）
 * 5. 调度任务并启动调度器
 * 6. 观察任务因日历排除而推迟执行的效果
 *
 * 关键学习点：
 * - AnnualCalendar 的使用方法
 * - setDayExcluded() 设置排除日期
 * - addCalendar() 注册日历到调度器
 * - modifiedByCalendar() 关联日历到触发器
 * - 日历排除对任务调度时间的影响
 *
 * 预期结果：
 * 任务原定在10月31日（万圣节）开始执行，但由于该日期被排除，
 * 实际执行时间会自动推迟到11月1日。
 */
public class CalendarExample {

  public void run() throws Exception {
    final Logger log = LoggerFactory.getLogger(CalendarExample.class);

    log.info("------- 初始化调度器 ----------------------");

    // 首先获取调度器的引用
    // 使用标准调度器工厂创建调度器实例
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 -----------");

    log.info("------- 开始调度任务 -------------------");

    // 创建年度日历用于管理节假日
    // AnnualCalendar 用于定义每年重复的排除日期
    AnnualCalendar holidays = new AnnualCalendar();

    // 设置美国独立日（7月4日）为排除日期
    // 注意：Java Calendar 中月份从0开始，所以6表示7月
    Calendar fourthOfJuly = new GregorianCalendar(2005, 6, 4);
    holidays.setDayExcluded(fourthOfJuly, true);

    // 设置万圣节（10月31日）为排除日期
    // 9表示10月
    Calendar halloween = new GregorianCalendar(2005, 9, 31);
    holidays.setDayExcluded(halloween, true);

    // 设置圣诞节（12月25日）为排除日期
    // 11表示12月
    Calendar christmas = new GregorianCalendar(2005, 11, 25);
    holidays.setDayExcluded(christmas, true);

    // 将节假日日历注册到调度器中
    // 参数说明："holidays" - 日历名称，holidays - 日历对象，false - 不替换现有日历，false - 不更新现有触发器
    sched.addCalendar("holidays", holidays, false, false);

    // 创建任务调度计划：每小时执行一次，开始时间设为万圣节上午10点
    // 这样设计是为了演示日历排除功能（万圣节被排除，任务会推迟执行）
    // 秒, 分, 时, 日, 月（10月31日上午10点）
    Date runDate = dateOf(0, 0, 10, 31, 10);

    // 创建任务详情，使用 SimpleJob 类
    // withIdentity() 设置任务的唯一标识：名称为 "job1"，组名为 "group1"
    JobDetail job = newJob(SimpleJob.class).withIdentity("job1", "group1").build();

    // 创建简单触发器，配置执行计划和日历关联
    SimpleTrigger trigger = newTrigger()
        .withIdentity("trigger1", "group1")  // 触发器标识
        .startAt(runDate)  // 开始时间：10月31日上午10点
        .withSchedule(simpleSchedule()
            .withIntervalInHours(1)  // 每小时执行一次
            .repeatForever())  // 无限重复
        .modifiedByCalendar("holidays")  // 关联 "holidays" 日历，自动跳过节假日
        .build();

    // 将任务和触发器提交给调度器，并获取实际的首次执行时间
    Date firstRunTime = sched.scheduleJob(job, trigger);

    // 输出首次执行时间信息
    // 重要提示：由于万圣节（10月31日）是节假日，任务不会在这一天执行，
    // 而是自动推迟到下一个可用日期（11月1日）执行！
    log.info(job.getKey() + " 将在以下时间运行: " + firstRunTime
             + " 重复次数: " + trigger.getRepeatCount()
             + " 次, 每 " + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    // 所有任务都已添加到调度器中，但只有启动调度器后任务才会开始执行
    log.info("------- 启动调度器 ----------------");
    sched.start();

    // 等待30秒观察任务执行情况
    // 注意：由于开始时间是万圣节（被排除的日期），在这30秒内不会有任务执行
    // 任务会被推迟到11月1日才开始执行
    log.info("------- 等待30秒观察执行情况... --------------");
    try {
      // 等待30秒以观察任务调度情况
      Thread.sleep(30L * 1000L);
      // 在此期间可以观察日志输出，了解日历排除的效果
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 关闭调度器
    log.info("------- 正在关闭调度器 ---------------------");
    sched.shutdown(true);  // true 表示等待当前正在执行的任务完成后再关闭
    log.info("------- 调度器关闭完成 -----------------");

    // 获取并输出调度器的执行统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务实例。");

  }

  /**
   * 程序入口方法
   *
   * 演示 Quartz 日历功能的完整流程：
   * 1. 创建年度日历并设置节假日排除
   * 2. 注册日历到调度器
   * 3. 创建关联日历的触发器
   * 4. 观察日历排除对任务调度的影响
   *
   * 运行此示例可以学习：
   * - 如何使用 AnnualCalendar 管理年度节假日
   * - 如何将日历与触发器关联
   * - 日历排除如何影响任务的实际执行时间
   *
   * @param args 命令行参数（本示例中未使用）
   * @throws Exception 如果调度器操作过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    CalendarExample example = new CalendarExample();
    example.run();
  }

}
