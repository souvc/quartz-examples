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
 
package org.quartz.examples.example3;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Cron 触发器示例
 * 
 * 本示例演示了 Quartz 调度器使用 Cron 触发器的基本调度功能。
 * Cron 触发器是最强大和灵活的触发器类型，支持复杂的时间调度表达式。
 * 
 * 主要功能:
 * - 演示 7 种不同的 Cron 表达式配置
 * - 展示秒级、分钟级、小时级的调度规则
 * - 演示工作日和周末的不同调度策略
 * - 展示特定时间段内的执行规则
 * 
 * @author Bill Kratzer
 */
public class CronTriggerExample {

  public void run() throws Exception {
    Logger log = LoggerFactory.getLogger(CronTriggerExample.class);

    log.info("------- Initializing -------------------");

    // 首先获取调度器的引用
    // 使用标准调度器工厂创建调度器实例
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- Initialization Complete --------");

    log.info("------- Scheduling Jobs ----------------");

    // 任务可以在调度器启动之前进行调度配置
    // 所有的任务和触发器都会在调度器启动后开始执行

    // 任务1: 每20秒执行一次
    // Cron表达式 "0/20 * * * * ?" 解释:
    // 0/20: 从第0秒开始，每20秒执行一次
    // *: 每分钟、每小时、每天、每月、每年
    // ?: 不指定星期
    JobDetail job = newJob(SimpleJob.class).withIdentity("job1", "group1").build();

    CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("0/20 * * * * ?"))
        .build();

    Date ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务2: 每隔一分钟执行一次（在每分钟的第15秒执行）
    // Cron表达式 "15 0/2 * * * ?" 解释:
    // 15: 第15秒
    // 0/2: 从第0分钟开始，每2分钟执行一次
    // *: 每小时、每天、每月、每年
    // ?: 不指定星期
    job = newJob(SimpleJob.class).withIdentity("job2", "group1").build();

    trigger = newTrigger().withIdentity("trigger2", "group1").withSchedule(cronSchedule("15 0/2 * * * ?")).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务3: 每隔一分钟执行一次，但仅在上午8点到下午5点之间
    // Cron表达式 "0 0/2 8-17 * * ?" 解释:
    // 0: 第0秒
    // 0/2: 从第0分钟开始，每2分钟执行一次
    // 8-17: 8点到17点（下午5点）
    // *: 每天、每月、每年
    // ?: 不指定星期
    job = newJob(SimpleJob.class).withIdentity("job3", "group1").build();

    trigger = newTrigger().withIdentity("trigger3", "group1").withSchedule(cronSchedule("0 0/2 8-17 * * ?")).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务4: 每3分钟执行一次，但仅在下午5点到晚上11点之间
    // Cron表达式 "0 0/3 17-23 * * ?" 解释:
    // 0: 第0秒
    // 0/3: 从第0分钟开始，每3分钟执行一次
    // 17-23: 17点到23点（晚上11点）
    // *: 每天、每月、每年
    // ?: 不指定星期
    job = newJob(SimpleJob.class).withIdentity("job4", "group1").build();

    trigger = newTrigger().withIdentity("trigger4", "group1").withSchedule(cronSchedule("0 0/3 17-23 * * ?")).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务5: 在每月的1号和15号上午10点执行
    // Cron表达式 "0 0 10am 1,15 * ?" 解释:
    // 0: 第0秒
    // 0: 第0分钟
    // 10am: 上午10点
    // 1,15: 每月的1号和15号
    // *: 每月、每年
    // ?: 不指定星期
    job = newJob(SimpleJob.class).withIdentity("job5", "group1").build();

    trigger = newTrigger().withIdentity("trigger5", "group1").withSchedule(cronSchedule("0 0 10am 1,15 * ?")).build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务6: 每30秒执行一次，但仅在工作日（周一到周五）
    // Cron表达式 "0,30 * * ? * MON-FRI" 解释:
    // 0,30: 第0秒和第30秒
    // *: 每分钟、每小时
    // ?: 不指定日期
    // *: 每月、每年
    // MON-FRI: 周一到周五
    job = newJob(SimpleJob.class).withIdentity("job6", "group1").build();

    trigger = newTrigger().withIdentity("trigger6", "group1").withSchedule(cronSchedule("0,30 * * ? * MON-FRI"))
        .build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    // 任务7: 每30秒执行一次，但仅在周末（周六和周日）
    // Cron表达式 "0,30 * * ? * SAT,SUN" 解释:
    // 0,30: 第0秒和第30秒
    // *: 每分钟、每小时
    // ?: 不指定日期
    // *: 每月、每年
    // SAT,SUN: 周六和周日
    job = newJob(SimpleJob.class).withIdentity("job7", "group1").build();

    trigger = newTrigger().withIdentity("trigger7", "group1").withSchedule(cronSchedule("0,30 * * ? * SAT,SUN"))
        .build();

    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
             + trigger.getCronExpression());

    log.info("------- Starting Scheduler ----------------");

    // 所有任务都已添加到调度器中，但在调度器启动之前
    // 任何任务都不会执行
    // 这是 Quartz 的重要特性：任务调度和任务执行是分离的
    sched.start();

    log.info("------- Started Scheduler -----------------");

    log.info("------- Waiting five minutes... ------------");
    try {
      // 等待5分钟以观察任务执行情况
      // 300秒 = 5分钟，在此期间可以观察不同Cron触发器的执行模式
      Thread.sleep(300L * 1000L);
      // 任务正在后台执行...
    } catch (Exception e) {
      // 处理线程中断异常
      log.warn("等待过程中发生异常: " + e.getMessage());
    }

    log.info("------- Shutting Down ---------------------");

    // 关闭调度器，参数true表示等待当前正在执行的任务完成后再关闭
    sched.shutdown(true);

    log.info("------- Shutdown Complete -----------------");

    // 获取调度器的元数据信息，包含执行统计
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");

  }

  /**
   * 主方法 - 程序入口点
   * 
   * @param args 命令行参数
   * @throws Exception 如果调度过程中发生异常
   */
  public static void main(String[] args) throws Exception {
    // 创建示例实例并运行
    CronTriggerExample example = new CronTriggerExample();
    example.run();
  }

}
