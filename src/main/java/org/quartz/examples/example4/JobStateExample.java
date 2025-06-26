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

package org.quartz.examples.example4;

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
 * 本示例演示如何向任务传递参数以及如何维护任务状态
 *
 * 主要功能：
 * 1. 创建带有不同参数的任务实例
 * 2. 演示任务状态在执行之间的持久化
 * 3. 展示 @PersistJobDataAfterExecution 和 @DisallowConcurrentExecution 注解的作用
 * 4. 对比实例变量与 JobDataMap 在状态维护方面的差异
 *
 * @author Bill Kratzer
 */
public class JobStateExample {

  public void run() throws Exception {
    // 初始化日志记录器
    Logger log = LoggerFactory.getLogger(JobStateExample.class);

    log.info("------- 初始化调度器 -------------------");

    // 首先获取调度器实例
    // StdSchedulerFactory 是 Quartz 的标准调度器工厂
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 --------");

    log.info("------- 调度任务 ----------------");

    // 获取一个"整齐"的未来时间点（10秒后）
    // 这样可以确保任务在一个整数秒开始执行
    Date startTime = nextGivenSecondDate(null, 10);

    // 创建第一个任务：job1 将运行5次（开始时间 + 4次重复），每10秒执行一次
    // JobDetail 定义了任务的具体信息，包括任务类、身份标识等
    JobDetail job1 = newJob(ColorJob.class).withIdentity("job1", "group1").build();

    // 为 job1 创建简单触发器
    // 设置开始时间、执行间隔（10秒）和重复次数（4次）
    SimpleTrigger trigger1 = newTrigger().withIdentity("trigger1", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(4)).build();

    // 向任务传递初始化参数
    // JobDataMap 是任务参数和状态的容器
    // 设置喜欢的颜色为绿色
    job1.getJobDataMap().put(ColorJob.FAVORITE_COLOR, "Green");
    // 设置初始执行次数为1
    job1.getJobDataMap().put(ColorJob.EXECUTION_COUNT, 1);

    // 将任务和触发器注册到调度器中
    // scheduleJob 方法返回任务的实际开始时间
    Date scheduleTime1 = sched.scheduleJob(job1, trigger1);
      log.info("{} 将在: {} 开始运行，重复: {} 次，每 {} 秒执行一次", new Object[]{job1.getKey(), scheduleTime1, trigger1.getRepeatCount(), trigger1.getRepeatInterval() / 1000});

    // 创建第二个任务：job2 也将运行5次，每10秒执行一次
    // 使用相同的任务类但不同的身份标识和参数
    JobDetail job2 = newJob(ColorJob.class).withIdentity("job2", "group1").build();

    // 为 job2 创建简单触发器，配置与 job1 相同
    SimpleTrigger trigger2 = newTrigger().withIdentity("trigger2", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(4)).build();

    // 向 job2 传递初始化参数
    // 这个任务有不同的喜欢颜色！演示同一任务类的不同实例可以有不同的参数
    // 设置喜欢的颜色为红色
    job2.getJobDataMap().put(ColorJob.FAVORITE_COLOR, "Red");
    // 设置初始执行次数为1
    job2.getJobDataMap().put(ColorJob.EXECUTION_COUNT, 1);

    // 将第二个任务和触发器注册到调度器中
    Date scheduleTime2 = sched.scheduleJob(job2, trigger2);
      log.info("{} 将在: {} 开始运行，重复: {} 次，每 {} 秒执行一次", new Object[]{job2.getKey().toString(), scheduleTime2, trigger2.getRepeatCount(), trigger2.getRepeatInterval() / 1000});

    log.info("------- 启动调度器 ----------------");

    // 所有任务都已添加到调度器中，但在调度器启动之前，
    // 任何任务都不会运行
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    log.info("------- 等待 60 秒观察任务执行... -------------");
    try {
      // 等待60秒以观察任务的执行情况
      // 在这期间可以看到任务状态的变化和参数的传递
      Thread.sleep(60L * 1000L);
    } catch (Exception e) {
      // 忽略中断异常
    }

    log.info("------- 关闭调度器 ---------------------");

    // 关闭调度器，参数 true 表示等待当前正在执行的任务完成
    sched.shutdown(true);

    log.info("------- 调度器关闭完成 -----------------");

    // 获取调度器的元数据信息，包括执行的任务总数
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");

  }

  /**
   * 程序入口点
   * 创建示例实例并运行演示
   */
  public static void main(String[] args) throws Exception {

    JobStateExample example = new JobStateExample();
    example.run();
  }

}
