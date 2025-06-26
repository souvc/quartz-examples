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
 
package org.quartz.examples.example6;

import static org.quartz.DateBuilder.nextGivenSecondDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
 * 本示例演示了 Quartz 如何处理任务抛出的 JobExecutionException 异常。
 * 
 * <p>
 * 功能特性：
 * 1. 演示两种不同的异常处理策略
 * 2. 对比可恢复异常和不可恢复异常的处理方式
 * 3. 展示异常后的重新调度和触发器取消机制
 * 4. 演示任务数据的动态修复
 * </p>
 * 
 * <p>
 * 示例包含两个任务：
 * - BadJob1: 演示可恢复异常，修复数据后立即重新执行
 * - BadJob2: 演示不可恢复异常，取消所有触发器停止执行
 * </p>
 * 
 * <p>
 * 关键学习点：
 * - JobExecutionException 的使用方法
 * - setRefireImmediately() 和 setUnscheduleAllTriggers() 的区别
 * - 异常处理中的数据修复机制
 * - 有状态任务的异常处理特性
 * </p>
 * 
 * @author Bill Kratzer
 */
public class JobExceptionExample {

  public void run() throws Exception {
    // 初始化日志记录器
    Logger log = LoggerFactory.getLogger(JobExceptionExample.class);

    log.info("------- 初始化调度器 ----------------------");

    // 首先获取调度器的引用
    // 使用标准调度器工厂创建调度器实例
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 ------------");

    log.info("------- 开始调度任务 -------------------");

    // 任务可以在调度器启动前进行调度

    // 获取一个"整齐的"未来时间点（15秒后）
    Date startTime = nextGivenSecondDate(null, 15);

    // badJob1 将每10秒运行一次
    // 这个任务会抛出异常并立即重新触发
    // 设置除数为"0"以触发除零异常
    JobDetail job = newJob(BadJob1.class).withIdentity("badJob1", "group1").usingJobData("denominator", "0").build();

    // 创建简单触发器，每10秒重复执行
    SimpleTrigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).repeatForever()).build();

    // 将任务和触发器提交给调度器
    Date ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在以下时间运行: " + ft + " 并重复: " + trigger.getRepeatCount() + " 次，每 "
             + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    // badJob2 将每5秒运行一次
    // 这个任务会抛出异常并且永远不会重新触发
    // 它将取消所有相关的触发器
    job = newJob(BadJob2.class).withIdentity("badJob2", "group1").build();

    // 创建第二个简单触发器，每5秒重复执行
    trigger = newTrigger().withIdentity("trigger2", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(5).repeatForever()).build();

    // 将第二个任务和触发器提交给调度器
    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在以下时间运行: " + ft + " 并重复: " + trigger.getRepeatCount() + " 次，每 "
             + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    log.info("------- 启动调度器 ----------------");

    // 任务只有在调用 start() 方法后才开始触发执行
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    try {
      // 等待30秒以观察任务执行和异常处理行为
      // 在这期间可以观察到：
      // 1. BadJob1 的异常、修复和重新执行
      // 2. BadJob2 的异常和触发器取消
      Thread.sleep(30L * 1000L);
    } catch (Exception e) {
      // 忽略中断异常
    }

    log.info("------- 关闭调度器 ---------------------");

    // 关闭调度器，false表示不等待正在执行的任务完成
    sched.shutdown(false);

    log.info("------- 调度器关闭完成 -----------------");

    // 获取调度器元数据并输出执行统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");
  }

  /**
   * 应用程序入口点
   * 
   * <p>
   * 本示例演示了 Quartz 中的异常处理机制：
   * </p>
   * 
   * <p>
   * 执行流程：
   * 1. BadJob1 首次执行时会因除零异常失败
   * 2. BadJob1 修复数据后立即重新执行并成功
   * 3. BadJob1 后续执行正常
   * 4. BadJob2 执行时会因除零异常失败
   * 5. BadJob2 取消所有触发器，不再执行
   * </p>
   * 
   * <p>
   * 关键学习点：
   * - 可恢复异常使用 setRefireImmediately(true)
   * - 不可恢复异常使用 setUnscheduleAllTriggers(true)
   * - 异常处理中可以修复数据
   * - 有状态任务的异常处理特性
   * </p>
   * 
   * @param args 命令行参数（未使用）
   * @throws Exception 如果示例执行过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    JobExceptionExample example = new JobExceptionExample();
    example.run();
  }

}
